/*******************************************************************************
 * QBiC Project Wizard enables users to create hierarchical experiments including different study conditions using factorial design.
 * Copyright (C) "2016"  Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package incubator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import main.OpenBisClient;
import main.ProjectwizardUI;
import model.ExperimentBean;
import model.NewSampleModelBean;

import org.vaadin.teemu.wizards.WizardStep;

import uicomponents.ProjectInformationComponent;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import componentwrappers.CustomVisibilityComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.CompositeValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.ValoTheme;

import control.ProjectNameValidator;

/**
 * Wizard Step to set the Context of the new experiment and sample creation
 * 
 * @author Andreas Friedrich
 * 
 */
public class ProjectContextStep implements WizardStep {

  private VerticalLayout main;
  private ComboBox spaceCode;
  private ProjectInformationComponent projectInfoComponent;

  List<ExperimentBean> experiments;

  private Table experimentTable;
  private Table samples;

  private OpenBisClient openbis;
  private NewAggregator aggregator;
  private List<String> designExperimentTypes = new ArrayList<String>(Arrays.asList(
      "Q_EXPERIMENTAL_DESIGN", "Q_SAMPLE_EXTRACTION", "Q_SAMPLE_PREPARATION"));

  List<String> contextOptions = new ArrayList<String>(Arrays.asList("Add a new experiment",
      "Add sample extraction to existing sample sources",
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
  public ProjectContextStep(OpenBisClient openbis, List<String> openbisSpaces,
      Set<String> investigators) {
    this.openbis = openbis;
    main = new VerticalLayout();
    main.setMargin(true);
    main.setSpacing(true);
    main.setSizeUndefined();
    Collections.sort(openbisSpaces);
    spaceCode = new ComboBox("Project Name", openbisSpaces);
    spaceCode.setStyleName(ProjectwizardUI.boxTheme);
    spaceCode.setNullSelectionAllowed(false);
    spaceCode.setImmediate(true);

    projectInfoComponent = new ProjectInformationComponent(investigators);
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

    main.addComponent(grid);

    initListeners();
  }

  private String generateProjectCode() {
    Random r = new Random();
    String res = "";
    while (res.length() < 5 || openbis.getProjectByCode(res) != null) {
      res = "Q";
      for (int i = 1; i < 5; i++) {
        char c = 'Y';
        while (c == 'Y' || c == 'Z')
          c = (char) (r.nextInt(26) + 'A');
        res += c;
      }
    }
    return res;
  }

  private void initListeners() {

    TextField f = getProjectCodeField();
    CompositeValidator vd = new CompositeValidator();
    RegexpValidator p =
        new RegexpValidator("Q[A-Xa-x0-9]{4}",
            "Project must have length of 5, start with Q and not contain Y or Z");
    vd.addValidator(p);
    vd.addValidator(new ProjectNameValidator(openbis));
    f.addValidator(vd);
    f.setImmediate(true);
    f.setValidationVisible(true);

    projectInfoComponent.getCodeButton().addClickListener(new Button.ClickListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 7932750235689517217L;

      @Override
      public void buttonClick(ClickEvent event) {
        makeContextVisible();
      }
    });


    /**
     * Space selection listener
     */
    ValueChangeListener spaceSelectListener = new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = -7487587994432604593L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        spaceCode.removeStyleName(ValoTheme.LABEL_SUCCESS);
        resetProjects();
        if (getSpaceCode() != null) {
          spaceCode.addStyleName(ValoTheme.LABEL_SUCCESS);
          List<String> projects = new ArrayList<String>();
          for (Project p : openbis.getProjectsOfSpace(getSpaceCode())) {
            projects.add(p.getCode());
          }
          setProjectCodes(projects);
          enableNewContextOption(true);
        }
      }

    };
    spaceCode.addValueChangeListener(spaceSelectListener);

    /**
     * Project selection listener
     */

