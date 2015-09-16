package steps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import main.ProjectwizardUI;
import model.ExperimentBean;
import model.NewSampleModelBean;

import org.vaadin.teemu.wizards.WizardStep;

import componentwrappers.CustomVisibilityComponent;
import componentwrappers.StandardTextField;
import uicomponents.ProjectSelectionComponent;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Wizard Step to set the Context of the new experiment and sample creation
 * 
 * @author Andreas Friedrich
 * 
 */
public class ProjectContextStep implements WizardStep {

  private VerticalLayout main;

  private ComboBox spaceCode;
  private ProjectSelectionComponent projectInfoComponent;
  private TextField expName;

  List<ExperimentBean> experiments;

  private Table experimentTable;

  private Table samples;

  List<String> contextOptions = new ArrayList<String>(Arrays.asList("Add a new experiment",
      "Add sample extraction to existing biological entities",
      "Measure existing extracted samples again", // "Copy parts of a project",
      "Download existing sample spreadsheet"));
  private CustomVisibilityComponent projectContext;

  private GridLayout grid;

  /**
   * Create a new Context Step for the wizard
   * 
   * @param openbisSpaces List of Spaces to select from in the openBIS instance
   * @param newProjectCode
   */
  public ProjectContextStep(List<String> openbisSpaces, ProjectSelectionComponent projSelect) {
    main = new VerticalLayout();
    main.setMargin(true);
    main.setSpacing(true);
    main.setSizeUndefined();
    Collections.sort(openbisSpaces);
    spaceCode = new ComboBox("Project Name", openbisSpaces);
    spaceCode.setStyleName(ProjectwizardUI.boxTheme);
    spaceCode.setNullSelectionAllowed(false);
    spaceCode.setImmediate(true);

    projectInfoComponent = projSelect;
    projectInfoComponent.setImmediate(true);
    projectInfoComponent.setVisible(false);

    projectContext = new CustomVisibilityComponent(new OptionGroup("", contextOptions));
    projectContext.setVisible(false);

    disableContextOptions();

    experimentTable = new Table("Applicable Experiments");
    experimentTable.setStyleName(ValoTheme.TABLE_SMALL);
    experimentTable.setPageLength(1);
    experimentTable.setContainerDataSource(new BeanItemContainer<ExperimentBean>(
        ExperimentBean.class));
    experimentTable.setSelectable(true);
    experimentTable.setVisible(false);

    samples = new Table("Sample Overview");
    samples.setStyleName(ValoTheme.TABLE_SMALL);
    samples.setVisible(false);
    samples.setPageLength(1);
    samples.setContainerDataSource(new BeanItemContainer<NewSampleModelBean>(
        NewSampleModelBean.class));

    // Label info =
    // new Label(
    // "If you want to add to or copy an existing experiment, please select the experiment. "
    // +
    // "When copying lower tier samples, they will be attached to existing samples that are higher in the hierarchy."
    // + " Downloaded TSVs will always contain all attached sample tiers.");
    // info.setStyleName("info");
    // info.setWidth("350px");

    expName = new StandardTextField("Experiment name");
    expName.setVisible(false);
    expName.setInputPrompt("Optional short name");

    grid = new GridLayout(2, 5);
    grid.setSpacing(true);
    grid.setMargin(true);
    grid.addComponent(
        ProjectwizardUI.questionize(spaceCode, "Name of the project", "Project Name"), 0, 0);
    grid.addComponent(projectInfoComponent, 0, 1);
    Component context =
        ProjectwizardUI
            .questionize(
                projectContext,
                "If this experiment's organisms or "
                    + "tissue extracts are already registered at QBiC from an earlier experiment, you can chose the second "
                    + "option (new tissue extracts from old organism) or the third (new measurements from old tissue extracts). "
                    + "You can also download existing sample information by choosing the last option.",
                "Project Context");
    grid.addComponent(context, 0, 2);
    grid.addComponent(experimentTable, 0, 3);
    grid.addComponent(samples, 1, 2, 1, 3);
    // expNameLayout = new VerticalLayout();
    // expNameLayout.addComponent(expName);
    grid.addComponent(expName, 0, 4);

    main.addComponent(grid);

    initListeners();
  }

