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
package uicomponents;


import java.util.List;
import java.util.Set;

import uicomponents.Styles;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
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
  private CustomVisibilityComponent projectBox;
  private TextField project;
  private Button reloadProjects;
  private TextField expName;
  private CustomVisibilityComponent personBox;
  private ComboBox piBox;
  private ComboBox contactBox;
  private ComboBox managerBox;
  private Button reloadPeople;

  TextArea description;

  ValueChangeListener projectSelectListener;

  public ProjectInformationComponent(Set<String> people) {
    setSpacing(true);
    ComboBox prBox = new ComboBox("Sub-Project");
    prBox.setStyleName(Styles.boxTheme);
    projectBox = new CustomVisibilityComponent(prBox);
    projectBox.setStyleName(Styles.boxTheme);
    projectBox.setImmediate(true);
    addComponent(Styles.questionize(projectBox, "QBiC 5 letter project code", "Project"));

    project = new StandardTextField();
    project.setStyleName(Styles.fieldTheme);
    project.setMaxLength(5);
    project.setWidth("90px");
    project.setEnabled(false);
    project.setValidationVisible(true);

    reloadProjects = new Button();
    Styles.iconButton(reloadProjects, FontAwesome.REFRESH);

    HorizontalLayout proj = new HorizontalLayout();
    proj.setCaption("New Sub-Project");
    proj.addComponent(project);
    proj.addComponent(reloadProjects);
    CustomVisibilityComponent newProj = new CustomVisibilityComponent(proj);

    addComponent(Styles.questionize(newProj,
        "Automatically create an unused QBiC project code or fill in your own. "
            + "The code consists of 5 characters, must start with Q and not contain Y or Z. You can create a random unused code by clicking "
            + FontAwesome.REFRESH.getHtml() + ".",
        "New Sub-Project"));
    expName = new StandardTextField("Short name");
    expName.setWidth("200px");
    // expName.setRequired(true);
    expName.setVisible(false);
    expName.setInputPrompt("Name of sub project");
    addComponent(expName);

    HorizontalLayout persBoxH = new HorizontalLayout();
    persBoxH.setCaption("Principal Investigator");
    VerticalLayout persBox = new VerticalLayout();

    piBox = new ComboBox();
    piBox.addItems(people);
    piBox.setFilteringMode(FilteringMode.CONTAINS);
    piBox.setStyleName(Styles.boxTheme);
    contactBox = new ComboBox("Contact Person", people);
    contactBox.setFilteringMode(FilteringMode.CONTAINS);
    contactBox.setStyleName(Styles.boxTheme);
    managerBox = new ComboBox("Project Manager", people);
    managerBox.setFilteringMode(FilteringMode.CONTAINS);
    managerBox.setStyleName(Styles.boxTheme);
    persBox.addComponent(piBox);
    persBox.addComponent(contactBox);
    persBox.addComponent(managerBox);

    reloadPeople = new Button();
    Styles.iconButton(reloadPeople, FontAwesome.REFRESH);
    persBoxH.addComponent(persBox);
    persBoxH.addComponent(reloadPeople);

    personBox = new CustomVisibilityComponent(persBoxH);
    personBox.setVisible(false);
    addComponent(Styles.questionize(personBox,
        "Investigator and contact person of this project. Please contact us if additional people need to be added. Press refresh button to show newly added people.",
        "Contacts"));

    description = new TextArea("Description");
    description.setRequired(true);
    description.setStyleName(Styles.fieldTheme);
    description.setInputPrompt("Sub-Project description, maximum of 2000 symbols.");
    description.setWidth("100%");
    description.setHeight("110px");
    description.setVisible(false);
    StringLengthValidator lv = new StringLengthValidator(
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
    personBox.setVisible(choseNewProject);
  }

  public void updatePeople(Set<String> people) {
    String pi = getInvestigator();
    String contact = getContactPerson();
    String manager = getProjectManager();
    piBox.removeAllItems();
    contactBox.removeAllItems();
    managerBox.removeAllItems();
    piBox.addItems(people);
    contactBox.addItems(people);
    managerBox.addItems(people);
    if (pi != null && !pi.isEmpty())
      piBox.select(pi);
    if (contact != null && !contact.isEmpty())
      contactBox.select(contact);
    if (manager != null && !manager.isEmpty())
      managerBox.select(manager);
  }

  private boolean selectionNull() {
    return projectBox.getValue() == null;
  }

  public Button getCodeButton() {
    return reloadProjects;
  }

  public ComboBox getProjectBox() {
    return (ComboBox) projectBox.getInnerComponent();
  }

  public TextField getProjectField() {
    return project;
  }

  public Button getProjectReloadButton() {
    return reloadProjects;
  }

  public Button getPeopleReloadButton() {
    return reloadPeople;
  }

  public String getSelectedProject() {
    if (selectionNull())
      if (project.isValid())
        return project.getValue();
      else
        return "";
    else {
      String project = projectBox.getValue().toString();
      if (project.contains(" "))
        // remove alternative name
        project = project.split(" ")[0];
      return project;
    }
  }

  public String getProjectDescription() {
    return description.getValue();
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

  public String getInvestigator() {
    return (String) piBox.getValue();
  }

  public String getContactPerson() {
    return (String) contactBox.getValue();
  }

  public String getProjectManager() {
    return (String) managerBox.getValue();
  }

}
