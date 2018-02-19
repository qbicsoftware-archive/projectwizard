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
import life.qbic.openbis.openbisclient.IOpenBisClient;
import life.qbic.openbis.openbisclient.OpenBisClient;
import life.qbic.openbis.openbisclient.OpenBisClientMock;
import life.qbic.portal.liferayandvaadinhelpers.main.LiferayAndVaadinUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.annotation.WebServlet;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import logging.Log4j2Logger;
import model.AttachmentConfig;
import parser.XMLParser;
import properties.Qproperties;
import registration.OpenbisCreationController;

import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;

import views.AdminView;
import views.MetadataUploadView;

import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import control.ExperimentImportController;
import control.WizardController;

@SuppressWarnings("serial")
@Theme("projectwizard")
public class ProjectwizardUI extends UI {

  @WebServlet(value = "/*", asyncSupported = true)
  @VaadinServletConfiguration(productionMode = true, ui = ProjectwizardUI.class,
      widgetset = "main.widgetset.ProjectwizardWidgetset")
  public static class Servlet extends VaadinServlet {
  }

  public static boolean testMode = false;// TODO
  public static String MSLabelingMethods;
  public static String tmpFolder;

  // hardcoded stuff (main experiment types used in the wizard)
  List<String> expTypes = new ArrayList<String>(
      Arrays.asList("Q_EXPERIMENTAL_DESIGN", "Q_SAMPLE_EXTRACTION", "Q_SAMPLE_PREPARATION"));


  logging.Logger logger = new Log4j2Logger(ProjectwizardUI.class);
  private String version = "Version 1.34, 16.02.18";

  private ConfigurationManager config;

  private IOpenBisClient openbis;

  private final TabSheet tabs = new TabSheet();
  private boolean isAdmin = false;

