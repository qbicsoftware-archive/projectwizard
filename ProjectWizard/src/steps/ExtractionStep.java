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
package steps;

import io.MethodVocabularyParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.ProjectwizardUI;

import org.vaadin.teemu.wizards.WizardStep;

import uicomponents.ConditionsPanel;
import uicomponents.LabelingMethod;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import componentwrappers.OpenbisInfoComboBox;
import componentwrappers.OpenbisInfoTextField;
import control.Functions;
import control.Functions.NotificationType;

/**
 * Wizard Step to model the extraction of biological samples from entities
 * 
 * @author Andreas Friedrich
 * 
 */
public class ExtractionStep implements WizardStep {

  boolean skip = false;
  OptionGroup conditionsSet = new OptionGroup("dummy");

  VerticalLayout main;

  OpenbisInfoComboBox tissue;
  ComboBox cellLine;
  Map<String, String> tissueMap;
  Map<String, String> cellLinesMap;

  GridLayout grid;
  ConditionsPanel c;

  String emptyFactor = "Other (please specify)";
  List<String> suggestions = new ArrayList<String>(Arrays.asList("Extraction time", "Tissue",
      "Growth Medium", "Radiation", "Treatment", emptyFactor));
  CheckBox isotopes;
  ComboBox isotopeTypes;

  OpenbisInfoTextField tissueNum;
  ComboBox person;

  OpenbisInfoTextField extractReps;
  private List<LabelingMethod> labelingMethods;

  public ConditionsPanel getCondPanel() {
    return c;
  }

  public OptionGroup conditionsSet() {
    return conditionsSet;
  }

  public String getPerson() {
    if (person.getValue() != null)
      return person.getValue().toString();
    else
      return null;
  }

  /**
   * Create a new Extraction step for the wizard
   * 
   * @param tissueMap A map of available tissues (codes and labels)
   * @param cellLinesMap
   */
  public ExtractionStep(Map<String, String> tissueMap, Map<String, String> cellLinesMap, Set<String> persons) {
    main = new VerticalLayout();
    main.setMargin(true);
    main.setSpacing(true);
    Label header = new Label("Sample Extracts");
    main.addComponent(ProjectwizardUI.questionize(header,
        "Extracts are individual tissue or other samples taken from organisms and used in the experiment. "
            + "You can input (optional) experimental variables, e.g. extraction times or treatments, that differ between different groups "
            + "of extracts.",
        "Sample Extracts"));

    this.tissueMap = tissueMap;
    ArrayList<String> tissues = new ArrayList<String>();
    tissues.addAll(tissueMap.keySet());
    Collections.sort(tissues);
    tissue = new OpenbisInfoComboBox("Tissue",
        "If different tissues are a study variables, leave this empty", tissues);
    tissue.getInnerComponent().setRequired(true);
    tissueNum = new OpenbisInfoTextField(
        "How many different tissue types are there in this sample extraction?", "", "50px", "2");
    tissueNum.getInnerComponent().setVisible(false);
    tissueNum.getInnerComponent().setEnabled(false);
    person = new ComboBox("Contact Person", persons);
    person.setStyleName(ProjectwizardUI.boxTheme);
    c = new ConditionsPanel(suggestions, emptyFactor, "Tissue",
        (ComboBox) tissue.getInnerComponent(), true, conditionsSet,
        (TextField) tissueNum.getInnerComponent());
    main.addComponent(c);

    isotopes = new CheckBox("Isotope Labeling");
    isotopes.setImmediate(true);
    main.addComponent(ProjectwizardUI.questionize(isotopes,
        "Are extracted cells labeled by isotope labeling (e.g. for Mass Spectrometry)?",
        "Isotope Labeling"));

    labelingMethods = initLabelingMethods();

    isotopeTypes = new ComboBox();
    isotopeTypes.setVisible(false);
    isotopeTypes.setImmediate(true);
    isotopeTypes.setStyleName(ProjectwizardUI.boxTheme);
    isotopeTypes.setNullSelectionAllowed(false);
    for (LabelingMethod l : labelingMethods)
      isotopeTypes.addItem(l.getName());
    main.addComponent(isotopeTypes);

    isotopes.addValueChangeListener(new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 6993706766195224898L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        isotopeTypes.setVisible(isotopes.getValue());
      }
    });
    main.addComponent(tissueNum.getInnerComponent());
    main.addComponent(tissue.getInnerComponent());
    main.addComponent(ProjectwizardUI.questionize(person,
        "Contact person responsible for tissue extraction.", "Contact Person"));

    tissue.getInnerComponent().addValueChangeListener(new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 1987640360028444299L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        cellLine.setVisible(tissue.getValue().equals("Cell Line"));
      }
    });
    this.cellLinesMap = cellLinesMap;
    ArrayList<String> cellLines = new ArrayList<String>();
    cellLines.addAll(cellLinesMap.keySet());
    Collections.sort(cellLines);
    cellLine = new ComboBox("Cell Line", cellLines);
    cellLine.setStyleName(ProjectwizardUI.boxTheme);
    cellLine.setImmediate(true);
    cellLine.setVisible(false);
    cellLine.setFilteringMode(FilteringMode.CONTAINS);
    main.addComponent(cellLine);

    extractReps = new OpenbisInfoTextField(
        "Extracted replicates per patient/animal/plant per experimental variable.",
        "Number of extractions per individual defined in the last step. "
            + "Technical replicates are added later!",
        "50px", "1");
    main.addComponent(extractReps.getInnerComponent());
  }

  private List<LabelingMethod> initLabelingMethods() {
    MethodVocabularyParser p = new MethodVocabularyParser();
    return p.parseQuantificationMethods(new File(ProjectwizardUI.MSLabelingMethods));
  }

  @Override
  public String getCaption() {
    return "Sample Extr.";
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    if (skip || tissueReady() && replicatesReady() && c.isValid())
      return true;
    else {
      Functions.notification("Information missing", "Please fill in the required fields.",
          NotificationType.ERROR);
      return false;
    }
  }

  private boolean replicatesReady() {
    return !extractReps.getValue().isEmpty();
  }

  private boolean tissueReady() {
    String t = tissue.getValue();
    return tissueIsFactor() || t != null;
  }

  @Override
  public boolean onBack() {
    return true;
  }

  public boolean tissueIsFactor() {
    return !tissue.getInnerComponent().isEnabled();
  }

  public List<String> getFactors() {
    return c.getConditions();
  }

  public int getExtractRepAmount() {
    return Integer.parseInt(extractReps.getValue());
  }

  public String getTissue() {
    return tissue.getValue();
  }

  public String getCellLine() {
    if (cellLine.getValue() != null)
      return (String) cellLine.getValue();
    else
      return "";
  }

  public boolean factorFieldOther(ComboBox source) {
    return emptyFactor.equals(source.getValue());
  }

  public int getTissueAmount() {
    return Integer.parseInt(tissueNum.getValue());
  }

  public void setSkipStep(boolean b) {
    skip = b;
  }

  public boolean isSkipped() {
    return skip;
  }

  public LabelingMethod getLabelingMethod() {
    Object o = isotopeTypes.getValue();
    if (o != null) {
      String type = o.toString();
      for (LabelingMethod l : labelingMethods)
        if (type.equals(l.getName()))
          return l;
      return null;
    } else
      return null;
  }

}
