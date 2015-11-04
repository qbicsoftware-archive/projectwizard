package views;

import logging.Log4j2Logger;
import main.OpenBisClient;
import main.OpenbisCreationController;

import adminviews.MCCView;
import com.vaadin.ui.Button;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class AdminView extends VerticalLayout {

  /**
   * 
   */
  private static final long serialVersionUID = -1713715806593305379L;

  OpenBisClient openbis;

  private TabSheet tabs;
  // space
  private TextField space;
  private Button createSpace;
  // projects TODO add 
  private Button createProject;
  // mcc patients
  private MCCView addMultiScale;

  // edit data

  // upload metainfo
  private MetadataUploadView metadataUpload;
  
  // logger
  logging.Logger logger = new Log4j2Logger(AdminView.class);

  public AdminView(OpenBisClient openbis, OpenbisCreationController creationController, String user) {
    this.openbis = openbis;
    tabs = new TabSheet();
    tabs.setStyleName(ValoTheme.TABSHEET_FRAMED);

    VerticalLayout spaceView = new VerticalLayout();
    spaceView.setSpacing(true);
    spaceView.setMargin(true);
    space = new TextField("Space");
    createSpace = new Button("Create Space");
    spaceView.addComponent(space);
    spaceView.addComponent(createSpace);
    tabs.addTab(spaceView, "Create Space");
    tabs.getTab(spaceView).setEnabled(false);
    
    metadataUpload = new MetadataUploadView(openbis);
    tabs.addTab(metadataUpload, "Update Metadata");

    addMultiScale = new MCCView(openbis, creationController, user);
    addMultiScale.setSpacing(true);
    addMultiScale.setMargin(true);
    
    tabs.addTab(addMultiScale, "Add Multiscale Samples");

    addComponent(tabs);

  }

  public String getSpace() {
    return space.getValue();
  }

  public Button getCreateSpace() {
    return createSpace;
  }

}
