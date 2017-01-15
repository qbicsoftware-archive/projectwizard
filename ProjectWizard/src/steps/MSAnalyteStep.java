package steps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.teemu.wizards.WizardStep;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

import control.Functions;
import control.Functions.NotificationType;
import io.DBVocabularies;
import logging.Log4j2Logger;
import main.ProjectwizardUI;
import model.AOpenbisSample;
import model.ExperimentModel;
import model.MSExperimentModel;
import model.OpenbisMSSample;
import model.OpenbisTestSample;
import properties.Factor;
import uicomponents.EnzymePanel;
import uicomponents.MSSampleMultiplicationTable;

public class MSAnalyteStep implements WizardStep {

  private VerticalLayout main;
  logging.Logger logger = new Log4j2Logger(MSAnalyteStep.class);

  private OptionGroup analyteOptions = new OptionGroup();
  private Table baseAnalyteSampleTable;
  private MSSampleMultiplicationTable msFractionationTable;
  private MSSampleMultiplicationTable msEnrichmentTable;
  private TextField washRunCount;
  private Table washRuns;

  private HashMap<Integer, AOpenbisSample> tableIdToSample;
  private HashMap<Integer, Integer> tableIdToFractions;
  private HashMap<Integer, Integer> tableIdToCycles;
  private HashMap<Integer, List<String>> enzymeMap;
  private TextArea additionalInfo;

  private MSExperimentModel msExperimentModel;
  private String analyte;
  private DBVocabularies vocabs;
  private ComboBox fractionationSelection;
  private ComboBox enrichmentSelection;
  private boolean needsDigestion = false;
  private boolean hasRun = false;
  private MSExperimentModel results;
  private List<String> lcmsMethods;
  private List<String> devices;

  public static enum AnalyteMultiplicationType {
    Fraction, Cycle;
  }

