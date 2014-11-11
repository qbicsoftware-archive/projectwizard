package ui;

import java.util.ArrayList;
import java.util.List;

import main.SampleSummaryBean;
import model.ExperimentBean;
import model.ISampleBean;

import org.vaadin.teemu.wizards.WizardStep;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

public class UploadRegisterStep implements WizardStep {

  private VerticalLayout main;
  private Button register;
  private TextArea error;
  private Upload upload;
  private Table summary;
  private List<List<List<ISampleBean>>> samples;
  private Label info;
  private ProgressBar bar;

  public UploadRegisterStep() {
    main = new VerticalLayout();
    main.setMargin(true);
    error = new TextArea();
    error.setVisible(false);
    summary = new Table("Summary");
    summary.setVisible(false);
    main.addComponent(summary);
    summary.setPageLength(6);
    register = new Button("Register All");
    register.setEnabled(false);
    main.addComponent(register);
    info = new Label();
    bar = new ProgressBar();
    main.addComponent(info);
    main.addComponent(bar);
  }

  public void initUpload(Upload upload) {
    this.upload = upload;
    main.addComponent(this.upload);
    main.addComponent(error);
  }

  public void setError(String error) {
    this.error.setValue(error);
    this.error.setVisible(true);
  }

  public void clearError() {
    this.error.setValue("");
    this.error.setVisible(false);
  }

  @Override
  public String getCaption() {
    return "Register Experiments";
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    return false;
  }

  @Override
  public boolean onBack() {
    return true;
  }

  public Button getRegisterButton() {
    return this.register;
  }

  public void setSummary(ArrayList<SampleSummaryBean> beans) {
    summary.setVisible(false);
    BeanItemContainer<SampleSummaryBean> c =
        new BeanItemContainer<SampleSummaryBean>(SampleSummaryBean.class);
    c.addAll(beans);
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
    System.out.println("All done!");
  }

  public ProgressBar getProgressBar() {
    return bar;
  }

  public Label getProgressLabel() {
    return info;
  }

}
