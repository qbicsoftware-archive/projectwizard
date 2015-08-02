package steps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import logging.Log4j2Logger;
import main.ProjectwizardUI;
import main.SampleSummaryBean;
import model.ISampleBean;

import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.WizardStep;

import views.IRegistrationView;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import componentwrappers.CustomVisibilityComponent;
import control.WizardController;

/**
 * Wizard Step to downloadTSV and upload the TSV file to and from and register samples and
 * experiments
 * 
 * @author Andreas Friedrich
 * 
 */
public class SummaryRegisterStep implements WizardStep, IRegistrationView {

  private VerticalLayout main;
  private Button downloadTSV;
  private Button register;
  private Button downloadGraph;
  private Table summary;
  private List<List<List<ISampleBean>>> samples;
  private Label registerInfo;
  private ProgressBar bar;
  private Wizard w;
  private CustomVisibilityComponent summaryComponent;
  logging.Logger logger = new Log4j2Logger(SummaryRegisterStep.class);

  public SummaryRegisterStep(Wizard w) {
    this.w = w;
    main = new VerticalLayout();
    main.setMargin(true);
    main.setSpacing(true);
    Label header = new Label("Sample Registration");
    main.addComponent(ProjectwizardUI.questionize(header,
        "Here you can download a spreadsheet of the samples in your experiment "
            + "and register your project in the database. "
            + "Registering samples may take a few seconds.", "Sample Registration"));

    // Label info =
    // new Label("You can download a spreadsheet of the samples in your experiment. "
    // + "Registering samples may take a few seconds.");
    // info.setStyleName("info");
    // info.setWidth("350px");
    // main.addComponent(info);

    summary = new Table("Summary");
    summary.setStyleName(ValoTheme.TABLE_SMALL);
    summary.setPageLength(3);
    summaryComponent =
        new CustomVisibilityComponent(
            ProjectwizardUI
                .questionize(
                    summary,
                    "This is a summary of samples for Biological Entities, Tissue Extracts and "
                        + "samples that will be measured. A spreadsheet of these samples can be downloaded below.",
                    "Experiment Summary"));
    summaryComponent.setVisible(false);
    main.addComponent(summaryComponent.getInnerComponent());

    downloadTSV = new Button("Download Spreadsheet");
    downloadTSV.setEnabled(false);
    main.addComponent(downloadTSV);

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
    return true;
  }

  @Override
  public boolean onBack() {
    return true;
  }

  public Button getRegisterButton() {
    return this.register;
  }

  public void setSummary(ArrayList<SampleSummaryBean> arrayList) {
    summaryComponent.setVisible(false);
    BeanItemContainer<SampleSummaryBean> c =
        new BeanItemContainer<SampleSummaryBean>(SampleSummaryBean.class);
    c.addAll(arrayList);
    summary.setPageLength(arrayList.size());
    summary.setContainerDataSource(c);
    summaryComponent.setVisible(true);
    enableDownloads(true);
  }

  public void setProcessed(List<List<List<ISampleBean>>> processed) {
    samples = processed;
  }

  public void setRegEnabled(boolean b) {
    register.setEnabled(b);
  }

  public List<List<List<ISampleBean>>> getSamples() {
    return samples;
  }

  public void registrationDone() {
    logger.info("Registration complete!");
    w.getFinishButton().setVisible(true);
    Notification n =
        new Notification(
            "Registration of samples complete. You can end the project creation by clicking 'Finish'.");
    n.setStyleName(ValoTheme.NOTIFICATION_CLOSABLE);
    n.setDelayMsec(-1);
    n.show(UI.getCurrent().getPage());
  }

  public ProgressBar getProgressBar() {
    return bar;
  }

  public Label getProgressLabel() {
    return registerInfo;
  }

  public boolean summaryIsSet() {
    return (summary.size() > 0);
  }

  public void resetSummary() {
    summary.removeAllItems();
  }

  public Button getGraphButton() {
    return downloadGraph;
  }

}
