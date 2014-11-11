package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import model.ExperimentBean;
import model.ExperimentType;
import model.NewSampleModelBean;

import org.vaadin.teemu.wizards.WizardStep;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class ProjectContextStep implements WizardStep {

  private VerticalLayout main;

  private ComboBox spaceCode;
  private ComboBox projectCode;

  List<ExperimentBean> experiments;

  private Table experimentTable;

  private Table samples;

  List<String> contextOptions = new ArrayList<String>(Arrays.asList(
      "Add completely new experiment",
      "Add new sample extraction to a project with existing biological entities",
      "Add to project by measuring existing extracted samples again"));
  private OptionGroup projectContext;

  public ProjectContextStep(List<String> openbisSpaces) {
    main = new VerticalLayout();
    main.setMargin(true);
    main.setSpacing(true);
    main.setSizeUndefined();
    Collections.sort(openbisSpaces);
    spaceCode = new ComboBox("Space Name", openbisSpaces);
    spaceCode.setNullSelectionAllowed(false);
    spaceCode.setImmediate(true);

    projectCode = new ComboBox("Project Code");
    projectCode.setEnabled(false);
    projectCode.setImmediate(true);
    projectCode.setNullSelectionAllowed(false);

    main.addComponent(spaceCode);
    main.addComponent(projectCode);
    projectContext = new OptionGroup("", contextOptions);
    projectContext.setItemEnabled(contextOptions.get(1), false);
    projectContext.setItemEnabled(contextOptions.get(2), false);
    main.addComponent(projectContext);

    experimentTable = new Table("Applicable Experiments");
    experimentTable.setPageLength(6);
    experimentTable.setContainerDataSource(new BeanItemContainer<ExperimentBean>(
        ExperimentBean.class));
    experimentTable.setSelectable(true);
    main.addComponent(experimentTable);

    samples = new Table("Existing Samples");
    samples.setPageLength(10);
    samples.setContainerDataSource(new BeanItemContainer<NewSampleModelBean>(
        NewSampleModelBean.class));
    samples.setSelectable(true);
    samples.setMultiSelect(true);
    main.addComponent(samples);
  }

  public void setProjectCodes(List<String> projects) {
    projectCode.addItems(projects);
    projectCode.setEnabled(true);
  }
  
  public void resetProjects() {
    projectCode.removeAllItems();
    projectCode.setEnabled(false);
    projectContext.setItemEnabled(contextOptions.get(1), false);
    projectContext.setItemEnabled(contextOptions.get(2), false);
    resetExperiments();
  }
  
  public void resetContext() {
    projectContext.select(projectContext.getNullSelectionItemId());
  }

  public void resetExperiments() {
    experimentTable.removeAllItems();
    resetContext();
    resetSamples();
  }

  public void resetSamples() {
    samples.removeAllItems();
  }

  public void setExperiments(List<ExperimentBean> beans) {
    experiments = beans;
  }

  public void showExperiments() {
    String context = (String) projectContext.getValue();
    List<ExperimentBean> beans = new ArrayList<ExperimentBean>();
    if (contextOptions.get(1).equals(context)) {
      for (ExperimentBean b : experiments) {
        if (b.getExperiment_type().equals(ExperimentType.Q_EXPERIMENTAL_DESIGN.toString()))
          beans.add(b);
      }
    }
    if (contextOptions.get(2).equals(context)) {
      for (ExperimentBean b : experiments) {
        if (b.getExperiment_type().equals(ExperimentType.Q_SAMPLE_EXTRACTION.toString()))
          beans.add(b);
      }
    }
    BeanItemContainer<ExperimentBean> c =
        new BeanItemContainer<ExperimentBean>(ExperimentBean.class);
    c.addAll(beans);
    experimentTable.setContainerDataSource(c);
    if(c.size() == 1)
      experimentTable.select(c.getIdByIndex(0));
  }

  public void setSamples(List<NewSampleModelBean> beans) {
    BeanItemContainer<NewSampleModelBean> c =
        new BeanItemContainer<NewSampleModelBean>(NewSampleModelBean.class);
    c.addAll(beans);
    samples.setContainerDataSource(c);
  }

  public void enableExtractContextOption(boolean enable) {
    projectContext.setItemEnabled(contextOptions.get(1), enable);
  }

  public void enableMeasureContextOption(boolean enable) {
    projectContext.setItemEnabled(contextOptions.get(2), enable);
  }

  public List<String> getContextOptions() {
    return contextOptions;
  }

  public OptionGroup getProjectContext() {
    return projectContext;
  }

  @Override
  public String getCaption() {
    return "Project context";
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    System.out.println(getSamples());
    if (spaceReady() && projectReady())
      return true;
    else
      return false;
  }

  private boolean projectReady() {
    return projectCode.getValue() != null && !projectCode.getValue().toString().isEmpty();
  }

  private boolean spaceReady() {
    return spaceCode.getValue() != null && !spaceCode.getValue().toString().isEmpty();
  }

  @Override
  public boolean onBack() {
    return true;
  }

  public String getProjectCode() {
    return (String) this.projectCode.getValue();
  }

  public String getSpaceCode() {
    return (String) this.spaceCode.getValue();
  }

  public ComboBox getProjectBox() {
    return projectCode;
  }

  public ComboBox getSpaceBox() {
    return spaceCode;
  }

  public Table getExperimentTable() {
    return experimentTable;
  }

  public ExperimentBean getExperimentName() {
    return (ExperimentBean) experimentTable.getValue();
  }

  @SuppressWarnings("unchecked")
  public List<NewSampleModelBean> getSamples() {
    List<NewSampleModelBean> res = new ArrayList<NewSampleModelBean>();
    res.addAll((Collection<? extends NewSampleModelBean>) samples.getValue());
    return res;
  }

}
