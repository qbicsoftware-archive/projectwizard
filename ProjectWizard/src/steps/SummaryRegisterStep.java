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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logging.Log4j2Logger;
import main.IOpenBisClient;
import main.OpenBisClient;
import main.ProjectwizardUI;
import main.SampleSummaryBean;
import model.ExperimentType;
import model.ISampleBean;
import model.OpenbisExperiment;
import model.notes.Note;
import uicomponents.ExperimentSummaryTable;

import org.apache.commons.lang.WordUtils;
import org.vaadin.teemu.wizards.WizardStep;

import views.IRegistrationView;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
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
  private Button downloadGraph;
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
  private String projectIdentifier;
  private String projectName;
  private boolean isEmptyProject = false;
  private List<OpenbisExperiment> exps;
  private List<Note> notes;

  public SummaryRegisterStep(DBManager dbm, IOpenBisClient openbis) {
    this.dbm = dbm;
    this.openbis = openbis;
    main = new VerticalLayout();
    main.setMargin(true);
    main.setSpacing(true);
    Label header = new Label("Sample Registration");
    main.addComponent(ProjectwizardUI.questionize(header,
        "Here you can download a spreadsheet of the samples in your experiment "
            + "and register your project in the database. "
            + "Registering samples may take a few seconds.",
        "Sample Registration"));

    summary = new ExperimentSummaryTable();

    summaryComponent = new CustomVisibilityComponent(ProjectwizardUI.questionize(summary,
        "This is a summary of samples for Sample Sources/Patients, Tissue Extracts and "
            + "samples that will be measured.",
        "Experiment Summary"));
    summaryComponent.setVisible(false);
    main.addComponent(summaryComponent.getInnerComponent());

    downloadTSV = new Button("Download Spreadsheet");
    downloadTSV.setEnabled(false);
    HorizontalLayout tsvInfo = new HorizontalLayout();
    tsvInfo.addComponent(downloadTSV);
    main.addComponent(ProjectwizardUI.questionize(tsvInfo,
        "You can download a technical spreadsheet to register your samples at a later time instead. More informative spreadsheets are available in the next step.",
        "TSV Download"));

    downloadGraph = new Button("Download Graph");
    downloadGraph.setEnabled(false);
    // main.addComponent(downloadGraph);

    register = new Button("Register All");
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
    downloadGraph.setEnabled(enabled);
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

  public void setRegEnabled(boolean b) {
    register.setEnabled(b);
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
    registrationComplete = true;
  }

  public void setPeopleAndProject(int investigator, int contact, String projectIdentifier,
      String projectName, List<OpenbisExperiment> exps) {
    this.investigatorID = investigator;
    this.contactID = contact;
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

  public void setEmptyProject(boolean b) {
    if(b)
    register.setCaption("Register Project");
    else
      register.setCaption("Register All");
    this.isEmptyProject = b;
  }

  public boolean summaryIsSet() {
    return (summary.size() > 0 || isEmptyProject);
  }

  public void resetSummary() {
    summary.removeAllItems();
  }

  public Button getGraphButton() {
    return downloadGraph;
  }

  public void setProjectNotes(List<Note> notes) {
    this.notes = notes;
  }

}