    ValueChangeListener projectSelectListener = new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = -443162343850159312L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        tryEnableCustomProject(generateProjectCode());
        resetExperiments();
        String project = getProjectCode();
        boolean hasBioEntities = projectHasBioEntities(getSpaceCode(), project);
        boolean hasExtracts = projectHasExtracts(getSpaceCode(), project);
        enableExtractContextOption(hasBioEntities);
        enableMeasureContextOption(hasExtracts);
        enableTSVWriteContextOption(hasBioEntities);
        if (project != null && !project.isEmpty()) {
          makeContextVisible();
          List<ExperimentBean> beans = new ArrayList<ExperimentBean>();
          for (Experiment e : openbis.getExperimentsOfProjectByCode(project)) {
            if (designExperimentTypes.contains(e.getExperimentTypeCode())) {
              int numOfSamples = openbis.getSamplesofExperiment(e.getIdentifier()).size();
              beans.add(new ExperimentBean(e.getIdentifier(), e.getExperimentTypeCode(), Integer
                  .toString(numOfSamples)));
            }
          }
          setExperiments(beans);
        }
      }

    };
    getProjectBox().addValueChangeListener(projectSelectListener);

    /**
     * Experiment selection listener
     */

    ValueChangeListener expSelectListener = new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 1931780520075315462L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        resetSamples();
        ExperimentBean exp = getExperimentName();
        if (exp != null) {
          List<NewSampleModelBean> beans = new ArrayList<NewSampleModelBean>();
          for (Sample s : openbis.getSamplesofExperiment(exp.getID())) {
            beans.add(new NewSampleModelBean(s.getCode(),
                s.getProperties().get("Q_SECONDARY_NAME"), s.getSampleTypeCode()));
          }
          setSamples(beans);
        }
      }

    };
    getExperimentTable().addValueChangeListener(expSelectListener);

    FocusListener fListener = new FocusListener() {
      private static final long serialVersionUID = 8721337946386845992L;

      @Override
      public void focus(FocusEvent event) {
        TextField p = projectInfoComponent.getProjectField();
        if (!p.isValid() || p.isEmpty()) {
          projectInfoComponent.tryEnableCustomProject(generateProjectCode());
        }
        makeContextVisible();
      }
    };
    projectInfoComponent.getProjectField().addFocusListener(fListener);

    Button.ClickListener projCL = new Button.ClickListener() {

      /**
       * 
       */
      private static final long serialVersionUID = -6646294420820222646L;

      @Override
      public void buttonClick(ClickEvent event) {
        projectInfoComponent.tryEnableCustomProject(generateProjectCode());
      }
    };
    projectInfoComponent.getProjectReloadButton().addClickListener(projCL);
  }

  /**
   * Test if a project has biological entities registered. Used to know availability of context
   * options
   * 
   * @param spaceCode Code of the selected openBIS space
   * @param code Code of the project
   * @return
   */
  private boolean projectHasBioEntities(String spaceCode, String code) {
    if (!openbis.projectExists(spaceCode, code))
      return false;
    for (Experiment e : openbis.getExperimentsOfProjectByCode(code)) {
      if (e.getExperimentTypeCode().equals("Q_EXPERIMENTAL_DESIGN"))
        return openbis.getSamplesofExperiment(e.getIdentifier()).size() > 0;
    }
    return false;
  }

  /**
   * Test is a project has biological extracts registered. Used to know availability of context
   * options
   * 
   * @param spaceCode Code of the selected openBIS space
   * @param code Code of the project
   * @return
   */
  private boolean projectHasExtracts(String spaceCode, String code) {
    if (!openbis.projectExists(spaceCode, code))
      return false;
    for (Experiment e : openbis.getExperimentsOfProjectByCode(code)) {
      if (e.getExperimentTypeCode().equals("Q_SAMPLE_EXTRACTION"))
        if (openbis.getSamplesofExperiment(e.getIdentifier()).size() > 0)
          return true;
    }
    return false;
  }

  public List<ExperimentBean> getExperiments() {
    return experiments;
  }

  public void setProjectCodes(List<String> projects) {
    projectInfoComponent.addProjects(projects);
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

  private void saveProjectInfo(NewProjectInfo projectInfo) {
    aggregator.setProjectInfo(projectInfo);
  }

  @Override
  public boolean onAdvance() {
    Notification n;
    if (spaceReady() && projectReady()) {
      if (inherit() || copy() || readOnly())
        // inherit, copy or read experiment
        if (expSelected()) {
          saveProjectInfo(new NewProjectInfo(getSpaceCode(), getProjectCode()));
          return true;
        } else {
          n = new Notification("Please select an existing experiment.");
        }
      else {
        if (getProjectBox().isEmpty())
          if (descriptionReady()) {
            // completely new sub-project
            saveProjectInfo(new NewProjectInfo(getSpaceCode(), getProjectCode(), getDescription(),
                getExpSecondaryName(), getPrincipalInvestigator()));
            return true;
          } else
            n = new Notification("Please fill in a description.");
        else {
          // new experiment but same sub-project
          saveProjectInfo(new NewProjectInfo(getSpaceCode(), getProjectCode()));
          return true;
        }
      }
    } else {
      n = new Notification("Please select a project and subproject or create a new one.");
    }
    n.setStyleName(ValoTheme.NOTIFICATION_CLOSABLE);
    n.setDelayMsec(-1);
    n.show(UI.getCurrent().getPage());
    return false;
  }

  private String getPrincipalInvestigator() {
    return projectInfoComponent.getInvestigator();
  }

  private boolean descriptionReady() {
    return getDescription() != null && !getDescription().isEmpty();
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

  public boolean fetchTSVModeSet() {
    String context = (String) projectContext.getValue();
    return contextOptions.get(3).equals(context);
  }

  public boolean expSecondaryNameSet() {
    TextField expName = projectInfoComponent.getExpNameField();
    return expName != null && !expName.isEmpty();
  }

  public String getDescription() {
    return projectInfoComponent.getProjectDescription();
  }

  public String getExpSecondaryName() {
    return projectInfoComponent.getExpNameField().getValue();
  }

  public void tryEnableCustomProject(String code) {
    projectInfoComponent.tryEnableCustomProject(code);
  }

  public void makeContextVisible() {
    projectContext.setVisible(true);
  }

}
