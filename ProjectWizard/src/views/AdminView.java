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
package views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import logging.Log4j2Logger;
import main.OpenBisClient;
import main.OpenbisCreationController;
import main.ProjectwizardUI;
import model.OpenbisSpaceUserRole;

import adminviews.MCCView;

import com.vaadin.data.validator.CompositeValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.ValoTheme;

import componentwrappers.StandardTextField;
import control.Functions;
import control.Functions.NotificationType;
import control.ProjectNameValidator;

public class AdminView extends VerticalLayout {

  /**
   * 
   */
  private static final long serialVersionUID = -1713715806593305379L;

  OpenBisClient openbis;
  OpenbisCreationController registrator;
  String user;

  private TabSheet tabs;
  // space
  private TextField space;
  private TextArea users;
  private Button createSpace;
  private Button createProject;
  private Button reloadProject;
  private ComboBox spaceBox;
  private TextField projectCode;
  private TextArea projectDescription;
  private TextField projectName;
  // mcc patients
  private MCCView addMultiScale;

  // edit data

  // upload metainfo
  private MetadataUploadView metadataUpload;

  // logger
  logging.Logger logger = new Log4j2Logger(AdminView.class);

  public AdminView(OpenBisClient openbis, List<String> spaces,
      OpenbisCreationController creationController, String user) {
    this.user = user;
    this.registrator = creationController;
    this.openbis = openbis;
    tabs = new TabSheet();
    tabs.setStyleName(ValoTheme.TABSHEET_FRAMED);

    VerticalLayout spaceView = new VerticalLayout();
    spaceView.setSpacing(true);
    spaceView.setMargin(true);
    space = new TextField("Space");
    users = new TextArea("Users");
    users.setWidth("80px");
    users.setHeight("80px");
    createSpace = new Button("Create Space");
    spaceView.addComponent(space);
    spaceView.addComponent(users);
    spaceView.addComponent(createSpace);
    tabs.addTab(spaceView, "Create Space");
    // tabs.getTab(spaceView).setEnabled(false);

    // ADD PROJECT
    VerticalLayout projectView = new VerticalLayout();
    projectView.setSpacing(true);
    projectView.setMargin(true);

    projectCode = new StandardTextField();
    projectCode.setStyleName(ProjectwizardUI.fieldTheme);
    projectCode.setMaxLength(5);
    projectCode.setWidth("90px");
    projectCode.setValidationVisible(true);
    CompositeValidator vd = new CompositeValidator();
    RegexpValidator p = new RegexpValidator("Q[A-Xa-x0-9]{4}",
        "Project must have length of 5, start with Q and not contain Y or Z");
    vd.addValidator(p);
    vd.addValidator(new ProjectNameValidator(openbis));
    projectCode.addValidator(vd);
    projectCode.setImmediate(true);

    reloadProject = new Button();
    ProjectwizardUI.iconButton(reloadProject, FontAwesome.REFRESH);

    HorizontalLayout proj = new HorizontalLayout();
    proj.setCaption("Project Code");
    proj.addComponent(projectCode);
    proj.addComponent(reloadProject);

    projectName = new StandardTextField("Project Name");
    projectName.setStyleName(ProjectwizardUI.fieldTheme);

    projectDescription = new TextArea("Description");

    spaceBox = new ComboBox("Space");
    spaceBox.addItems(spaces);
    spaceBox.setNullSelectionAllowed(false);
    spaceBox.setStyleName(ProjectwizardUI.boxTheme);

    createProject = new Button("Create Project");

    projectView.addComponent(spaceBox);
    projectView.addComponent(proj);
    projectView.addComponent(projectName);
    projectView.addComponent(projectDescription);
    projectView.addComponent(createProject);
//    tabs.addTab(projectView, "Create Project");

    // METADATA
    metadataUpload = new MetadataUploadView(openbis);
    tabs.addTab(metadataUpload, "Update Metadata");

    // MULTISCALE
    addMultiScale = new MCCView(openbis, creationController, user);
    addMultiScale.setSpacing(true);
    addMultiScale.setMargin(true);

    tabs.addTab(addMultiScale, "Add Multiscale Samples");

    addComponent(tabs);

    initButtons();
  }

  private void initButtons() {
    createSpace.addClickListener(new Button.ClickListener() {

      /**
       * 
       */
      private static final long serialVersionUID = -6870391592753359641L;

      @Override
      public void buttonClick(ClickEvent event) {
        if (!openbis.spaceExists(getSpace())) {
          HashMap<OpenbisSpaceUserRole, ArrayList<String>> roleInfos =
              new HashMap<OpenbisSpaceUserRole, ArrayList<String>>();
          if (getUsers().size() > 0)
            roleInfos.put(OpenbisSpaceUserRole.USER, getUsers());
          registrator.registerSpace(getSpace(), roleInfos, user);
        }
      }
    });

    createProject.addClickListener(new Button.ClickListener() {

      /**
       * 
       */
      private static final long serialVersionUID = -6870391514753359641L;

      @Override
      public void buttonClick(ClickEvent event) {
        String space = (String) spaceBox.getValue();
        if (canRegisterProject()) {
          String code = projectCode.getValue();
          registrator.registerProject(space, code, projectDescription.getValue(),
              user);
//          Map<String, Object> metadata = new HashMap<String, Object>();
//          if (projectName != null && !projectName.isEmpty()) {
//            metadata.put("Q_SECONDARY_NAME", projectName.getValue());
//            registrator.registerExperiment(space, code, "Q_EXPERIMENTAL_DESIGN", code + "E1",
//                metadata, user);// TODO we might want to change E1 here to E01 at some point
//          }
          projectCode.setValue("");
          projectName.setValue("");
          projectDescription.setValue("");
          Functions.notification("Success", "Project was registered!", NotificationType.SUCCESS);
        } else {
          Functions.notification("Missing data",
              "You have to select a space and fill in Description and Project Code",
              NotificationType.ERROR);
        }
      }
    });

    reloadProject.addClickListener(new Button.ClickListener() {

      /**
       * 
       */
      private static final long serialVersionUID = -6646294420820222646L;

      @Override
      public void buttonClick(ClickEvent event) {
        projectCode.setValue(generateProjectCode());
      }
    });
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

  private boolean canRegisterProject() {
    return !spaceBox.isEmpty() && !projectDescription.isEmpty() && !projectCode.isEmpty();
  }

  public String getSpace() {
    return space.getValue();
  }

  public ArrayList<String> getUsers() {
    if (!users.getValue().trim().equals(""))
      return new ArrayList<String>(Arrays.asList(users.getValue().split("\n")));
    else
      return new ArrayList<String>();
  }

  public Button getCreateSpace() {
    return createSpace;
  }

}
