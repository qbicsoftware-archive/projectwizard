package main;

import io.ConfigurationManager;
import io.ConfigurationManagerFactory;
import io.DBConfig;
import io.DBManager;
import io.DBVocabularies;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import logging.Log4j2Logger;
import model.AttachmentConfig;

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

import control.BarcodeController;
import control.ExperimentImportController;
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
  private String version = "Version 0.986, 11.01.16";

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
  public static String MSLabelingMethods;
  private String dataSourceUser;
  private String dataSourcePass;
  private String dataSourceURL;

  private String mysqlHost;
  private String mysqlPort;
  private String mysqlDB;
  private String mysqlUser;
  private String mysqlPass;

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
    String userID = "";
    boolean success = true;
    if (LiferayAndVaadinUtils.isLiferayPortlet()) {
      logger.info("Wizard is running on Liferay and user is logged in.");
      userID = LiferayAndVaadinUtils.getUser().getScreenName();
    } else {
      if (isDevelopment()) {
        logger.warn("Checks for local dev version successful. User is granted admin status.");
        userID = "admin";
        isAdmin = true;
      } else {
        success = false;
        logger.info("User \""+userID+"\" not found. Probably running on Liferay and not logged in.");
        layout.addComponent(new Label("User not found. Are you logged in?"));
      }
    }
    // establish connection to the OpenBIS API
    try {
      this.openbis = new OpenBisClient(dataSourceUser, dataSourcePass, dataSourceURL);
      this.openbis.login();
    } catch (Exception e) {
      success = false;
      logger.error("User \""+userID+"\" could not connect to openBIS and has been informed of this.");
      layout.addComponent(new Label(
          "Data Management System could not be reached. Please try again later or contact us."));
    }
    if (success) {
      // stuff from openbis
      Map<String, String> taxMap = openbis.getVocabCodesAndLabelsForVocab("Q_NCBI_TAXONOMY");
      Map<String, String> tissueMap = openbis.getVocabCodesAndLabelsForVocab("Q_PRIMARY_TISSUES");
      Map<String, String> deviceMap = openbis.getVocabCodesAndLabelsForVocab("Q_MS_DEVICES");
      Map<String, String> cellLinesMap = openbis.getVocabCodesAndLabelsForVocab("Q_CELL_LINES");
      List<String> sampleTypes = openbis.getVocabCodesForVocab("Q_SAMPLE_TYPES");
      List<String> enzymes = openbis.getVocabCodesForVocab("Q_DIGESTION_PROTEASES");
      List<String> msProtocols = openbis.getVocabCodesForVocab("Q_MS_PROTOCOLS");
      List<String> lcmsMethods = openbis.getVocabCodesForVocab("Q_MS_LCMS_METHODS");
      List<String> chromTypes = openbis.getVocabCodesForVocab("Q_CHROMATOGRAPHY_TYPES");
      final List<String> spaces = getUserSpaces(userID);
      // stuff from mysql database
      DBConfig mysqlConfig = new DBConfig(mysqlHost, mysqlPort, mysqlDB, mysqlUser, mysqlPass);
      DBManager dbm = new DBManager(mysqlConfig);
      Map<String, Integer> map = new HashMap<String, Integer>();
      try {
        map = dbm.getPrincipalInvestigatorsWithIDs();
      } catch (NullPointerException e) {
        map.put("No Connection", -1);
      }
      // hardcoded stuff (experiment types mainly used in the wizard)
      List<String> expTypes =
          new ArrayList<String>(Arrays.asList("Q_EXPERIMENTAL_DESIGN", "Q_SAMPLE_EXTRACTION",
              "Q_SAMPLE_PREPARATION"));

      DBVocabularies vocabs =
          new DBVocabularies(taxMap, tissueMap, cellLinesMap, sampleTypes, spaces, map, expTypes,
              enzymes, deviceMap, msProtocols, lcmsMethods, chromTypes);
      // initialize the View with sample types, spaces and the dictionaries of tissues and species
      initView(dbm, vocabs, userID);
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

  boolean isDevelopment() {
    boolean devEnv = false;
    try {
      // TODO tests if this is somehow a local development environment
      // in which case user is granted admin rights. Change so it works for you.
      // Be careful that this is never true on production or better yet that logged out users can
      // not see the portlet page.
      devEnv = new File(".").getCanonicalPath().contains("eclipse");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return devEnv;
  }

  private void initView(final DBManager dbm, final DBVocabularies vocabularies, final String user) {
    tabs.removeAllComponents();
    AttachmentConfig attachConfig =
        new AttachmentConfig(Integer.parseInt(attachmentSize), attachmentURI, attachmentUser,
            attachmentPass);
    WizardController c = new WizardController(openbis, dbm, vocabularies, attachConfig);
    c.init(user);
    Wizard w = c.getWizard();
    WizardProgressListener wl = new WizardProgressListener() {

      @Override
      public void activeStepChanged(WizardStepActivationEvent event) {}

      @Override
      public void stepSetChanged(WizardStepSetChangedEvent event) {}

      @Override
      public void wizardCompleted(WizardCompletedEvent event) {
        initView(dbm, vocabularies, user);
      }

      @Override
      public void wizardCancelled(WizardCancelledEvent event) {
        initView(dbm, vocabularies, user);
      }

    };
    w.addListener(wl);
    VerticalLayout wLayout = new VerticalLayout();
    wLayout.addComponent(w);
    wLayout.setMargin(true);

    tabs.addTab(wLayout, "Create Project").setIcon(FontAwesome.FLASK);
    final WizardBarcodeView bw = new WizardBarcodeView(vocabularies.getSpaces());
    BarcodeController bc = new BarcodeController(bw, openbis, barcodeScripts, pathVar);
    bc.init();
    tabs.addTab(bw, "Create Barcodes").setIcon(FontAwesome.BARCODE);
    StandaloneTSVImport tsvImport = new StandaloneTSVImport();

    OpenbisCreationController creationController = new OpenbisCreationController(openbis);

    ExperimentImportController uc = new ExperimentImportController(tsvImport, creationController);
    uc.init(user);
    tabs.addTab(tsvImport, "Import Project").setIcon(FontAwesome.FILE);
    if (isAdmin) {
      logger.info("User is " + user + " and can see admin panel.");
      VerticalLayout padding = new VerticalLayout();
      padding.setMargin(true);
      padding.addComponent(new AdminView(openbis, vocabularies.getSpaces(), creationController,
          user));
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
    ConfigurationManager c = ConfigurationManagerFactory.getInstance();

    tmpFolder = c.getTmpFolder();
    dataSourceUser = c.getDataSourceUser();
    dataSourcePass = c.getDataSourcePassword();
    dataSourceURL = c.getDataSourceUrl();

    barcodeScripts = c.getBarcodeScriptsFolder();
    pathVar = c.getBarcodePathVariable();

    mysqlHost = c.getMysqlHost();
    mysqlDB = c.getMysqlDB();
    mysqlPort = c.getMysqlPort();
    mysqlUser = c.getMysqlUser();
    mysqlPass = c.getMysqlPass();

    attachmentSize = c.getAttachmentMaxSize();
    attachmentURI = c.getAttachmentURI();
    attachmentUser = c.getAttachmentUser();
    attachmentPass = c.getAttachmenPassword();

    MSLabelingMethods = c.getVocabularyMSLabeling();
  }

}
