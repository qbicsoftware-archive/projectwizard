package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import model.OpenbisInfoComboBox;
import model.OpenbisInfoTextField;

import org.vaadin.teemu.wizards.WizardStep;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ExtractionStep implements WizardStep {
  
  boolean skip = false;

  VerticalLayout main;

  OpenbisInfoComboBox tissue;
  Map<String, String> tissueMap;

  GridLayout grid;

  String emptyFactor = "Other (please specify)";
  List<String> suggestions = new ArrayList<String>(Arrays.asList("Extraction time", "Tissue",
      "Growth Medium", "Radiation", "Treatment", emptyFactor));
  ComboBox boxFactor1;
  ComboBox boxFactor2;
  ComboBox boxFactor3;
  TextField other1;
  TextField other2;
  TextField other3;

  OpenbisInfoTextField tissueNum;
  
  @SuppressWarnings("rawtypes")
  List<AbstractField> factorFields;

  OpenbisInfoTextField extractReps;

  CheckBox needBarcodes;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public ExtractionStep(Map<String, String> tissueMap) {
    main = new VerticalLayout();
    main.setMargin(true);

    main.addComponent(new Label("Are there different types of sample preparation?"));

    grid = new GridLayout(3, 2);
    boxFactor1 = initFactorBox("Condition 1");
    boxFactor2 = initFactorBox("Condition 2");
    boxFactor3 = initFactorBox("Condition 3");
    other1 = initFactorField();
    other2 = initFactorField();
    other3 = initFactorField();

    grid.addComponent(boxFactor1);
    grid.addComponent(boxFactor2);
    grid.addComponent(boxFactor3);
    grid.addComponent(other1);
    grid.addComponent(other2);
    grid.addComponent(other3);
    main.addComponent(grid);
    
    tissueNum = new OpenbisInfoTextField("How many different tissues are there in this sample extraction?", "", "25px", "2");
    tissueNum.getInnerComponent().setVisible(false);
    tissueNum.getInnerComponent().setEnabled(false);
    
    main.addComponent(tissueNum.getInnerComponent());

    this.tissueMap = tissueMap;
    ArrayList<String> tissues = new ArrayList<String>();
    tissues.addAll(tissueMap.keySet());
    Collections.sort(tissues);
    tissue =
        new OpenbisInfoComboBox("Tissue", "If different tissues are a study condition, leave this empty",
            tissues);
    main.addComponent(tissue.getInnerComponent());

    factorFields =
        new ArrayList<AbstractField>(Arrays.asList(boxFactor1, other1, boxFactor2, other2,
            boxFactor3, other3));

    extractReps =
        new OpenbisInfoTextField("Extracted replicates per entity",
            "Number of extractions per individual defined in the last step."
                + "Technical replicates are added later!", "25px", "1");
    main.addComponent(extractReps.getInnerComponent());
//    main.addComponent(new Label(
//        "Do you need barcodes for tubes for the sample extraction step (e.g. for freezing samples)?"));
//
//    needBarcodes = new CheckBox("Yes, create barcodes.");
//    main.addComponent(needBarcodes);
  }

  private TextField initFactorField() {
    TextField field = new TextField();
    field.setEnabled(false);
    field.setVisible(false);
    return field;
  }

  private ComboBox initFactorBox(String label) {
    ComboBox box = new ComboBox(label);
    box.addItems(suggestions);
    box.setImmediate(true);
    return box;
  }

  public void enableOtherField(ComboBox box, boolean enable) {
    if (box.equals(boxFactor1)) {
      other1.setEnabled(enable);
      other1.setVisible(enable);
    } else if (box.equals(boxFactor2)) {
      other2.setEnabled(enable);
      other2.setVisible(enable);
    } else if (box.equals(boxFactor3)) {
      other3.setEnabled(enable);
      other3.setVisible(enable);
    }
  }

  @Override
  public String getCaption() {
    return "Sample Extraction";
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    if (skip || (tissueReady() || factorReady()) && replicatesReady())
      return true;
    else
      return false;
  }

  private boolean replicatesReady() {
    return !extractReps.getValue().isEmpty();
  }

  private boolean tissueReady() {
    return !(tissue.getValue() == null || tissue.getValue().isEmpty());
  }
  
  @SuppressWarnings("rawtypes")
  private boolean factorReady() {
    for (AbstractField f : factorFields) {
      if (!factorFieldEmpty(f))
        return true;
    }
    return false;
  }

  @SuppressWarnings("rawtypes")
  public boolean factorFieldEmpty(AbstractField f) {
    Object val = f.getValue();
    return val == null || val.equals("") || val.equals(emptyFactor);
  }

  @Override
  public boolean onBack() {
    return true;
  }

  @SuppressWarnings("rawtypes")
  public List<String> getFactors() {
    List<String> res = new ArrayList<String>();
    for (AbstractField f : factorFields) {
      if (!factorFieldEmpty(f)) {
        res.add(f.getValue().toString());
      }
    }
    return res;
  }

  public List<ComboBox> getBoxFactors() {
    return new ArrayList<ComboBox>(Arrays.asList(boxFactor1, boxFactor2, boxFactor3));
  }

  public boolean tissueIsFactor() {
    return getFactors().contains("Tissue");
  }

  public void enableTissueField(boolean enable) {
    tissue.getInnerComponent().setEnabled(enable);
    tissueNum.getInnerComponent().setEnabled(!enable);
    tissueNum.getInnerComponent().setVisible(!enable);
    if(!enable) tissue.getInnerComponent().setValue(null);
  }

  public int getExtractRepAmount() {
    return Integer.parseInt(extractReps.getValue());
  }

  public String getTissue() {
    return tissue.getValue();
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
}
