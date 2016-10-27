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

import io.DBVocabularies;
import logging.Log4j2Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import main.ProjectwizardUI;
import model.AOpenbisSample;
import model.ExperimentModel;
import model.MSExperimentModel;
import model.OpenbisMSSample;
import model.OpenbisTestSample;
import properties.Factor;
import steps.MSAnalyteStep.AnalyteMultiplicationType;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

import control.Functions;
import control.Functions.NotificationType;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class MSSampleMultiplicationTable extends VerticalLayout {

  /**
   * 
   */
  private static final long serialVersionUID = -2282545855402710972L;
  private List<String> enzymes;
  private AnalyteMultiplicationType type;
  private boolean aboutPeptides;
  private List<EnzymeChooser> choosers;
  private Button.ClickListener buttonListener;
  private VerticalLayout enzymePane;
  private GridLayout buttonGrid;
  private Button add;
  private Button remove;

  private Map<String, AOpenbisSample> tableIdToParent;
  private Table sampleTable;
  private CheckBox poolSamples;

  logging.Logger logger = new Log4j2Logger(MSSampleMultiplicationTable.class);
  private GeneralMSInfoPanel generalFractionMSInfo;


  public MSSampleMultiplicationTable(AnalyteMultiplicationType type, DBVocabularies vocabs,
      boolean peptides) {
    setSpacing(true);

    this.type = type;
    this.aboutPeptides = peptides;
    this.enzymes = vocabs.getEnzymes();
    Collections.sort(enzymes);

    choosers = new ArrayList<EnzymeChooser>();
    EnzymeChooser c = new EnzymeChooser(enzymes);
    choosers.add(c);

    setSpacing(true);

    sampleTable = new Table();
    sampleTable.setWidth("640px");
    sampleTable.setCaption("Resulting " + type + "s");
    sampleTable.setStyleName(ProjectwizardUI.tableTheme);
    sampleTable.addContainerProperty("Base Sample", Label.class, null);
    sampleTable.addContainerProperty(type, Label.class, null);
    sampleTable.addContainerProperty(type + " Name", TextField.class, null);
    sampleTable.addContainerProperty(type + " Lab ID", TextField.class, null);
    sampleTable.addContainerProperty("Process", Component.class, null);

    sampleTable.setColumnWidth("Base Sample", 110);
    sampleTable.setColumnWidth(type, 65);
    sampleTable.setColumnWidth(type + " Name", 210);
    sampleTable.setColumnWidth(type + " Lab ID", 110);
    sampleTable.setColumnWidth("Process", 130);
    if (peptides) {
      sampleTable.setColumnCollapsingAllowed(true);
      sampleTable.setColumnCollapsed("Process", true);
    }
    addComponent(sampleTable);

    generalFractionMSInfo = new GeneralMSInfoPanel(vocabs, type + " Measurement Details");
    generalFractionMSInfo.setVisible(false);
    addComponent(generalFractionMSInfo);

    poolSamples = new CheckBox("Pool All " + type + "s");
    String info = "Create one pool of all protein " + type
        + " per original sample. They will be digested using the enzyme selected for digestion of single "
        + type + "s (see selection below).";
    if (peptides)
      info = "Create one pool of all peptide " + type
          + " per original sample. They will be measured using the same MS properties used for each single "
          + type + " (see selection below).";
    addComponent(ProjectwizardUI.questionize(poolSamples, info, "Pool All " + type + "s"));

    if (!peptides) {
      add = new Button();
      remove = new Button();
      ProjectwizardUI.iconButton(add, FontAwesome.PLUS_SQUARE);
      ProjectwizardUI.iconButton(remove, FontAwesome.MINUS_SQUARE);
      initListener();

      enzymePane = new VerticalLayout();
      enzymePane.setCaption(type + " Digestion Enzymes");
      enzymePane.addComponent(c);
      enzymePane.setVisible(false);
      addComponent(enzymePane);
      buttonGrid = new GridLayout(2, 1);
      buttonGrid.setSpacing(true);
      buttonGrid.addComponent(add);
      buttonGrid.addComponent(remove);
      buttonGrid.setVisible(false);
      addComponent(buttonGrid);
    }
  }

  private void pasteSelectionToColumn(String propertyName, Object selection) {
    for (Object id : sampleTable.getItemIds()) {
      ComboBox b = parseBoxRow(id, propertyName);
      b.setValue(selection);
    }
  }

  private Object createComplexCellComponent(ComboBox contentBox, String propertyName,
      final String id) {
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
        ComboBox b = parseBoxRow(id, propertyName);
        Object selection = b.getValue();
        pasteSelectionToColumn(propertyName, selection);
      }
    });
    return complexComponent;
  }

  private void initListener() {
    buttonListener = new Button.ClickListener() {

      private static final long serialVersionUID = 2240224129259577437L;

      @Override
      public void buttonClick(ClickEvent event) {
        if (event.getButton().equals(add))
          add();
        else
          remove();
      }
    };
    add.addClickListener(buttonListener);
    remove.addClickListener(buttonListener);
  }

  public List<String> getEnzymes() {
    List<String> res = new ArrayList<String>();
    for (EnzymeChooser c : choosers) {
      if (c.isSet())
        res.add(c.getEnzyme());
    }
    return res;
  }

  private void add() {
    if (choosers.size() < 4) {
      EnzymeChooser c = new EnzymeChooser(enzymes);
      choosers.add(c);

      removeComponent(buttonGrid);
      enzymePane.addComponent(c);
      addComponent(buttonGrid);
    }
  }

  private void remove() {
    int size = choosers.size();
    if (size > 1) {
      EnzymeChooser last = choosers.get(size - 1);
      last.reset();
      enzymePane.removeComponent(last);
      choosers.remove(last);
    }
  }

  public void resetInputs() {
    for (EnzymeChooser c : choosers) {
      c.reset();
    }
  }

  private ComboBox generateTableBox(Collection<String> entries, String width) {
    ComboBox b = new ComboBox();
    b.addItems(entries);
    b.setWidth(width);
    b.setFilteringMode(FilteringMode.CONTAINS);
    b.setStyleName(ProjectwizardUI.boxTheme);
    return b;
  }

  private TextField generateTableTextInput(String width) {
    TextField tf = new TextField();
    tf.setStyleName(ProjectwizardUI.fieldTheme);
    tf.setImmediate(true);
    tf.setWidth(width);
    tf.setValidationVisible(true);
    return tf;
  }

  public void setProteinSamples(List<AOpenbisSample> proteins,
      HashMap<Integer, Integer> tableIdToFractions, boolean peptides) {
    sampleTable.removeAllItems();
    tableIdToParent = new HashMap<String, AOpenbisSample>();
    int i = 0;
    for (AOpenbisSample s : proteins) {
      i++;
      // multiply by number of fractions
      for (int j = 1; j <= tableIdToFractions.get(i); j++) {
        boolean complexRow = (i == 1) && (j == 1); // the first row contains a combobox with added
                                                   // button to copy
        // its selection to the whole column

        String parentID = Integer.toString(i);
        String fractionID = Integer.toString(j);
        String id = parentID + "-" + fractionID;
        tableIdToParent.put(id, s);

        List<Object> row = new ArrayList<Object>();


        Label sample = new Label(s.getQ_SECONDARY_NAME() + "<br>" + s.getQ_EXTERNALDB_ID(),
            Label.CONTENT_XHTML);
        row.add(sample);

        Label num = new Label(fractionID);
        row.add(num);

        TextField secNameInput = generateTableTextInput("200px");
        secNameInput.setValue(s.getQ_SECONDARY_NAME() + " " + type + " #" + fractionID);
        row.add(secNameInput);
        TextField extIdInput = generateTableTextInput("95px");
        row.add(extIdInput);

        ComboBox processBox =
            generateTableBox(new ArrayList<String>(Arrays.asList("None", "Measure")), "95px");
        if (!peptides) {
          processBox.addItem("Digest");
          processBox.addItem("Both");
        }
        processBox.setNullSelectionAllowed(false);
        processBox.select("None");
        if (complexRow)
          row.add(createComplexCellComponent(processBox, "Process", id));
        else
          row.add(processBox);

        processBox.addValueChangeListener(new ValueChangeListener() {

          @Override
          public void valueChange(ValueChangeEvent event) {
            checkFractionMeasured();
          }
        });

        sampleTable.addItem(row.toArray(new Object[row.size()]), id);
      }
    }
    int pagelength = 0;
    for (int n : tableIdToFractions.values()) {
      pagelength += n;
    }
    sampleTable.setPageLength(pagelength);
    this.setVisible(pagelength > 0);
    checkFractionMeasured();
  }

  protected void checkFractionMeasured() {
    boolean digest = false;
    boolean measure = false;
    for (Object i : sampleTable.getItemIds()) {
      ComboBox processBox = parseBoxRow(i, "Process");
      String process = processBox.getValue().toString();
      digest |= process.equals("Both") || process.equals("Digest");
      measure |= process.equals("Both") || process.equals("Measure") || aboutPeptides;
    }
    boolean hasSamples = sampleTable.size() > 0;
    if (!aboutPeptides) {
      enzymePane.setVisible(digest && hasSamples);
      buttonGrid.setVisible(digest && hasSamples);
    }
    generalFractionMSInfo.setVisible(measure);
  }

  private ComboBox parseBoxRow(Object id, String propertyName) {
    Item item = sampleTable.getItem(id);
    Object component = item.getItemProperty(propertyName).getValue();
    if (component instanceof ComboBox)
      return (ComboBox) component;
    else {
      HorizontalLayout h = (HorizontalLayout) component;
      return (ComboBox) h.getComponent(0);
    }
  }

  private TextField parseTextRow(Object id, String propertyName) {
    Item item = sampleTable.getItem(id);
    TextField t = (TextField) item.getItemProperty(propertyName).getValue();
    return t;
  }

  public boolean isValid() {
    boolean res = true;
    String error = "";
    for (Iterator i = sampleTable.getItemIds().iterator(); i.hasNext();) {
      // Get the current item identifier, which is an integer.
      int iid = (Integer) i.next();

      // Now get the actual item from the table.
      // Item item = proteinExperiments.getItem(iid);
      error = "Please fill in the number of " + type + "s (or '0' for none)"; // TODO old?

      String fractions = parseTextRow(iid, type + "s").getValue();
      if (!fractions.isEmpty()) {
        try {
          Integer.parseInt(fractions);
        } catch (NumberFormatException e) {
          res = false;
        }
      } else {
        res = false;
      }
    }

    if (!res) {
      Functions.notification("Missing Input", error, NotificationType.ERROR);
      return false;
    } else
      return true;
  }

  public MSExperimentModel getFractionsWithMSProperties(MSExperimentModel model, String sampleType,
      String method) {
    Map<String, ExperimentModel> fractHelper = new HashMap<String, ExperimentModel>();
    List<ExperimentModel> fractionations = new ArrayList<ExperimentModel>();
    List<ExperimentModel> msExperiments = new ArrayList<ExperimentModel>();
    List<ExperimentModel> peptides = new ArrayList<ExperimentModel>();

    Map<String, Object> props = generalFractionMSInfo.getExperimentalProperties();
    List<String> enzymeList = getEnzymes();
    // there can be one ms measurement experiment per protein/peptide sample measured (fractions)
    for (Object i : sampleTable.getItemIds()) {
      String item = (String) i;
      String[] ids = item.split("-");

      AOpenbisSample parent = tableIdToParent.get(item);

      // new fraction/enrichment cycled sample - this always exists if it's in the table
      String secondaryName = parseTextRow(i, type.toString() + " Name").getValue();
      String extID = parseTextRow(i, type.toString() + " Lab ID").getValue();
      OpenbisTestSample fractionSample =
          new OpenbisTestSample(-2, new ArrayList<AOpenbisSample>(Arrays.asList(parent)),
              sampleType, secondaryName, extID, new ArrayList<Factor>(), "");

      ComboBox selection = parseBoxRow(i, "Process");
      String option = selection.getValue().toString();
      if (option.equals("Both") || option.equals("Measure") || sampleType.equals("PEPTIDES")) {
        // new ms sample
        OpenbisMSSample msSample =
            new OpenbisMSSample(-1, new ArrayList<AOpenbisSample>(Arrays.asList(fractionSample)),
                secondaryName + " run", extID + " run", new ArrayList<Factor>(), "");
        // new ms experiment
        ExperimentModel msExp =
            new ExperimentModel(item, new ArrayList<AOpenbisSample>(Arrays.asList(msSample)));
        msExp.setProperties(props);
        msExperiments.add(msExp);
      }
      if (option.equals("Both") || option.equals("Digest")) {
        OpenbisTestSample pepSample =
            new OpenbisTestSample(-1, new ArrayList<AOpenbisSample>(Arrays.asList(fractionSample)),
                "PEPTIDES", fractionSample.getQ_SECONDARY_NAME() + " digested",
                fractionSample.getQ_EXTERNALDB_ID(), new ArrayList<Factor>(), "");
        ExperimentModel peptideExp =
            new ExperimentModel(item, new ArrayList<AOpenbisSample>(Arrays.asList(pepSample)));
        String enzymes = StringUtils.join(enzymeList, ", ");
        peptideExp.addProperty("Q_ADDITIONAL_INFO", enzymes);
        peptides.add(peptideExp);
      }
      if (fractHelper.containsKey(ids[0])) {
        fractHelper.get(ids[0]).addSample(fractionSample);
      } else {
        // there is only one fractionation experiment per fractionation containing all of the
        // resulting samples
        ExperimentModel fractionExp =
            new ExperimentModel(item, new ArrayList<AOpenbisSample>(Arrays.asList(fractionSample)));
        switch (type) {
          case Fraction:
            fractionExp.addProperty("Q_MS_FRACTIONATION_METHOD", method);
            break;
          case Cycle:
            fractionExp.addProperty("Q_MS_ENRICHMENT_METHOD", method);
            break;
          default:
            logger.error("Unknown AnalyteMultiplicationType: " + type);
            break;
        }
        fractHelper.put(ids[0], fractionExp);// TODO x
        if (sampleType.equals("PEPTIDES"))
          peptides.add(fractionExp);
        else
          fractionations.add(fractionExp);
      }
    }
    if (fractionations.size() > 0)
      model.addAnalyteStepExperiments(fractionations);
    if (msExperiments.size() > 0)
      model.addMSRunStepExperiments(msExperiments);
    if (peptides.size() > 0)
      model.addPeptideExperiments(peptides);
    if (poolSamples.getValue()) {
      List<AOpenbisSample> pools = new ArrayList<AOpenbisSample>();
      int i = 0;
      for (String id : fractHelper.keySet()) {
        i++;

        List<AOpenbisSample> fractions = fractHelper.get(id).getSamples();
        AOpenbisSample pool = new OpenbisTestSample(1, fractions, sampleType,
            sampleType.toLowerCase() + " pool " + Integer.toString(i), "", new ArrayList<Factor>(),
            "");
        pools.add(pool);

        if (sampleType.equals("PEPTIDES")) {
          ExperimentModel msExp =
              new ExperimentModel(2, new ArrayList<AOpenbisSample>(Arrays.asList(pool)));
          msExp.setProperties(props);
          model.getLastStepMsRuns().add(msExp);
        } else {
          OpenbisTestSample pepSample =
              new OpenbisTestSample(-1, new ArrayList<AOpenbisSample>(Arrays.asList(pool)),
                  "PEPTIDES", pool.getQ_SECONDARY_NAME() + " digested", pool.getQ_EXTERNALDB_ID(),
                  new ArrayList<Factor>(), "");
          ExperimentModel peptideExp =
              new ExperimentModel(1, new ArrayList<AOpenbisSample>(Arrays.asList(pepSample)));
          String enzymes = StringUtils.join(enzymeList, ", ");
          peptideExp.addProperty("Q_ADDITIONAL_INFO", enzymes + " digestion");
          model.getPeptideExperiments().add(peptideExp);
        }
      }

      if (sampleType.equals("PEPTIDES")) {
        model.getPeptideExperiments().add(new ExperimentModel(3, pools));
      } else {
        model.getLastStepAnalytes().add(new ExperimentModel(3, pools));
      }
    }
    return model;
  }

  public boolean hasDigestions() {
    for (Object i : sampleTable.getItemIds()) {
      ComboBox selection = parseBoxRow(i, "Process");
      String option = selection.getValue().toString();
      if (option.equals("Both") || option.equals("Digest"))
        return true;
    }
    return false;
  }

  public void filterDictionariesByPrefix(String prefix) {
    generalFractionMSInfo.filterDictionariesByPrefix(prefix);
  }

}