  public MSAnalyteStep(DBVocabularies vocabs, String analyte) {
    this.analyte = analyte;
    main = new VerticalLayout();
    main.setSpacing(true);
    main.setMargin(true);
    this.vocabs = vocabs;

    additionalInfo = new TextArea("General Information");
    additionalInfo.setStyleName(ProjectwizardUI.areaTheme);
    main.addComponent(additionalInfo);

    String label = "Protein Options";
    String info =
        "Here you can select different fractionation techniques used on the protein samples as well as digest them using different enzymes. "
            + "For samples that are measured, mass spectrometry specific information can be saved.";
    if (analyte.equals("PEPTIDES")) {
      label = "Peptide Options";
      info = "Here you can select different fractionation techniques used on the peptide samples. "
          + "Mass spectrometry specific information about peptide measurements can be saved.";
    }
    Label header = new Label(label);
    main.addComponent(ProjectwizardUI.questionize(header, info, label));

    analyteOptions.addItems(new ArrayList<String>(Arrays.asList("Fractionation", "Enrichment")));
    analyteOptions.setMultiSelect(true);
    main.addComponent(analyteOptions);

    baseAnalyteSampleTable = new Table();
    // baseAnalyteSampleTable.setWidth(width);
    baseAnalyteSampleTable.setStyleName(ProjectwizardUI.tableTheme);
    baseAnalyteSampleTable.addContainerProperty("Sample", Label.class, null);
    baseAnalyteSampleTable.addContainerProperty("Fractions", TextField.class, null);
    baseAnalyteSampleTable.addContainerProperty("Cycles", TextField.class, null);
    baseAnalyteSampleTable.addContainerProperty("Process", Component.class, null);
    baseAnalyteSampleTable.addContainerProperty("Enzyme", Component.class, null);
    baseAnalyteSampleTable.addContainerProperty("Chr. Type", Component.class, null);
    baseAnalyteSampleTable.addContainerProperty("LCMS Method", Component.class, null);
    baseAnalyteSampleTable.addContainerProperty("Method Description", TextField.class, null);
    baseAnalyteSampleTable.addContainerProperty("MS Device", Component.class, null);

    baseAnalyteSampleTable.setColumnWidth("Process", 125);
    baseAnalyteSampleTable.setColumnWidth("Fractions", 71);
    baseAnalyteSampleTable.setColumnWidth("Cycles", 54);
    baseAnalyteSampleTable.setColumnWidth("Chr. Type", 130);
    baseAnalyteSampleTable.setColumnWidth("Enzyme", 135);
    baseAnalyteSampleTable.setColumnWidth("LCMS Method", 150);
    baseAnalyteSampleTable.setColumnWidth("Method Description", 175);
    baseAnalyteSampleTable.setColumnWidth("MS Device", 130);

    // This is where the magic happens. Selection of fractionation enables the fractions column of
    // the
    // main table.
    // Selection of enrichment enables the cycles column of the main table. Additionally, the combo
    // boxes
    // that allow specification of the used methods are enabled or disabled depending on user
    // choice. Changing enrichment cycles or fractions to values larger 1 results in a second table
    // (cycles table or fractionation table) to be initialized containing the resulting fractions or
    // enriched samples.
    analyteOptions.addValueChangeListener(new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        Collection<String> test = (Collection<String>) analyteOptions.getValue();
        boolean enrich = false;
        boolean fract = false;
        for (String s : test) {
          if (s.equals("Fractionation")) {
            fract = true;
          }
          if (s.equals("Enrichment")) {
            enrich = true;
          }
        }
        if (!fract) {
          for (Object id : baseAnalyteSampleTable.getItemIds()) {
            TextField b = parseTextRow(baseAnalyteSampleTable, id, "Fractions");
            b.setValue("0");
          }
        }
        if (!enrich) {
          for (Object id : baseAnalyteSampleTable.getItemIds()) {
            TextField b = parseTextRow(baseAnalyteSampleTable, id, "Cycles");
            b.setValue("0");
          }
        }
        enableCol("Fractions", fract);
        enableCol("Cycles", enrich);
        enrichmentSelection.setVisible(enrich);
        fractionationSelection.setVisible(fract);
      }
    });

    if (analyte.equals("PEPTIDES")) {
      baseAnalyteSampleTable.setColumnCollapsingAllowed(true);
      collapseColumn(true, "Enzyme");
    }

    main.addComponent(baseAnalyteSampleTable);

    List<String> fractMethods = vocabs.getFractionationTypes();
    Collections.sort(fractMethods);
    fractionationSelection = new ComboBox("Fractionation Method", fractMethods);
    fractionationSelection.setVisible(false);
    fractionationSelection.setRequired(true);
    fractionationSelection.setStyleName(ProjectwizardUI.boxTheme);
    fractionationSelection.setNullSelectionAllowed(false);
    main.addComponent(fractionationSelection);

    msFractionationTable = new MSSampleMultiplicationTable(AnalyteMultiplicationType.Fraction,
        vocabs, analyte.equals("PEPTIDES"));
    main.addComponent(msFractionationTable);

    List<String> enrichMethods = vocabs.getEnrichmentTypes();
    Collections.sort(enrichMethods);
    enrichmentSelection = new ComboBox("Enrichment Method", enrichMethods);
    enrichmentSelection.setStyleName(ProjectwizardUI.boxTheme);
    enrichmentSelection.setVisible(false);
    enrichmentSelection.setRequired(true);
    enrichmentSelection.setNullSelectionAllowed(false);
    main.addComponent(enrichmentSelection);

    msEnrichmentTable = new MSSampleMultiplicationTable(AnalyteMultiplicationType.Cycle, vocabs,
        analyte.equals("PEPTIDES"));
    main.addComponent(msEnrichmentTable);

    ObjectProperty<Integer> washCount = new ObjectProperty<Integer>(0);
    washRunCount = new TextField("Wash Runs (" + WordUtils.capitalize(analyte) + ")");
    washRunCount.setConverter(new StringToIntegerConverter());
    washRunCount.setWidth("40px");
    washRunCount.setStyleName(ProjectwizardUI.fieldTheme);
    washRunCount.setPropertyDataSource(washCount);

    washRuns = new Table("Wash Runs");
    washRuns.addContainerProperty("Name", TextField.class, null);
    washRuns.addContainerProperty("Lab ID", TextField.class, null);
    washRuns.setColumnWidth("Name", 210);
    washRuns.setColumnWidth("Lab ID", 110);
    washRuns.setVisible(false);

    washRunCount.addValueChangeListener(new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        washRuns.setVisible(washCount.getValue() > 0);
        for (int i = 1; i <= washCount.getValue(); i++) {
          List<Object> row = new ArrayList<Object>();

          TextField washName = generateTableTextInput("200px");
          washName.setValue("Wash Run " + Integer.toString(i));
          row.add(washName);
          row.add(generateTableTextInput("95px"));
          washRuns.addItem(row.toArray(new Object[row.size()]), i);

        }
        washRuns.setPageLength(Math.min(10, washCount.getValue()));
      }
    });

    main.addComponent(washRunCount);
    main.addComponent(washRuns);
  }

  private void enableCol(String colName, boolean enable) {
    for (Object i : baseAnalyteSampleTable.getItemIds()) {
      parseTextRow(baseAnalyteSampleTable, i, colName).setEnabled(enable);
    }
  }

  private void collapseColumn(boolean hide, String colName) {
    baseAnalyteSampleTable.setColumnCollapsed(colName, hide);
  }

  private TextField generateTableTextInput(String width) {
    TextField tf = new TextField();
    tf.setStyleName(ProjectwizardUI.fieldTheme);
    tf.setImmediate(true);
    tf.setWidth(width);
    tf.setValidationVisible(true);
    return tf;
  }

  public String getAdditionalInfo() {
    return additionalInfo.getValue();
  }

  public boolean hasRun() {
    return hasRun;
  }

  public void setAnalyteSamples(List<AOpenbisSample> analytes,
      Map<String, List<AOpenbisSample>> pools) {
    hasRun = false;
    boolean peptides = analyte.equals("PEPTIDES");

    this.msExperimentModel = new MSExperimentModel();
    List<AOpenbisSample> samplesForTable = new ArrayList<AOpenbisSample>();
    samplesForTable.addAll(analytes);
    if (pools != null)
      samplesForTable.addAll(getPoolingSamples(pools));
    baseAnalyteSampleTable.removeAllItems();
    tableIdToSample = new HashMap<Integer, AOpenbisSample>();
    tableIdToFractions = new HashMap<Integer, Integer>();
    tableIdToCycles = new HashMap<Integer, Integer>();
    enzymeMap = new HashMap<Integer, List<String>>();
    int i = 0;
    for (AOpenbisSample s : samplesForTable) {
      i++;
      boolean complexRow = i == 1; // the first row contains a combobox with added button to copy
                                   // its selection to the whole column
      tableIdToSample.put(i, s);
      tableIdToFractions.put(i, 0);
      tableIdToCycles.put(i, 0);

      List<Object> row = new ArrayList<Object>();

      Label sample =
          new Label(s.getQ_SECONDARY_NAME() + "<br>" + s.getQ_EXTERNALDB_ID(), Label.CONTENT_XHTML);

      row.add(sample);
      TextField fractionNumberField = generateTableTextInput("50px");
      fractionNumberField.setEnabled(fractionationSelection.isVisible());
      row.add(fractionNumberField);
      TextField cycleNumberField = generateTableTextInput("50px");
      cycleNumberField.setEnabled(enrichmentSelection.isVisible());
      row.add(cycleNumberField);

      List<String> processOptions = new ArrayList<String>(Arrays.asList("None", "Measure"));
      if (analyte.equals("PROTEINS")) {
        processOptions.add("Digest");
        processOptions.add("Both");
      }
      ComboBox processBox = generateTableBox(processOptions, "95px");

      processBox.setNullSelectionAllowed(false);
      processBox.select("None");
      if (complexRow)
        row.add(createComplexCellComponent(processBox, "Process", i));
      else
        row.add(processBox);

      List<String> enzymes = vocabs.getEnzymes();
      Collections.sort(enzymes);
      ComboBox enzymeBox = generateTableBox(enzymes, "105px");
      enzymeBox.removeAllItems();
      enzymeBox.addItem("[Multiple]");
      enzymeBox.addItems(enzymes);
      enzymeBox.setEnabled(false);
      enzymeBox.setFilteringMode(FilteringMode.CONTAINS);
      if (complexRow)
        row.add(createComplexCellComponent(enzymeBox, "Enzyme", i));
      else
        row.add(enzymeBox);
      final int rowNum = i;
      enzymeBox.addValueChangeListener(new ValueChangeListener() {

        @Override
        public void valueChange(ValueChangeEvent event) {
          Object newVal = enzymeBox.getValue();
          if (newVal.equals("[Multiple]"))
            createEnzymeSelectionWindow(rowNum);
          else if (!newVal.equals("Custom"))
            enzymeBox.removeItem("Custom");
        }
      });

      List<String> chromTypes = new ArrayList<String>(vocabs.getChromTypesMap().keySet());
      Collections.sort(chromTypes);
      ComboBox chrTypeBox = generateTableBox(chromTypes, "95px");
      chrTypeBox.setEnabled(peptides);
      chrTypeBox.setFilteringMode(FilteringMode.CONTAINS);
      if (complexRow)
        row.add(createComplexCellComponent(chrTypeBox, "Chr. Type", i));
      else
        row.add(chrTypeBox);

      ComboBox lcmsMethodBox = generateTableBox(lcmsMethods, "115px");
      lcmsMethodBox.setEnabled(peptides);
      lcmsMethodBox.setFilteringMode(FilteringMode.CONTAINS);
      if (complexRow)
        row.add(createComplexCellComponent(lcmsMethodBox, "LCMS Method", i));
      else
        row.add(lcmsMethodBox);

      TextField lcmsSpecialField = generateTableTextInput("165px");
      lcmsSpecialField.setEnabled(false);
      row.add(lcmsSpecialField);

      lcmsMethodBox.addValueChangeListener(new ValueChangeListener() {

        @Override
        public void valueChange(ValueChangeEvent event) {
          String val = (String) lcmsMethodBox.getValue();
          boolean special = val.equals("SPECIAL_METHOD");
          lcmsSpecialField.setEnabled(special);
          if (!special)
            lcmsSpecialField.setValue("");
        }
      });

      ComboBox deviceBox = generateTableBox(devices, "100px");
      deviceBox.setEnabled(peptides);
      deviceBox.setFilteringMode(FilteringMode.CONTAINS);
      if (complexRow)
        row.add(createComplexCellComponent(deviceBox, "MS Device", i));
      else
        row.add(deviceBox);

      baseAnalyteSampleTable.addItem(row.toArray(new Object[row.size()]), i);

      fractionNumberField.setValue("0");
      final int item = i;
      fractionNumberField.addValueChangeListener(new ValueChangeListener() {

        @Override
        public void valueChange(ValueChangeEvent event) {
          String value = fractionNumberField.getValue();
          boolean fractionation = Functions.isInteger(value) && Integer.parseInt(value) >= 0;
          if (fractionation) {
            tableIdToFractions.put(item, Integer.parseInt(value));
            msFractionationTable.setProteinSamples(samplesForTable, tableIdToFractions,
                analyte.equals("PEPTIDES"));
          }
        }
      });
      cycleNumberField.setValue("0");
      cycleNumberField.addValueChangeListener(new ValueChangeListener() {

        @Override
        public void valueChange(ValueChangeEvent event) {
          String value = cycleNumberField.getValue();
          boolean enrichment = Functions.isInteger(value) && Integer.parseInt(value) >= 0;
          if (enrichment) {
            tableIdToCycles.put(item, Integer.parseInt(value));
            msEnrichmentTable.setProteinSamples(samplesForTable, tableIdToCycles,
                analyte.equals("PEPTIDES"));
          }
        }
      });

      processBox.addValueChangeListener(new ValueChangeListener() {

        @Override
        public void valueChange(ValueChangeEvent event) {
          String value = (String) processBox.getValue();
          boolean enableEnzyme = value.equals("Digest") || value.equals("Both");
          boolean enableMS = value.equals("Measure") || value.equals("Both");
          parseBoxRow(baseAnalyteSampleTable, item, "Enzyme").setEnabled(enableEnzyme);
          parseBoxRow(baseAnalyteSampleTable, item, "Chr. Type").setEnabled(enableMS);
          parseBoxRow(baseAnalyteSampleTable, item, "LCMS Method").setEnabled(enableMS);
          parseBoxRow(baseAnalyteSampleTable, item, "MS Device").setEnabled(enableMS);
        }
      });
    }
    baseAnalyteSampleTable.setPageLength(samplesForTable.size());
    msFractionationTable.setProteinSamples(samplesForTable, tableIdToFractions,
        analyte.equals("PEPTIDES"));
    msEnrichmentTable.setProteinSamples(samplesForTable, tableIdToCycles,
        analyte.equals("PEPTIDES"));
  }

  protected void createEnzymeSelectionWindow(int row) {
    Window subWindow = new Window(" Enzyme selection");
    subWindow.setWidth("400px");

    VerticalLayout layout = new VerticalLayout();
    layout.setSpacing(true);
    layout.setMargin(true);
    EnzymePanel pan = new EnzymePanel(vocabs.getEnzymes());
    Button ok = new Button("Okay.");
    ok.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        List<String> enzymes = pan.getEnzymes();
        ComboBox b = parseBoxRow(baseAnalyteSampleTable, row, "Enzyme");
        if (enzymes.isEmpty()) {
          Functions.notification("No enzymes selected", "Please select at least one enzyme!",
              NotificationType.ERROR);
        } else if (enzymes.size() == 1) {
          b.setValue(enzymes.get(0));
          subWindow.close();
        } else {
          b.addItem("Custom");
          b.setValue("Custom");
          enzymeMap.put(row, enzymes);
          subWindow.close();
        }
      }
    });
    layout.addComponent(pan);
    layout.addComponent(ok);

    subWindow.setContent(layout);
    // Center it in the browser window
    subWindow.center();
    subWindow.setModal(true);
    subWindow.setIcon(FontAwesome.FLASK);
    subWindow.setResizable(false);
    ProjectwizardUI ui = (ProjectwizardUI) UI.getCurrent();
    ui.addWindow(subWindow);
  }

  private Object createComplexCellComponent(ComboBox contentBox, String propertyName,
      final int rowID) {
    HorizontalLayout complexComponent = new HorizontalLayout();
    complexComponent.setWidth(contentBox.getWidth() + 10, contentBox.getWidthUnits());
    complexComponent.addComponent(contentBox);
    complexComponent.setExpandRatio(contentBox, 1);

    Button copy = new Button();
    ProjectwizardUI.iconButton(copy, FontAwesome.ARROW_CIRCLE_O_DOWN);
    copy.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
    VerticalLayout vBox = new VerticalLayout();
    vBox.setWidth("15px");
    vBox.addComponent(copy);
    complexComponent.addComponent(vBox);
    complexComponent.setComponentAlignment(vBox, Alignment.BOTTOM_RIGHT);
    copy.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        ComboBox b = parseBoxRow(baseAnalyteSampleTable, rowID, propertyName);
        Object selection = b.getValue();
        pasteSelectionToColumn(propertyName, selection);
      }
    });
    return complexComponent;
  }

  private List<AOpenbisSample> getPoolingSamples(Map<String, List<AOpenbisSample>> pools) {
    List<AOpenbisSample> res = new ArrayList<AOpenbisSample>();
    if (pools.size() > 0) {
      for (String secName : pools.keySet()) {
        List<AOpenbisSample> parents = new ArrayList<AOpenbisSample>();
        for (AOpenbisSample s : pools.get(secName)) {
          parents.add(s);
        }
        res.add(new OpenbisTestSample(-1, parents, this.analyte, secName, "", // TODO ext db id
            new ArrayList<Factor>(), ""));
      }
    }
    return res;
  }

  private ComboBox parseBoxRow(Table table, Object rowID, String propertyName) {
    Item item = table.getItem(rowID);
    Object component = item.getItemProperty(propertyName).getValue();
    if (component instanceof ComboBox)
      return (ComboBox) component;
    else {
      HorizontalLayout h = (HorizontalLayout) component;
      return (ComboBox) h.getComponent(0);
    }
  }

  private void pasteSelectionToColumn(String propertyName, Object selection) {
    for (Object id : baseAnalyteSampleTable.getItemIds()) {
      // should always be ID = 1
      ComboBox b = parseBoxRow(baseAnalyteSampleTable, id, propertyName);
      if (selection.equals("Custom") && propertyName.equals("Enzyme")) {
        Integer i = (int) id;
        enzymeMap.put(i, enzymeMap.get(1));
        b.addItem("Custom");
      }
      b.setValue(selection);
    }
  }

  private TextField parseTextRow(Table table, Object id, String propertyName) {
    Item item = table.getItem(id);
    TextField t = (TextField) item.getItemProperty(propertyName).getValue();
    return t;
  }

  private ComboBox generateTableBox(Collection<String> entries, String width) {
    ComboBox b = new ComboBox();
    b.addItems(entries);
    b.setWidth(width);
    b.setFilteringMode(FilteringMode.CONTAINS);
    b.setStyleName(ProjectwizardUI.boxTheme);
    return b;
  }

  @Override
  public String getCaption() {
    if (analyte.equals("PEPTIDES"))
      return "Peptide Options";
    else
      return "Protein Options";
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    return isValid();
  }

  private boolean isValid() {
    Set<String> selected = (Set<String>) analyteOptions.getValue();
    if (selected.contains("Fractionation") && (fractionationSelection.getValue() == null
        || fractionationSelection.getValue().toString().isEmpty())) {
      Functions.notification("Please Select Fractionation",
          "Please select the type of fractionation you want to perform on the samples or deselect 'Fractionation'.",
          NotificationType.ERROR);
      return false;
    }
    if (selected.contains("Enrichment") && (enrichmentSelection.getValue() == null
        || enrichmentSelection.getValue().toString().isEmpty())) {
      Functions.notification("Please Select Enrichment",
          "Please select the type of enrichment you want to perform on the samples or deselect 'Enrichment'.",
          NotificationType.ERROR);
      return false;
    }
    if (analyte.equals("PROTEINS") && needsDigestion) {
      for (Object i : baseAnalyteSampleTable.getItemIds()) {
        ComboBox selection = parseBoxRow(baseAnalyteSampleTable, i, "Process");
        String option = selection.getValue().toString();
        if (option.equals("Both") || option.equals("Digest")) {
          return true;
        }
      }
      if (!msFractionationTable.hasDigestions() && !msEnrichmentTable.hasDigestions()) {
        Functions.notification("Please Select Digestion",
            "Please add at least one process of protein digestion or deselect peptide measurement in a previous step.",
            NotificationType.ERROR);
        return false;
      }
    }
    return true;

  }

  @Override
  public boolean onBack() {
    return true;
  }

  public void createPreliminaryExperiments() {
    String method = "unknown";// TODO this can't reach openbis
    if (fractionationSelection.getValue() != null)
      method = fractionationSelection.getValue().toString();
    this.msExperimentModel =
        msFractionationTable.getFractionsWithMSProperties(this.msExperimentModel, analyte, method);

    method = "unknown";// TODO this can't reach openbis
    if (enrichmentSelection.getValue() != null)
      method = enrichmentSelection.getValue().toString();
    this.msExperimentModel =
        msEnrichmentTable.getFractionsWithMSProperties(this.msExperimentModel, analyte, method);
    hasRun = true;
    this.results = getSamplesWithMSProperties();
  }

  public MSExperimentModel getSamplesWithMSProperties() {
    ExperimentModel baseAnalytes = new ExperimentModel(-5);
    List<ExperimentModel> msExperiments = new ArrayList<ExperimentModel>();
    List<ExperimentModel> peptides = new ArrayList<ExperimentModel>();

    if (analyte.equals("PROTEINS")) {
      for (Object i : baseAnalyteSampleTable.getItemIds()) {

        AOpenbisSample baseAnalyte = tableIdToSample.get(i);
        baseAnalytes.addSample(baseAnalyte);
      }
      msExperimentModel.setBaseAnalytes(baseAnalytes);
    }

    for (Object i : baseAnalyteSampleTable.getItemIds()) {
      String item = Integer.toString((int) i);
      AOpenbisSample parent = tableIdToSample.get(i);

      ComboBox selection = parseBoxRow(baseAnalyteSampleTable, i, "Process");
      String option = selection.getValue().toString();
      if (option.equals("Both") || option.equals("Measure")) {
        // new ms sample from existing proteins (no fractions/enrichments) or peptides
        OpenbisMSSample msSample =
            new OpenbisMSSample(1, new ArrayList<AOpenbisSample>(Arrays.asList(parent)),
                parent.getQ_SECONDARY_NAME() + " run", parent.getQ_EXTERNALDB_ID() + " run",
                new ArrayList<Factor>(), "");
        // new ms experiment
        ExperimentModel msExp =
            new ExperimentModel(item, new ArrayList<AOpenbisSample>(Arrays.asList(msSample)));
        Map<String, Object> props = getMSPropertiesFromSampleRow(i);
        msExp.setProperties(props);
        msExperiments.add(msExp);
      }
      if (option.equals("Both") || option.equals("Digest")) {
        OpenbisTestSample pepSample =
            new OpenbisTestSample(-1, new ArrayList<AOpenbisSample>(Arrays.asList(parent)),
                "PEPTIDES", parent.getQ_SECONDARY_NAME() + " digested", parent.getQ_EXTERNALDB_ID(),
                new ArrayList<Factor>(), "");
        ExperimentModel peptideExp =
            new ExperimentModel("", new ArrayList<AOpenbisSample>(Arrays.asList(pepSample)));
        List<String> enzymes = getEnzymesFromSampleRow(i);
        String digestion = StringUtils.join(enzymes, ", ");
        peptideExp.addProperty("Q_ADDITIONAL_INFO", "Digestion: " + digestion);
        peptides.add(peptideExp);
      }
    }
    // add wash runs
    ExperimentModel washExp = new ExperimentModel("", new ArrayList<AOpenbisSample>());
    for (Object i : washRuns.getItemIds()) {
      int item = (int) i;
      String name = parseTextRow(washRuns, i, "Name").getValue();
      String labID = parseTextRow(washRuns, i, "Lab ID").getValue();
      OpenbisMSSample washRun = new OpenbisMSSample(item, new ArrayList<AOpenbisSample>(), name,
          labID, new ArrayList<Factor>(), "");
      washExp.addSample(washRun);
    }
    if (washRuns.size() > 0) {
      logger.debug("adding wash runs: " + washExp.getSamples().size());
      msExperiments.add(washExp);
    }
    if (msExperiments.size() > 0)
      msExperimentModel.addMSRunStepExperiments(msExperiments);
    if (peptides.size() > 0)
      msExperimentModel.addPeptideExperiments(peptides);
    return msExperimentModel;
  }

  private Map<String, Object> getMSPropertiesFromSampleRow(Object i) {
    Map<String, Object> res = new HashMap<String, Object>();
    Object device = parseBoxRow(baseAnalyteSampleTable, i, "MS Device").getValue();
    Object lcms = parseBoxRow(baseAnalyteSampleTable, i, "LCMS Method").getValue();
    Object chrom = parseBoxRow(baseAnalyteSampleTable, i, "Chr. Type").getValue();
    String special = parseTextRow(baseAnalyteSampleTable, i, "Method Description").getValue();
    if (device != null)
      res.put("Q_MS_DEVICE", vocabs.getDeviceMap().get(device.toString()));
    if (lcms != null)
      res.put("Q_MS_LCMS_METHOD", lcms.toString());
    if (chrom != null)
      res.put("Q_CHROMATOGRAPHY_TYPE", vocabs.getChromTypesMap().get(chrom.toString()));
    if (!special.isEmpty())
      res.put("Q_MS_LCMS_METHOD_INFO", special);
    return res;
  }

  private List<String> getEnzymesFromSampleRow(Object i) {
    if (parseBoxRow(baseAnalyteSampleTable, i, "Enzyme").getValue() == null)
      return null;
    else {
      String entry = parseBoxRow(baseAnalyteSampleTable, i, "Enzyme").getValue().toString();
      if (entry.equals("Custom"))
        return enzymeMap.get(i);
      else
        return new ArrayList<String>(Arrays.asList(entry));
    }
  }

  public void setAnalyteSamplesAndExperiments(MSExperimentModel msExperimentModel) {

    List<AOpenbisSample> allSamples = new ArrayList<AOpenbisSample>();
    List<ExperimentModel> source = msExperimentModel.getLastStepAnalytes();
    if (analyte.equals("PEPTIDES"))
      source = msExperimentModel.getPeptideExperiments();
    for (ExperimentModel analytes : source) {
      allSamples.addAll(analytes.getSamples());
    }
    setAnalyteSamples(allSamples, null);// this resets the model, needed for pooling
    this.msExperimentModel = new MSExperimentModel(msExperimentModel);
  }

  public void setNeedsDigestion(boolean required) {
    this.needsDigestion = required;
  }

  public MSExperimentModel getResults() {
    return results;
  }

  public void filterDictionariesByPrefix(String prefix) {
    devices = new ArrayList<String>();
    lcmsMethods = new ArrayList<String>();
    if (prefix.isEmpty()) {
      devices.addAll(vocabs.getDeviceMap().keySet());
    } else {
      for (String device : vocabs.getDeviceMap().keySet()) {
        if (device.contains("(" + prefix + ")"))
          devices.add(device);
      }
    }
    for (String lcmsMethod : vocabs.getLcmsMethods()) {
      if (lcmsMethod.startsWith(prefix))
        lcmsMethods.add(lcmsMethod);
    }
    Collections.sort(devices);
    Collections.sort(lcmsMethods);
    msEnrichmentTable.filterDictionariesByPrefix(prefix);
    msFractionationTable.filterDictionariesByPrefix(prefix);
  }

}
