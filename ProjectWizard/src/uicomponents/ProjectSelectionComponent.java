package uicomponents;


import java.util.List;

import main.ProjectwizardUI;

import com.vaadin.data.Property.ValueChangeListener;
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

public class ProjectSelectionComponent extends VerticalLayout {

  /**
   * 
   */
  private static final long serialVersionUID = 3467663055161160735L;
  CustomVisibilityComponent projectBox;
  TextField project;
  Button reload;
  TextField expName;
  TextArea description;

  ValueChangeListener projectSelectListener;

  public ProjectSelectionComponent() {
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
    
    description = new TextArea("Description");
    description.setStyleName(ProjectwizardUI.fieldTheme);
    description.setInputPrompt("Optional project description");
    description.setVisible(false);
    // addComponent(secondaryName.getInnerComponent());
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

   public String getSecondaryName() {
   return expName.getValue();
   }

  public void addItems(List<String> projects) {
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
