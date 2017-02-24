/*******************************************************************************
 * QBiC Project Wizard enables users to create hierarchical experiments including different study
 * conditions using factorial design. Copyright (C) "2016" Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package steps;

import io.DBManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import logging.Log4j2Logger;
import main.IOpenBisClient;
import uicomponents.Styles;
import main.SampleSummaryBean;
import model.ExperimentType;
import model.ISampleBean;
import model.OpenbisExperiment;
import model.RegistrationMode;
import model.notes.Note;
import uicomponents.ExperimentSummaryTable;

import org.vaadin.hene.flexibleoptiongroup.FlexibleOptionGroup;
import org.vaadin.hene.flexibleoptiongroup.FlexibleOptionGroupItemComponent;
import org.vaadin.teemu.wizards.WizardStep;

import views.IRegistrationView;

import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;

import componentwrappers.CustomVisibilityComponent;
import control.Functions;
import control.Functions.NotificationType;

/**
 * Wizard Step to downloadTSV and upload the TSV file to and from and register samples and context
 * 
 * @author Andreas Friedrich
 * 
 */
public class SummaryRegisterStep implements WizardStep, IRegistrationView {

  private VerticalLayout main;
  private Button downloadTSV;
  private Button register;
  private ExperimentSummaryTable summary;
  private List<List<ISampleBean>> samples;
  private Label registerInfo;
  private ProgressBar bar;
  private CustomVisibilityComponent summaryComponent;
  private logging.Logger logger = new Log4j2Logger(SummaryRegisterStep.class);
  private boolean registrationComplete = false;
  private DBManager dbm;
  private IOpenBisClient openbis;
  private int investigatorID;
  private int contactID;
  private int managerID;
  private String projectIdentifier;
  private String projectName;
  private List<OpenbisExperiment> exps;
  private List<Note> notes;
  private FlexibleOptionGroup optionGroup;
  private VerticalLayout optionLayout;
  private RegistrationMode registrationMode;
  private final String paidOption = "I am aware of all costs associated with this project, "
      + "since I previously signed a corresponding agreement "
      + "and I agree to pay all charges upon receival of the "
      + "invoice. This submission is binding.";
  private final String freeOption = "I understand that this experimental design draft will "
      + "now be submitted for QBiC review. I thereby request "
      + "a consultancy meeting. There are no costs associated " + "with this submission.";

