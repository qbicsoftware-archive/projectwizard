package views;

import java.util.ArrayList;
import java.util.List;

import logging.Log4j2Logger;
import main.SampleSummaryBean;
import model.ISampleBean;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import control.Uploader;

public class StandaloneTSVImport extends VerticalLayout implements IRegistrationView {

  /**
   * 
   */
  private static final long serialVersionUID = 5358966181721590658L;
  private Button register;
  private Label error;
  private Upload upload;
  private Table summary;
  private List<List<List<ISampleBean>>> samples;
  private Label registerInfo;
  private ProgressBar bar;
  
  logging.Logger logger = new Log4j2Logger(StandaloneTSVImport.class);

  public StandaloneTSVImport() {
    setMargin(true);
    setSpacing(true);

    Label info =
        new Label(
            "Here you can upload a(n edited) tsv file of a previously created project. "
                + "Registering samples may take a few seconds.");
    info.setIcon(FontAwesome.INFO);
    info.setStyleName("info");
    info.setWidth("250px");
    addComponent(info);

    summary = new Table("Summary");
    summary.setStyleName(ValoTheme.TABLE_SMALL);
    summary.setPageLength(3);
    summary.setVisible(false);
    addComponent(summary);

    error = new Label();
    error.setWidth("250px");
    error.setIcon(FontAwesome.INFO);
    error.setVisible(false);
    error.setStyleName("info");
    register = new Button("Register All");
    register.setEnabled(false);
    addComponent(register);

    registerInfo = new Label();
    bar = new ProgressBar();
    addComponent(registerInfo);
    addComponent(bar);
  }

  public void initUpload(Upload upload) {
    this.upload = upload;
    addComponent(this.upload);
    addComponent(error);
  }

  public void setError(String error) {
    this.error.setValue(error);
    this.error.setVisible(true);
  }

  public void clearError() {
    this.error.setValue("");
    this.error.setVisible(false);
  }

  public Button getRegisterButton() {
    return this.register;
  }

  public void setSummary(ArrayList<SampleSummaryBean> arrayList) {
    summary.setVisible(false);
    BeanItemContainer<SampleSummaryBean> c =
        new BeanItemContainer<SampleSummaryBean>(SampleSummaryBean.class);
    c.addAll(arrayList);
    summary.setPageLength(arrayList.size());
    summary.setContainerDataSource(c);
    summary.setVisible(true);
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

}
