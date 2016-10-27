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

import javax.servlet.annotation.WebServlet;

import logging.Log4j2Logger;
import model.AttachmentConfig;
import model.BarcodeConfig;

import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;

import views.AdminView;
import views.StandaloneTSVImport;
import views.WizardBarcodeView;

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
import control.SampleFilterGenerator;
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
  private String version = "Version 1.23, 27.10.16";

  public static String boxTheme = ValoTheme.COMBOBOX_SMALL;
  public static String fieldTheme = ValoTheme.TEXTFIELD_SMALL;
  public static String areaTheme = ValoTheme.TEXTAREA_SMALL;
  public static String tableTheme = ValoTheme.TABLE_SMALL;
  public static boolean testMode = false;
  // hardcoded stuff (experiment types mainly used in the wizard)
  List<String> expTypes = new ArrayList<String>(
      Arrays.asList("Q_EXPERIMENTAL_DESIGN", "Q_SAMPLE_EXTRACTION", "Q_SAMPLE_PREPARATION"));

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
        l.setWidth("350px");
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
  private String barcodeResultsFolder;
  private String pathVar;

  private String attachmentSize;
  private String attachmentURI;
  private String attachmentUser;
  private String attachmentPass;

  private IOpenBisClient openbis;

  private final TabSheet tabs = new TabSheet();
  private boolean isAdmin = false;

  // private HorizontalLayout getButtonBox() {
  // ComboBox box = new ComboBox("", new ArrayList<String>(Arrays.asList("1", "2", "3")));
  // box.setWidth("150px");
  // box.setStyleName(boxTheme);
  // HorizontalLayout complexComponent = new HorizontalLayout();
  // complexComponent.addComponent(box);
  // complexComponent.setWidth("300px");
  // Button copy = new Button();
  // copy.setIcon(FontAwesome.ARROW_CIRCLE_O_DOWN);
  // copy.setWidth("10px");
  // copy.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
  // VerticalLayout vBox = new VerticalLayout();
  // vBox.addComponent(copy);
  // complexComponent.addComponent(vBox);
  // complexComponent.setComponentAlignment(vBox, Alignment.BOTTOM_RIGHT);
  // return complexComponent;
  // }

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
        logger.info(
            "User \"" + userID + "\" not found. Probably running on Liferay and not logged in.");
        layout.addComponent(new Label("User not found. Are you logged in?"));
      }
    }
    // establish connection to the OpenBIS API
    if (!isDevelopment() || !testMode) {
      try {
        logger.debug("trying to connect to openbis");
        this.openbis = new OpenBisClient(dataSourceUser, dataSourcePass, dataSourceURL);
        this.openbis.login();
      } catch (Exception e) {
        success = false;
        logger.error(
            "User \"" + userID + "\" could not connect to openBIS and has been informed of this.");
        layout.addComponent(new Label(
            "Data Management System could not be reached. Please try again later or contact us."));
      }
    }
    if (isDevelopment() && testMode) {
      logger.error("No connection to openBIS. Trying mock version for testing.");
      this.openbis = new OpenBisClientMock(dataSourceUser, dataSourcePass, dataSourceURL);
      layout.addComponent(new Label(
          "openBIS could not be reached. Resuming with mock version. Some options might be non-functional. Reload to retry."));
    }
    if (success) {
      // stuff from openbis
      Map<String, String> taxMap = openbis.getVocabCodesAndLabelsForVocab("Q_NCBI_TAXONOMY");
      Map<String, String> tissueMap = openbis.getVocabCodesAndLabelsForVocab("Q_PRIMARY_TISSUES");
      Map<String, String> deviceMap = openbis.getVocabCodesAndLabelsForVocab("Q_MS_DEVICES");
      Map<String, String> cellLinesMap = openbis.getVocabCodesAndLabelsForVocab("Q_CELL_LINES");
      List<String> sampleTypes = openbis.getVocabCodesForVocab("Q_SAMPLE_TYPES");
      Map<String, String> purificationMethods =
          openbis.getVocabCodesAndLabelsForVocab("Q_PROTEIN_PURIFICATION_METHODS");
      List<String> fractionationTypes =
          openbis.getVocabCodesForVocab("Q_MS_FRACTIONATION_PROTOCOLS");
      List<String> enrichmentTypes = openbis.getVocabCodesForVocab("Q_MS_ENRICHMENT_PROTOCOLS");
      List<String> enzymes = openbis.getVocabCodesForVocab("Q_DIGESTION_PROTEASES");
      Map<String, String> antibodiesWithLabels =
          openbis.getVocabCodesAndLabelsForVocab("Q_ANTIBODY");
      List<String> msProtocols = openbis.getVocabCodesForVocab("Q_MS_PROTOCOLS");
      List<String> lcmsMethods = openbis.getVocabCodesForVocab("Q_MS_LCMS_METHODS");
      List<String> chromTypes = openbis.getVocabCodesForVocab("Q_CHROMATOGRAPHY_TYPES");
      final List<String> spaces = openbis.getUserSpaces(userID);
      isAdmin = openbis.isUserAdmin(userID);
      // stuff from mysql database
      DBConfig mysqlConfig = new DBConfig(mysqlHost, mysqlPort, mysqlDB, mysqlUser, mysqlPass);
      DBManager dbm = new DBManager(mysqlConfig);
      Map<String, Integer> peopleMap = fetchPeople(dbm);
      DBVocabularies vocabs = new DBVocabularies(taxMap, tissueMap, cellLinesMap, sampleTypes,
          spaces, peopleMap, expTypes, enzymes, antibodiesWithLabels, deviceMap, msProtocols,
          lcmsMethods, chromTypes, fractionationTypes, enrichmentTypes, purificationMethods);
      // initialize the View with sample types, spaces and the dictionaries of tissues and species
      initView(dbm, vocabs, userID);
      layout.addComponent(tabs);

    }
    if (LiferayAndVaadinUtils.isLiferayPortlet())
      try {
        for (com.liferay.portal.model.Role r : LiferayAndVaadinUtils.getUser().getRoles())
          if (r.getName().equals("Administrator")) {// TODO what other roles?
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

  private Map<String, Integer> fetchPeople(DBManager dbm) {
    Map<String, Integer> map = new HashMap<String, Integer>();
    try {
      map = dbm.getPrincipalInvestigatorsWithIDs();
    } catch (NullPointerException e) {
      map.put("No Connection", -1);
    }
    return map;
  }

  boolean isDevelopment() {
    boolean devEnv = false;
    try {
      // TODO tests if this is somehow a local development environment
      // in which case user is granted admin rights. Change so it works for you.
      // Be careful that this is never true on production or better yet that logged out users can
      // not see the portlet page.
      String path = new File(".").getCanonicalPath();
      devEnv = path.toLowerCase().contains("eclipse");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return devEnv;
  }

  private void initView(final DBManager dbm, final DBVocabularies vocabularies, final String user) {
    tabs.removeAllComponents();
    AttachmentConfig attachConfig = new AttachmentConfig(Integer.parseInt(attachmentSize),
        attachmentURI, attachmentUser, attachmentPass);
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
        vocabularies.setPeople(fetchPeople(dbm));
        vocabularies.setSpaces(openbis.getUserSpaces(user));
        initView(dbm, vocabularies, user);
      }

      @Override
      public void wizardCancelled(WizardCancelledEvent event) {
        vocabularies.setPeople(fetchPeople(dbm));
        vocabularies.setSpaces(openbis.getUserSpaces(user));
        initView(dbm, vocabularies, user);
      }

    };
    w.addListener(wl);
    VerticalLayout wLayout = new VerticalLayout();
    wLayout.addComponent(w);
    wLayout.setMargin(true);
//    wLayout.addComponent(addCommentLayout(EntityType.EXPERIMENT, "/CHICKEN_FARM/QANDI/QANDIE1", user));// TODO
    
    tabs.addTab(wLayout, "Create Project").setIcon(FontAwesome.FLASK);
    BarcodeConfig bcConf =
        new BarcodeConfig(barcodeScripts, tmpFolder, barcodeResultsFolder, pathVar);
    SampleFilterGenerator gen = new SampleFilterGenerator();
    BarcodeController bc = new BarcodeController(openbis, bcConf, dbm);
    gen.addObserver(bc);
    final WizardBarcodeView bw = new WizardBarcodeView(vocabularies.getSpaces(), isAdmin, gen);
    bw.initControl(bc);
    tabs.addTab(bw, "Create Barcodes").setIcon(FontAwesome.BARCODE);
    StandaloneTSVImport tsvImport = new StandaloneTSVImport();

    OpenbisCreationController creationController = new OpenbisCreationController(openbis);// will
                                                                                          // not
                                                                                          // work
                                                                                          // when
                                                                                          // openbis
                                                                                          // is down

    ExperimentImportController uc = new ExperimentImportController(tsvImport, creationController);
    uc.init(user);
    tabs.addTab(tsvImport, "Import Project").setIcon(FontAwesome.FILE);
    if (isAdmin) {
      logger.info("User is " + user + " and can see admin panel and print barcodes.");
      VerticalLayout padding = new VerticalLayout();
      padding.setMargin(true);
      padding
          .addComponent(new AdminView(openbis, vocabularies.getSpaces(), creationController, user));
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
    barcodeResultsFolder = c.getBarcodeResultsFolder();
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

//  private JAXBElement<Notes> parseNotes(EntityType t, String id) {
//    System.out.println(EntityType.EXPERIMENT);
//    JAXBElement<Notes> notes = null;
//    String xml = null;
//    if (t.equals(ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType.EXPERIMENT)) {
//      List<Experiment> e = openbis.getExperimentById2(id);
//      xml = e.get(0).getProperties().get("Q_NOTES");
//    } else {
//      java.util.EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.PROPERTIES);
//      SearchCriteria sc = new SearchCriteria();
//      sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, id));
//      List<Sample> samples = openbis.getOpenbisInfoService()
//          .searchForSamplesOnBehalfOfUser(openbis.getSessionToken(), sc, fetchOptions, "admin");
//      if (samples != null && samples.size() == 1) {
//        Sample sample = samples.get(0);
//        xml = sample.getProperties().get("Q_NOTES");
//      }
//    }
//    try {
//      if (xml != null) {
//        notes = HistoryReader.parseNotes(xml);
//      } else {
//        notes = new JAXBElement<Notes>(new QName(""), Notes.class, new Notes());
//      }
//    } catch (java.lang.IndexOutOfBoundsException | JAXBException | NullPointerException e) {
//      e.printStackTrace();
//    }
//    return notes;
//  }
//
//  private VerticalLayout addCommentLayout(EntityType t, String id, String user) {
//    VerticalLayout res = new VerticalLayout();
//    Panel commentsPanel = new Panel();
//    Grid comments = new Grid();
//    comments.setWidth(100, Unit.PERCENTAGE);
//    JAXBElement<Notes> notes = parseNotes(t, id);
//    BeanItemContainer<Note> container = new BeanItemContainer<Note>(Note.class);
//    container.addAll(notes.getValue().getNote());
//    comments.setContainerDataSource(container);
//    comments.setColumnOrder("time", "username", "comment");
//    comments.setHeightMode(HeightMode.ROW);
//
//    VerticalLayout addComment = new VerticalLayout();
//    addComment.setMargin(true);
//    addComment.setWidth(100, Unit.PERCENTAGE);
//    final TextArea newComments = new TextArea();
//    newComments.setInputPrompt("Write your comment here...");
//    newComments.setWidth(100, Unit.PERCENTAGE);
//    newComments.setRows(2);
//    Button sendComment = new Button("Add Comment");
//    sendComment.addStyleName(ValoTheme.BUTTON_FRIENDLY);
//    addComment.addComponent(newComments);
//    addComment.addComponent(sendComment);
//    sendComment.addClickListener(new ClickListener() {
//
//      @Override
//
//      public void buttonClick(ClickEvent event) {
//        if ("".equals(newComments.getValue()))
//          return;
//
//        String newComment = newComments.getValue();
//        // reset comments
//        newComments.setValue("");
//        // use some date format
//        Date dNow = new Date();
//        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//
//        Note note = new Note();
//        note.setComment(newComment);
//        note.setUsername(user);
//        note.setTime(ft.format(dNow));
//        writeNoteToOpenbis(id, note);
//
//        // show it now
//        comments.getContainerDataSource().addItem(note);
//
//        Label commentsLabel = new Label(
//            translateComments((BeanItemContainer<Note>) comments.getContainerDataSource(), user),
//            ContentMode.HTML);
//        commentsPanel.setContent(commentsLabel);
//
//      }
//    });
//    res.addComponent(addComment);
//    res.addComponent(commentsPanel);
//    return res;
//  }
//
//  private void writeNoteToOpenbis(String id, Note note) {
//    Map<String, Object> params = new HashMap<String, Object>();
//    params.put("id", id);
//    params.put("user", note.getUsername());
//    params.put("comment", note.getComment());
//    params.put("time", note.getTime());
//    openbis.ingest("DSS1", "add-to-xml-note", params);
//  }
//
//  public String translateComments(BeanItemContainer<Note> notes, String user) {
//    String lastDay = "";
//    String labelString = "";
//    for (Iterator<Note> i = notes.getItemIds().iterator(); i.hasNext();) {
//      Note noteBean = (Note) i.next();
//      String date = noteBean.getTime();
//      String[] datetime = date.split("T");
//      String day = datetime[0];
//      String time = datetime[1].split("\\.")[0];
//      if (!lastDay.equals(day)) {
//        lastDay = day;
//        labelString += String.format("%s\n", "<u>" + day + "</u>");
//      }
//      labelString += String.format("%s\n%s %s\n", "<p><b>" + user + "</b>.</p>",
//          noteBean.getComment(), "<p><i><small>" + time + "</small></i>.</p>");
//    }
//
//    return labelString;
//
//  }

}
