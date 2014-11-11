package main;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.annotation.WebServlet;

import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.WizardStep;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;

import ui.BarcodeView;
import ui.CreateDataStep;
import ui.EntityStep;
import ui.ExtractFactorStep;
import ui.ExtractionStep;
import ui.BioFactorStep;
import ui.ProjectContextStep;
import ui.TestStep;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import control.BarcodeController;
import control.WizardController;
import control.WizardDataAggregator;

@SuppressWarnings("serial")
@Theme("projectwizard")
public class ProjectwizardUI extends UI {

  @WebServlet(value = "/*", asyncSupported = true)
  @VaadinServletConfiguration(productionMode = false, ui = ProjectwizardUI.class)
  public static class Servlet extends VaadinServlet {
  }

  private String DATASOURCE_USER = "datasource.user";
  private String DATASOURCE_PASS = "datasource.password";
  private String DATASOURCE_URL = "datasource.url";
  private String TMP_FOLDER = "tmp.folder";
  private String BARCODE_SCRIPTS = "barcode.scripts";
  private String PATH_VARIABLE = "path.variable";

  public static String tmpFolder;
  private String dataSourceUser;
  private String dataSourcePass;
  private String dataSourceURL;
  private String barcodeScripts;
  private String pathVar;

  OpenBisClient openbis;

  private final TabSheet tabs = new TabSheet();

  @Override
  protected void init(VaadinRequest request) {
    VerticalLayout layout = new VerticalLayout();
    readConfig();
    this.openbis = new OpenBisClient(dataSourceUser, dataSourcePass, dataSourceURL, true);
    layout.setMargin(true);
    setContent(layout);
    initView(openbis.getVocabCodesAndLabelsForVocab("Q_NCBI_TAXONOMY"),
        openbis.getVocabCodesAndLabelsForVocab("Q_PRIMARY_TISSUES"),
        openbis.getVocabCodesForVocab("Q_SAMPLE_TYPES"), openbis.listSpaces());
    layout.addComponent(tabs);
  }

  private void initView(final Map<String, String> taxMap, final Map<String, String> tissueMap,
      final List<String> sampleTypes, final List<String> spaces) {
    tabs.removeAllComponents();
    WizardController c = new WizardController(openbis, taxMap, tissueMap, sampleTypes, spaces);
    c.init();
    Wizard w = c.getWizard();
    WizardProgressListener wl = new WizardProgressListener() {

      @Override
      public void activeStepChanged(WizardStepActivationEvent event) {}

      @Override
      public void stepSetChanged(WizardStepSetChangedEvent event) {}

      @Override
      public void wizardCompleted(WizardCompletedEvent event) {}

      @Override
      public void wizardCancelled(WizardCancelledEvent event) {
        initView(taxMap, tissueMap, sampleTypes, spaces);
      }

    };
    w.addListener(wl);
    tabs.addTab(w, "Project-Wizard");
    BarcodeView bw = new BarcodeView(spaces);
    BarcodeController bc = new BarcodeController(bw, openbis, barcodeScripts, pathVar);
    bc.init();
    tabs.addTab(bw, "Barcodes and Sample Sheet");
  }

  private void readConfig() {
    Properties config = new Properties();
    try {
      config.load(new FileReader("/Users/frieda/Desktop/portlet.properties"));
      StringWriter configDebug = new StringWriter();
      config.list(new PrintWriter(configDebug));
      tmpFolder = config.getProperty(TMP_FOLDER);
      dataSourceUser = config.getProperty(DATASOURCE_USER);
      dataSourcePass = config.getProperty(DATASOURCE_PASS);
      dataSourceURL = config.getProperty(DATASOURCE_URL);
      barcodeScripts = config.getProperty(BARCODE_SCRIPTS);
      pathVar = config.getProperty(PATH_VARIABLE);
    } catch (IOException e) {
      System.err.println("Failed to load configuration: " + e);
    }
  }
}