  @Override
  protected void init(VaadinRequest request) {
    tabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
    VerticalLayout layout = new VerticalLayout();

    // read in the configuration file
    config = ConfigurationManagerFactory.getInstance();
    tmpFolder = config.getTmpFolder();
    MSLabelingMethods = config.getVocabularyMSLabeling();

    layout.setMargin(true);
    setContent(layout);
    String userID = "";
    boolean success = true;
    if (LiferayAndVaadinUtils.isLiferayPortlet()) {
      logger.info("Wizard " + version + " is running on Liferay and user is logged in.");
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
        this.openbis = new OpenBisClient(config.getDataSourceUser(), config.getDataSourcePassword(),
            config.getDataSourceUrl());
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
      this.openbis = new OpenBisClientMock(config.getDataSourceUser(),
          config.getDataSourcePassword(), config.getDataSourceUrl());
      layout.addComponent(new Label(
          "openBIS could not be reached. Resuming with mock version. Some options might be non-functional. Reload to retry."));
    }
    if (success) {
      // stuff from openbis
      Map<String, String> taxMap = openbis.getVocabCodesAndLabelsForVocab("Q_NCBI_TAXONOMY");
      Map<String, String> tissueMap = openbis.getVocabCodesAndLabelsForVocab("Q_PRIMARY_TISSUES");
      Map<String, String> deviceMap = openbis.getVocabCodesAndLabelsForVocab("Q_MS_DEVICES");
      Map<String, String> cellLinesMap = openbis.getVocabCodesAndLabelsForVocab("Q_CELL_LINES");
      Map<String, String> enzymeMap =
          openbis.getVocabCodesAndLabelsForVocab("Q_DIGESTION_PROTEASES");
      Map<String, String> chromTypes =
          openbis.getVocabCodesAndLabelsForVocab("Q_CHROMATOGRAPHY_TYPES");
      List<String> sampleTypes = openbis.getVocabCodesForVocab("Q_SAMPLE_TYPES");
      Map<String, String> purificationMethods =
          openbis.getVocabCodesAndLabelsForVocab("Q_PROTEIN_PURIFICATION_METHODS");
      List<String> fractionationTypes =
          openbis.getVocabCodesForVocab("Q_MS_FRACTIONATION_PROTOCOLS");
      List<String> enrichmentTypes = openbis.getVocabCodesForVocab("Q_MS_ENRICHMENT_PROTOCOLS");
      Map<String, String> antibodiesWithLabels =
          openbis.getVocabCodesAndLabelsForVocab("Q_ANTIBODY");
      List<String> msProtocols = openbis.getVocabCodesForVocab("Q_MS_PROTOCOLS");
      List<String> lcmsMethods = openbis.getVocabCodesForVocab("Q_MS_LCMS_METHODS");
      final List<String> spaces = openbis.getUserSpaces(userID);
      isAdmin = openbis.isUserAdmin(userID);
      // stuff from mysql database
      DBConfig mysqlConfig = new DBConfig(config.getMysqlHost(), config.getMysqlPort(),
          config.getMysqlDB(), config.getMysqlUser(), config.getMysqlPass());
      DBManager dbm = new DBManager(mysqlConfig);
      Map<String, Integer> peopleMap = dbm.fetchPeople();
      DBVocabularies vocabs = new DBVocabularies(taxMap, tissueMap, cellLinesMap, sampleTypes,
          spaces, peopleMap, expTypes, enzymeMap, antibodiesWithLabels, deviceMap, msProtocols,
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
    AttachmentConfig attachConfig =
        new AttachmentConfig(Integer.parseInt(config.getAttachmentMaxSize()),
            config.getAttachmentURI(), config.getAttachmentUser(), config.getAttachmenPassword());
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
        vocabularies.setPeople(dbm.fetchPeople());
        vocabularies.setSpaces(openbis.getUserSpaces(user));
        initView(dbm, vocabularies, user);
      }

      @Override
      public void wizardCancelled(WizardCancelledEvent event) {
        vocabularies.setPeople(dbm.fetchPeople());
        vocabularies.setSpaces(openbis.getUserSpaces(user));
        initView(dbm, vocabularies, user);
      }

    };
    w.addListener(wl);
    VerticalLayout wLayout = new VerticalLayout();
    wLayout.addComponent(w);
    wLayout.setMargin(true);

    tabs.addTab(wLayout, "Create Project").setIcon(FontAwesome.FLASK);
    // TODO barcode tab, remove once new portlet is online
    // BarcodeConfig bcConf = new BarcodeConfig(config.getBarcodeScriptsFolder(), tmpFolder,
    // config.getBarcodeResultsFolder(), config.getBarcodePathVariable());
    // SampleFilterGenerator gen = new SampleFilterGenerator();
    // BarcodeController bc = new BarcodeController(openbis, bcConf, dbm);
    // gen.addObserver(bc);
    // final WizardBarcodeView bw = new WizardBarcodeView(vocabularies.getSpaces(), isAdmin, gen);
    // bw.initControl(bc);
    // tabs.addTab(bw, "Create Barcodes").setIcon(FontAwesome.BARCODE);
    // tabs.addSelectedTabChangeListener(new SelectedTabChangeListener() {
    //
    // @Override
    // public void selectedTabChange(SelectedTabChangeEvent event) {
    // bw.resetSpace();
    // }
    // });

    OpenbisCreationController creationController = new OpenbisCreationController(openbis);// will
                                                                                          // not
                                                                                          // work
                                                                                          // when
                                                                                          // openbis
                                                                                          // is down

    ExperimentImportController uc =
        new ExperimentImportController(creationController, vocabularies, openbis, dbm);
    uc.init(user);
    tabs.addTab(uc.getView(), "Import Project").setIcon(FontAwesome.FILE);

    boolean overwriteAllowed = isAdmin || canOverwrite();
    tabs.addTab(new MetadataUploadView(openbis, vocabularies, overwriteAllowed), "Update Metadata")
        .setIcon(FontAwesome.PENCIL);;
    if (isAdmin) {
      logger.info("User is " + user + " and can see admin panel.");
      VerticalLayout padding = new VerticalLayout();
      padding.setMargin(true);
      padding.addComponent(new AdminView(openbis, vocabularies, creationController, user));
      tabs.addTab(padding, "Admin Functions").setIcon(FontAwesome.WRENCH);
    }
    if (overwriteAllowed)
      logger.info("User can overwrite existing metadata for their project.");
  }

  // TODO group that might be used to delete metadata or even sample/experiment objects in the
  // future
  private boolean canDelete() {
    try {
      User user = LiferayAndVaadinUtils.getUser();
      for (UserGroup grp : user.getUserGroups()) {
        String group = grp.getName();
        if (config.getDeletionGrp().contains(group)) {
          logger.info(
              "User " + user.getScreenName() + " can delete because they are part of " + group);
          return true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Could not fetch user groups. User won't be able to delete.");
    }
    return false;
  }

  private boolean canOverwrite() {
    try {
      User user = LiferayAndVaadinUtils.getUser();
      for (UserGroup grp : user.getUserGroups()) {
        String group = grp.getName();
        if (config.getMetadataWriteGrp().contains(group)) {
          logger.info("User " + user.getScreenName()
              + " can overwrite metadata because they are part of " + group);
          return true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Could not fetch user groups. User won't be able to overwrite metadata.");
    }
    return false;
  }

}
