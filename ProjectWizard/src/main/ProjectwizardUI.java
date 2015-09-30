package main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import logging.Log4j2Logger;

import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;

import views.AdminView;
import views.StandaloneTSVImport;
import views.WizardBarcodeView;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.themes.ValoTheme;
import componentwrappers.CustomVisibilityComponent;

import control.AttachmentConfig;
import control.BarcodeController;
import control.UploadController;
import control.VisibilityChangeListener;
import control.WizardController;
import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;

@SuppressWarnings("serial")
@Theme("projectwizard")
public class ProjectwizardUI extends UI {

  @WebServlet(value = "/*", asyncSupported = true)
  @VaadinServletConfiguration(productionMode = true, ui = ProjectwizardUI.class,
      widgetset = "main.widgetset.ProjectwizardWidgetset")
  public static class Servlet extends VaadinServlet {
  }

  logging.Logger logger = new Log4j2Logger(ProjectwizardUI.class);
  private String version = "Version 0.9692, 29.09.15";

  private String DATASOURCE_USER = "datasource.user";
  private String DATASOURCE_PASS = "datasource.password";
  private String DATASOURCE_URL = "datasource.url";
  private String TMP_FOLDER = "tmp.folder";
  private String BARCODE_SCRIPTS = "barcode.scripts";
  private String PATH_VARIABLE = "path.variable";
  private String MAX_ATTACHMENT_SIZE = "max.upload.size";
  private String ATTACHMENT_URI = "attachment.uri";
  private String ATTACHMENT_USER = "attachment.user";
  private String ATTACHMENT_PASS = "attachment.password";

  public static String boxTheme = ValoTheme.COMBOBOX_SMALL;
  public static String fieldTheme = ValoTheme.TEXTFIELD_SMALL;
  public static String areaTheme = ValoTheme.TEXTAREA_SMALL;
  public static String tableTheme = ValoTheme.TABLE_SMALL;

  public static void iconButton(Button b, Resource icon) {
    b.setStyleName(ValoTheme.BUTTON_BORDERLESS);
    b.setIcon(icon);
    b.setWidth("10px");
  }

  public static HorizontalLayout questionize(Component c, final String info, final String header) {
    final HorizontalLayout res = new HorizontalLayout();
    res.setSpacing(true);
    if (c instanceof CustomVisibilityComponent) {
      CustomVisibilityComponent custom = (CustomVisibilityComponent) c;
      c = custom.getInnerComponent();
      custom.addListener(new VisibilityChangeListener() {

        @Override
        public void setVisible(boolean b) {
          res.setVisible(b);
        }
      });
    }

    res.setVisible(c.isVisible());
    res.setCaption(c.getCaption());
    c.setCaption(null);
    res.addComponent(c);

    PopupView pv = new PopupView(new Content() {

      @Override
      public Component getPopupComponent() {
        Label l = new Label(info, ContentMode.HTML);
        l.setCaption(header);
        l.setIcon(FontAwesome.INFO);
        l.setWidth("250px");
        l.addStyleName("info");
        return new VerticalLayout(l);
      }

      @Override
      public String getMinimizedValueAsHTML() {
        return "[?]";
      }
    });
    pv.setHideOnMouseOut(false);

    res.addComponent(pv);

    return res;
  }

  public static String tmpFolder;
  private String dataSourceUser;
  private String dataSourcePass;
  private String dataSourceURL;
  private String barcodeScripts;
  private String pathVar;
  private String attachmentSize;
  private String attachmentURI;
  private String attachmentUser;
  private String attachmentPass;

  OpenBisClient openbis;

  private final TabSheet tabs = new TabSheet();
  private boolean isAdmin = false;

  @Override
  protected void init(VaadinRequest request) {
    tabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
    VerticalLayout layout = new VerticalLayout();
    // read in the configuration file
    readConfig();
    layout.setMargin(true);
    setContent(layout);
    String userID = "admin";
    boolean success = true;
    if (LiferayAndVaadinUtils.isLiferayPortlet())
      try {
        userID = LiferayAndVaadinUtils.getUser().getScreenName();
      } catch (Exception e) {
        success = false;
        layout.addComponent(new Label("Unknown user. Are you logged in?"));
      }
    isAdmin = userID.equals("admin");
    // establish connection to the OpenBIS API
    try {
      this.openbis = new OpenBisClient(dataSourceUser, dataSourcePass, dataSourceURL);
      this.openbis.login();
    } catch (Exception e) {
      success = false;
      layout.addComponent(new Label(
          "Data Management System could not be reached. Please try again later or contact us."));
    }
    if (success) {
      // initialize the View with sample types, spaces and the dictionaries of tissues and species
      initView(openbis.getVocabCodesAndLabelsForVocab("Q_NCBI_TAXONOMY"),
          openbis.getVocabCodesAndLabelsForVocab("Q_PRIMARY_TISSUES"),
          openbis.getVocabCodesForVocab("Q_SAMPLE_TYPES"), userID);
      layout.addComponent(tabs);
    }

    if (LiferayAndVaadinUtils.isLiferayPortlet())
      try {
        for (com.liferay.portal.model.Role r : LiferayAndVaadinUtils.getUser().getRoles())
          if (r.getName().equals("Administrator")) {
            layout.addComponent(new Label(version));
            layout.addComponent(new Label("User: " + userID));
          }
      } catch (Exception e) {
        success = false;
        layout.addComponent(new Label("Unkown user. Are you logged in?"));
      }
    else {
      layout.addComponent(new Label(version));
      layout.addComponent(new Label("User: " + userID));
    }
  }

