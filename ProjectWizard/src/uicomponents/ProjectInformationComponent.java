package uicomponents;


import java.util.List;
import java.util.Set;

import main.ProjectwizardUI;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import componentwrappers.CustomVisibilityComponent;
import componentwrappers.StandardTextField;

public class ProjectInformationComponent extends VerticalLayout {

  /**
   * 
   */
  private static final long serialVersionUID = 3467663055161160735L;
  CustomVisibilityComponent projectBox;
  TextField project;
  Button reload;
  TextField expName;
  CustomVisibilityComponent investigatorBox;
  TextArea description;

  ValueChangeListener projectSelectListener;

  public ProjectInformationComponent(Set<String> set) {
    setSpacing(true);
    ComboBox prBox = new ComboBox("Sub-Projects");
    prBox.setStyleName(ProjectwizardUI.boxTheme);
    projectBox = new CustomVisibilityComponent(prBox);
    projectBox.setStyleName(ProjectwizardUI.boxTheme);
    projectBox.setImmediate(true);
    addComponent(ProjectwizardUI.questionize(projectBox, "QBiC 5 letter project code", "Project"));

    project = new StandardTextField();
    project.setStyleName(ProjectwizardUI.fieldTheme);
    project.setMaxLength(5);
    project.setWidth("90px");
    project.setEnabled(false);
    project.setValidationVisible(true);

    reload = new Button();
    ProjectwizardUI.iconButton(reload, FontAwesome.REFRESH);

    HorizontalLayout proj = new HorizontalLayout();
    proj.setCaption("New Sub-Project");
    proj.addComponent(project);
    proj.addComponent(reload);
    CustomVisibilityComponent newProj = new CustomVisibilityComponent(proj);

    addComponent(ProjectwizardUI
        .questionize(
            newProj,
            "Automatically create an unused QBiC project code or fill in your own. "
                + "The code consists of 5 characters, must start with Q and not contain Y or Z. You can create a new code by clicking "
                + FontAwesome.REFRESH.getHtml() + ".", "New Sub-Project"));
    expName = new StandardTextField("Sub-Project name");
    expName.setVisible(false);
    expName.setInputPrompt("Optional short name");
    addComponent(expName);

    ComboBox piBox = new ComboBox("Principal Investigator", set);
    piBox.setStyleName(ProjectwizardUI.boxTheme);
    investigatorBox = new CustomVisibilityComponent(piBox);
    investigatorBox.setStyleName(ProjectwizardUI.boxTheme);
    investigatorBox.setImmediate(true);
    investigatorBox.setVisible(false);
    addComponent(ProjectwizardUI
        .questionize(
            investigatorBox,
            "The principal investigator of this project. Please contact us if additional people need to be added.",
            "Investigator"));

    description = new TextArea("Description");
    description.setRequired(true);
    description.setStyleName(ProjectwizardUI.fieldTheme);
    description.setInputPrompt("Sub-Project description, maximum of 2000 symbols.");
    description.setWidth("100%");
    description.setHeight("110px");
    description.setVisible(false);
    StringLengthValidator lv =
        new StringLengthValidator(
            "Description is only allowed to contain a maximum of 2000 letters.", 0, 2000, true);
    description.addValidator(lv);
    description.setImmediate(true);
    description.setValidationVisible(true);
    addComponent(description);
  }

  public void tryEnableCustomProject(String code) {
    boolean choseNewProject = selectionNull();
    if (choseNewProject) {
      project.setValue(code);
    } else {
      project.setValue("");
    }
    project.setEnabled(choseNewProject);
    expName.setVisible(choseNewProject);
    description.setVisible(choseNewProject);
    investigatorBox.setVisible(choseNewProject);
  }

  private boolean selectionNull() {
    return projectBox.getValue() == null;
  }

  public Button getCodeButton() {
    return reload;
  }

  public ComboBox getProjectBox() {
    return (ComboBox) projectBox.getInnerComponent();
  }

  public TextField getProjectField() {
    return project;
  }

  public Button getProjectReloadButton() {
    return reload;
  }

  public String getSelectedProject() {
    if (selectionNull())
      if (project.isValid())
        return project.getValue();
      else
        return "";
    else
      return projectBox.getValue().toString();
  }

  public String getProjectDescription() {
    return description.getValue();
  }

  public String getInvestigator() {
    return investigatorBox.getValue();
  }

  public String getSecondaryName() {
    return expName.getValue();
  }

  public void addProjects(List<String> projects) {
    ((AbstractSelect) projectBox.getInnerComponent()).addItems(projects);
  }

  public void resetProjects() {
    projectBox.setEnabled(false);
    ((ComboBox) projectBox.getInnerComponent()).removeAllItems();
    project.setEnabled(true);
  }

  public void enableProjectBox(boolean b) {
    projectBox.setEnabled(b);
  }

  public TextField getExpNameField() {
    return expName;
  }

}
