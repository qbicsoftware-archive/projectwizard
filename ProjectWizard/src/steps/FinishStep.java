package steps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import logging.Log4j2Logger;
import model.AttachmentInformation;
import model.ExperimentBean;

import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.WizardStep;

import processes.AttachmentMover;
import processes.MoveUploadsReadyRunnable;
import processes.RegisteredSamplesReadyRunnable;

import uicomponents.UploadsPanel;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;

/**
 * Wizard Step to downloadTSV and upload the TSV file to and from and register samples and context
 * 
 * @author Andreas Friedrich
 * 
 */
public class FinishStep implements WizardStep {

  // "upload successful - it may take a few minutes for your files to show up in the navigator."

  private VerticalLayout main;
  private Label header;
  private Table summary;
//  private CheckBox attach;
  private UploadsPanel uploads;
  private Wizard w;

  private logging.Logger logger = new Log4j2Logger(FinishStep.class);

  public FinishStep(final AttachmentMover mover, int uploadSize, final Wizard w) {
    this.w = w;
    String userID = "admin";
    if (LiferayAndVaadinUtils.isLiferayPortlet())
      try {
        userID = LiferayAndVaadinUtils.getUser().getScreenName();
      } catch (Exception e) {
        logger.error(e.getMessage());
        logger.error("Could not contact Liferay for User screen name.");
      }

    main = new VerticalLayout();
    main.setMargin(true);
    main.setSpacing(true);
    header = new Label("Summary");
    header.setContentMode(ContentMode.PREFORMATTED);
    main.addComponent(header);
    // main.addComponent(ProjectwizardUI.questionize(header,
    // "Here you can download a spreadsheet of the samples in your experiment "
    // + "and register your project in the database. "
    // + "Registering samples may take a few seconds.", "Summary"));

    // Label info =
    // new Label("You can download a spreadsheet of the samples in your experiment. "
    // + "Registering samples may take a few seconds.");
    // info.setStyleName("info");
    // info.setWidth("350px");
    // main.addComponent(info);

    summary = new Table("It consists of these steps:");
    summary.addContainerProperty("Type", String.class, null);
    summary.addContainerProperty("Number of Samples", Integer.class, null);
    summary.setStyleName(ValoTheme.TABLE_SMALL);
    summary.setPageLength(1);
    main.addComponent(summary);
//    attach = new CheckBox("Upload Additional Files");
//    attach.setVisible(false);
//    attach.addValueChangeListener(new ValueChangeListener() {
//
//      @Override
//      public void valueChange(ValueChangeEvent event) {
//        uploads.setVisible(attach.getValue());
//        w.getFinishButton().setVisible(!attach.getValue());
//      }
//    });
//    main.addComponent(attach);
    uploads =
        new UploadsPanel("QTEST", new ArrayList<String>(Arrays.asList("E1 (Design)",
            "E2 (Extraction)", "E3 (Preparation)")), userID, uploadSize);
    uploads.setVisible(false);
    final Button commit = uploads.getCommitButton();
    final FinishStep view = this;
    commit.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        uploads.startCommit();
        mover.moveAttachments(new ArrayList<AttachmentInformation>(uploads.getAttachments().values()),
            uploads.getBar(), uploads.getLabel(), new MoveUploadsReadyRunnable(view));
      }
    });
    main.addComponent(uploads);
  }

  public void fileCommitDone() {
    uploads.commitDone();
    logger.info("Moving of files to Datamover folder complete!");
    Notification n =
        new Notification(
            "Registration of files complete. It might take a few minutes for your files to show up in the navigator. \n"
                + "You can end the project creation by clicking 'Finish'.");
    n.setStyleName(ValoTheme.NOTIFICATION_CLOSABLE);
    n.setDelayMsec(-1);
    n.show(UI.getCurrent().getPage());
    w.getFinishButton().setVisible(true);
  }

  public void setExperimentInfos(String proj, String desc,
      Map<String, List<Sample>> samplesByExperiment) {
    int entities = 0;
    int samples = 0;
    int i = 0;
    for (String exp : samplesByExperiment.keySet()) {
      i++;
      List<Sample> samps = samplesByExperiment.get(exp);
      int amount = samps.size();
      String type = "Unknown";
      String sampleType = samps.get(0).getSampleTypeCode();
      switch (sampleType) {
        case "Q_BIOLOGICAL_ENTITY":
          entities += amount;
          type = "Sample Sources";
          break;
        case "Q_BIOLOGICAL_SAMPLE":
          samples += amount;
          type = "Sample Extracts";
          break;
        case "Q_TEST_SAMPLE":
          samples += amount;
          type = "Sample Preparations";
          break;
        default:
          break;
      }
      summary.addItem(new Object[] {type, amount}, i);
    }
    summary.setPageLength(summary.size());
    header.setValue("Your Experimental Design was registered. Project " + proj + " now has "
        + entities + " Sample Sources and " + samples + " samples. \n" + "Project description: "
        + desc.substring(0, Math.min(desc.length(), 60)) + "...");
//    enableUploads(true);
    w.getFinishButton().setVisible(true);//TODO
  }

  @Override
  public String getCaption() {
    return "Summary";
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    return true;
  }

  @Override
  public boolean onBack() {
    return true;
  }

//  private void enableUploads(boolean b) {
//    attach.setVisible(true);
//  }

  public void resetSummary() {
    summary.removeAllItems();
  }

}