  private List<String> getUserSpaces(String userID) {
    List<String> userSpaces = new ArrayList<String>();
    for (SpaceWithProjectsAndRoleAssignments s : openbis.getFacade().getSpacesWithProjects()) {
      Set<Role> roles = s.getRoles(userID);
      if (!roles.isEmpty()) {
        userSpaces.add(s.getCode());
        if (!isAdmin) {
          for (Role r : roles)
            isAdmin = r.toString().equals("ADMIN(instance)");
        }
      }
    }
    return userSpaces;
  }

  private void initView(final Map<String, String> taxMap, final Map<String, String> tissueMap,
      final List<String> sampleTypes, final String user) {
    final List<String> spaces = getUserSpaces(user);
    tabs.removeAllComponents();
    AttachmentConfig attachConfig =
        new AttachmentConfig(Integer.parseInt(attachmentSize), attachmentURI, attachmentUser,
            attachmentPass);
    WizardController c =
        new WizardController(openbis, taxMap, tissueMap, sampleTypes, spaces, pathVar, attachConfig);
    c.init();
    Wizard w = c.getWizard();
    WizardProgressListener wl = new WizardProgressListener() {

      @Override
      public void activeStepChanged(WizardStepActivationEvent event) {}

      @Override
      public void stepSetChanged(WizardStepSetChangedEvent event) {}

      @Override
      public void wizardCompleted(WizardCompletedEvent event) {
        initView(taxMap, tissueMap, sampleTypes, user);
      }

      @Override
      public void wizardCancelled(WizardCancelledEvent event) {
        initView(taxMap, tissueMap, sampleTypes, user);
      }

    };
    w.addListener(wl);
    VerticalLayout wLayout = new VerticalLayout();
    wLayout.addComponent(w);
    wLayout.setMargin(true);
    tabs.addTab(wLayout, "Create Project").setIcon(FontAwesome.FLASK);
    final WizardBarcodeView bw = new WizardBarcodeView(spaces);
    BarcodeController bc = new BarcodeController(bw, openbis, barcodeScripts, pathVar);
    bc.init();
    tabs.addTab(bw, "Create Barcodes").setIcon(FontAwesome.BARCODE);
    StandaloneTSVImport tsvImport = new StandaloneTSVImport();

    OpenbisCreationController creationController = new OpenbisCreationController(openbis);

    UploadController uc = new UploadController(tsvImport, creationController);
    uc.init();
    tabs.addTab(tsvImport, "Import Project").setIcon(FontAwesome.FILE);
    if (isAdmin) {
      logger.debug("User is " + user + " and can see admin panel.");
      VerticalLayout padding = new VerticalLayout();
      padding.setMargin(true);
      padding.addComponent(new AdminView(openbis, creationController));
      tabs.addTab(padding, "Admin Functions").setIcon(FontAwesome.WRENCH);
    }
    tabs.addSelectedTabChangeListener(new SelectedTabChangeListener() {

      @Override
      public void selectedTabChange(SelectedTabChangeEvent event) {
        bw.resetSpace();
      }
    });
  }

  private void readConfig() {
    Properties config = new Properties();
    try {
      List<String> configs =
          new ArrayList<String>(Arrays.asList("/Users/frieda/Desktop/testing/portlet.properties",
              "/home/rayslife/portlet.properties", "/usr/local/share/guse/portlets.properties",
              "/home/tomcat-liferay/liferay_production/portlets.properties"));
      for (String s : configs) {
        File f = new File(s);
        if (f.exists())
          config.load(new FileReader(s));
      }
      StringWriter configDebug = new StringWriter();
      config.list(new PrintWriter(configDebug));
      tmpFolder = config.getProperty(TMP_FOLDER);
      dataSourceUser = config.getProperty(DATASOURCE_USER);
      dataSourcePass = config.getProperty(DATASOURCE_PASS);
      dataSourceURL = config.getProperty(DATASOURCE_URL);
      barcodeScripts = config.getProperty(BARCODE_SCRIPTS);
      pathVar = config.getProperty(PATH_VARIABLE);
      attachmentSize = config.getProperty(MAX_ATTACHMENT_SIZE);
      attachmentURI = config.getProperty(ATTACHMENT_URI);
      attachmentUser = config.getProperty(ATTACHMENT_USER);
      attachmentPass = config.getProperty(ATTACHMENT_PASS);
    } catch (IOException e) {
      System.err.println("Failed to load configuration: " + e);
    }
  }

}