  public SummaryRegisterStep(DBManager dbm, IOpenBisClient openbis) {
    this.dbm = dbm;
    this.openbis = openbis;
    main = new VerticalLayout();
    main.setMargin(true);
    main.setSpacing(true);
    Label header = new Label("Sample Registration");
    main.addComponent(Styles.questionize(header,
        "Here you can download a spreadsheet of the samples in your experiment "
            + "and register your project in the database. "
            + "Registering samples may take a few seconds.",
        "Sample Registration"));

    summary = new ExperimentSummaryTable();

    summaryComponent = new CustomVisibilityComponent(Styles.questionize(summary,
        "This is a summary of samples for Sample Sources/Patients, Tissue Extracts and "
            + "samples that will be measured.",
        "Experiment Summary"));
    summaryComponent.setVisible(false);
    main.addComponent(summaryComponent.getInnerComponent());

    downloadTSV = new Button("Download Spreadsheet");
    downloadTSV.setEnabled(false);
    HorizontalLayout tsvInfo = new HorizontalLayout();
    tsvInfo.addComponent(downloadTSV);
    main.addComponent(Styles.questionize(tsvInfo,
        "You can download a technical spreadsheet to register your samples at a later time instead. More informative spreadsheets are available in the next step.",
        "TSV Download"));

    Container cont = new IndexedContainer();
    cont.addContainerProperty("caption", String.class, "");
    cont.getContainerProperty(cont.addItem(), "caption").setValue(paidOption);
    cont.getContainerProperty(cont.addItem(), "caption").setValue(freeOption);
    optionGroup = new FlexibleOptionGroup(cont);
    optionGroup.setItemCaptionPropertyId("caption");

    optionLayout = new VerticalLayout();
    Iterator<FlexibleOptionGroupItemComponent> iter;
    iter = optionGroup.getItemComponentIterator();
    while (iter.hasNext()) {
      FlexibleOptionGroupItemComponent fogItemComponent = iter.next();
      Label caption = new Label(fogItemComponent.getCaption());
      caption.setWidth("400px");
      optionLayout.addComponent(new HorizontalLayout(fogItemComponent, caption));
    }

    main.addComponent(optionLayout);
    optionGroup.addValueChangeListener(new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        testRegEnabled();
      }
    });

    register = new Button("Register All Samples");
    register.setEnabled(false);
    main.addComponent(register);

    registerInfo = new Label();
    bar = new ProgressBar();
    main.addComponent(registerInfo);
    main.addComponent(bar);
  }

  public void setSummary(List<SampleSummaryBean> summaries) {
    summary.setSamples(summaries);
    summaryComponent.setVisible(true);
    enableDownloads(true);
  }

  public void enableDownloads(boolean enabled) {
    downloadTSV.setEnabled(enabled);
  }

  public Button getDownloadButton() {
    return this.downloadTSV;
  }

  @Override
  public String getCaption() {
    return "Registration";
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    return registrationComplete();
  }

  private boolean registrationComplete() {
    return registrationComplete;
  }

  @Override
  public boolean onBack() {
    return true;
  }

  public Button getRegisterButton() {
    return this.register;
  }

  public void setProcessed(List<List<ISampleBean>> processed) {
    samples = processed;
  }

  public void testRegEnabled() {
    boolean optionChosen = optionGroup.getValue() != null;
    register
        .setEnabled(optionChosen || registrationMode.equals(RegistrationMode.RegisterEmptyProject));
    if (optionChosen) {
      if (registrationReady()) {
        if (optionGroup.getValue().equals(1))
          register.setCaption("Register All Samples");
        if (optionGroup.getValue().equals(2))
          register.setCaption("Send Project to QBiC");
      }
    }
  }

  public List<List<ISampleBean>> getSamples() {
    return samples;
  }

  private void writeNoteToOpenbis(String id, Note note) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("id", id);
    params.put("user", note.getUsername());
    params.put("comment", note.getComment());
    params.put("time", note.getTime());
    openbis.ingest("DSS1", "add-to-xml-note", params);
  }

  public void registrationDone() {
    logger.info("Sample registration complete!");
    for (OpenbisExperiment e : exps) {
      if (e.getType().equals(ExperimentType.Q_EXPERIMENTAL_DESIGN)) {
        String id = projectIdentifier + "/" + e.getOpenbisName();
        for (Note n : notes) {
          writeNoteToOpenbis(id, n);
        }
      }
    }
    int projectID = dbm.addProjectToDB(projectIdentifier, projectName);
    if (investigatorID != -1)
      dbm.addPersonToProject(projectID, investigatorID, "PI");
    if (contactID != -1)
      dbm.addPersonToProject(projectID, contactID, "Contact");
    if (managerID != -1)
      dbm.addPersonToProject(projectID, managerID, "Manager");
    for (OpenbisExperiment e : exps) {
      String identifier = projectIdentifier + "/" + e.getOpenbisName();
      int expID = dbm.addExperimentToDB(identifier);
      if (e.getPersonID() > -1) {
        int person = e.getPersonID();
        dbm.addPersonToExperiment(expID, person, "Contact");
      }
    }
    Functions.notification("Registration complete!",
        "Registration of samples complete. Press 'next' for additional options.",
        NotificationType.SUCCESS);
    optionGroup.setEnabled(false);
    register.setEnabled(false);
    registrationComplete = true;
  }

  public void setPeopleAndProject(int investigator, int contact, int manager, String projectIdentifier,
      String projectName, List<OpenbisExperiment> exps) {
    this.investigatorID = investigator;
    this.contactID = contact;
    this.managerID = manager;
    this.projectIdentifier = projectIdentifier;
    this.projectName = projectName;
    this.exps = exps;
  }

  public ProgressBar getProgressBar() {
    return bar;
  }

  public Label getProgressLabel() {
    return registerInfo;
  }

  private boolean registrationReady() {
    return (summary.size() > 0 || registrationMode.equals(RegistrationMode.RegisterEmptyProject));
  }

  public void resetSummary() {
    summary.removeAllItems();
  }

  public void setProjectNotes(List<Note> notes) {
    this.notes = notes;
  }

  public void setRegistrationMode(RegistrationMode mode) {
    this.registrationMode = mode;
    switch (mode) {
      case RegisterEmptyProject:
        optionLayout.setVisible(false);
        register.setCaption("Register Project");
        break;
      case RegisterSamples:
        optionLayout.setVisible(true);
        register.setCaption("Register All Samples");
        break;
      case DownloadTSV:
        optionLayout.setVisible(false);
        break;
      default:
        logger.error("Unknown registration mode: " + mode);
        break;
    }
  }

}