  private void initListeners() {
    projectInfoComponent.getCodeButton().addClickListener(new Button.ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        makeContextVisible();
      }
    });
  }

  public List<ExperimentBean> getExperiments() {
    return experiments;
  }

  public void setProjectCodes(List<String> projects) {
    projectInfoComponent.addItems(projects);
    projectInfoComponent.enableProjectBox(true);
    projectInfoComponent.setVisible(true);
  }

  public void disableContextOptions() {
    for (int i = 0; i < contextOptions.size(); i++)
      projectContext.setItemEnabled(contextOptions.get(i), false);
  }

  public void resetProjects() {
    projectInfoComponent.resetProjects();
    disableContextOptions();
    resetExperiments();
  }

  public void resetContext() {
    projectContext.select(projectContext.getNullSelectionItemId());
  }

  public void resetExperiments() {
    hideExperiments();
    resetContext();
  }

  public void resetSamples() {
    samples.removeAllItems();
    samples.setVisible(false);
  }

  public void setExperiments(List<ExperimentBean> beans) {
    experiments = beans;
  }

  public void hideExperiments() {
    experimentTable.setVisible(false);
    experimentTable.removeAllItems();
    resetSamples();
  }

  public void showExperiments(List<ExperimentBean> beans) {
    BeanItemContainer<ExperimentBean> c =
        new BeanItemContainer<ExperimentBean>(ExperimentBean.class);
    c.addAll(beans);
    experimentTable.setContainerDataSource(c);
    if (c.size() == 1)
      experimentTable.select(c.getIdByIndex(0));
    experimentTable.setPageLength(Math.min(10, c.size()));
    experimentTable.setVisible(true);
  }

  public void setSamples(List<NewSampleModelBean> beans) {
    BeanItemContainer<NewSampleModelBean> c =
        new BeanItemContainer<NewSampleModelBean>(NewSampleModelBean.class);
    c.addAll(beans);
    samples.setPageLength(Math.min(beans.size(), 10));
    samples.setContainerDataSource(c);
    samples.setVisible(true);
  }

  public void enableNewContextOption(boolean enable) {
    projectContext.setItemEnabled(contextOptions.get(0), enable);
  }

  public void enableExtractContextOption(boolean enable) {
    projectContext.setItemEnabled(contextOptions.get(1), enable);
  }

  public void enableMeasureContextOption(boolean enable) {
    projectContext.setItemEnabled(contextOptions.get(2), enable);
  }

  // public void enableCopyContextOption(boolean enable) {
  // projectContext.setItemEnabled(contextOptions.get(3), enable);
  // }

  public void enableTSVWriteContextOption(boolean enable) {
    projectContext.setItemEnabled(contextOptions.get(3), enable);
  }

  public List<String> getContextOptions() {
    return contextOptions;
  }

  public OptionGroup getProjectContext() {
    return (OptionGroup) projectContext.getInnerComponent();
  }

  @Override
  public String getCaption() {
    return "Project";
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    Notification n;
    if (spaceReady() && projectReady()) {
      if (inherit() || copy() || readOnly())
        if (expSelected())
          return true;
        else {
          n = new Notification("Please select an existing experiment.");
        }
      else
        return true;
    } else {
      n = new Notification("Please select a project and subproject or create a new one.");
    }
    n.setStyleName(ValoTheme.NOTIFICATION_CLOSABLE);
    n.setDelayMsec(-1);
    n.show(UI.getCurrent().getPage());
    return false;
  }

  private boolean expSelected() {
    return (getSamples().size() > 0);
  }

  private boolean inherit() {
    String context = (String) projectContext.getValue();
    return (contextOptions.get(1).equals(context) || contextOptions.get(2).equals(context));
  }

  private boolean copy() {
    String context = (String) projectContext.getValue();
    return contextOptions.get(3).equals(context);
  }

  private boolean readOnly() {
    return false;
    // String context = (String) projectContext.getValue();
    // return contextOptions.get(4).equals(context);
  }

  private boolean projectReady() {
    return getProjectCode() != null && !getProjectCode().isEmpty();
  }

  private boolean spaceReady() {
    return spaceCode.getValue() != null && !spaceCode.getValue().toString().isEmpty();
  }

  @Override
  public boolean onBack() {
    return true;
  }

  public String getProjectCode() {
    return this.projectInfoComponent.getSelectedProject();
  }

  public String getSpaceCode() {
    return (String) this.spaceCode.getValue();
  }

  public ComboBox getProjectBox() {
    return projectInfoComponent.getProjectBox();
  }

  public TextField getProjectCodeField() {
    return projectInfoComponent.getProjectField();
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
    samples.setSelectable(true);
    samples.setMultiSelect(true);
    samples.setValue(samples.getItemIds());
    res.addAll((Collection<? extends NewSampleModelBean>) samples.getValue());
    samples.setMultiSelect(false);
    samples.setSelectable(false);
    return res;
  }

  // public boolean copyModeSet() {
  // String context = (String) projectContext.getValue();
  // return contextOptions.get(3).equals(context);
  // }

  public boolean fetchTSVModeSet() {
    String context = (String) projectContext.getValue();
    return contextOptions.get(3).equals(context);
  }

  public boolean expSecondaryNameSet() {
    return expName != null && !expName.isEmpty();
  }

  public String getDescription() {
    return projectInfoComponent.getProjectDescription();
  }

  public String getExpSecondaryName() {
    return expName.getValue();
  }

  public void tryEnableCustomProject(String code) {
    projectInfoComponent.tryEnableCustomProject(code);
  }

  public void makeContextVisible() {
    projectContext.setVisible(true);
  }

  public void enableExpName(boolean b) {
    expName.setVisible(b);
  }

}
