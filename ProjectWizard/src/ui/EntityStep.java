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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class EntityStep implements WizardStep {
  
  boolean skip = false;

  VerticalLayout main;

  OpenbisInfoComboBox species;
  Map<String, String> speciesMap;

  GridLayout grid;

  String emptyFactor = "Other (please specify)";
  List<String> suggestions = new ArrayList<String>(Arrays.asList("Age", "Genotype", "Health State",
      "Phenotype", "Species", "Treatment", emptyFactor));
  ComboBox boxFactor1;
  ComboBox boxFactor2;
  ComboBox boxFactor3;
  TextField other1;
  TextField other2;
  TextField other3;
  OpenbisInfoTextField speciesNum;

  @SuppressWarnings("rawtypes")
  List<AbstractField> factorFields;

  OpenbisInfoTextField bioReps;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public EntityStep(Map<String, String> speciesMap) {
    main = new VerticalLayout();
    main.setMargin(true);
    main.addComponent(new Label(
        "Are there conditions distinguishing the groups before sample extraction?"));

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

    speciesNum =
        new OpenbisInfoTextField("How many different species are there in this project?", "",
            "25px", "2");
    speciesNum.getInnerComponent().setVisible(false);
    speciesNum.getInnerComponent().setEnabled(false);
    main.addComponent(speciesNum.getInnerComponent());

    this.speciesMap = speciesMap;
    ArrayList<String> openbisSpecies = new ArrayList<String>();
    openbisSpecies.addAll(speciesMap.keySet());
    Collections.sort(openbisSpecies);
    species =
        new OpenbisInfoComboBox("Species",
            "If there are samples of different species, leave this empty", openbisSpecies);
    main.addComponent(species.getInnerComponent());

    factorFields =
        new ArrayList<AbstractField>(Arrays.asList(boxFactor1, other1, boxFactor2, other2,
            boxFactor3, other3));

    bioReps =
        new OpenbisInfoTextField(
            "How many biological replicates (e.g. mice) per condition are there?",
            "Number of (biological) replicates for each condition."
                + "Technical replicates are added later!", "25px", "1");

    main.addComponent(bioReps.getInnerComponent());
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
    return "Biological Entities";
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    if (skip || (factorReady() || speciesReady()) && replicatesReady())
      return true;
    else
      return false;
  }

  private boolean replicatesReady() {
    return !bioReps.getValue().isEmpty();
  }

  private boolean speciesReady() {
    String s = species.getValue();
    return s != null && !species.getValue().isEmpty();
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

  public boolean speciesIsFactor() {
    return getFactors().contains("Species");
  }

  public void enableSpeciesField(boolean enable) {
    species.getInnerComponent().setEnabled(enable);
    speciesNum.getInnerComponent().setEnabled(!enable);
    speciesNum.getInnerComponent().setVisible(!enable);
    if (!enable)
      species.getInnerComponent().setValue(null);
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

  public int getBioRepAmount() {
    return Integer.parseInt(bioReps.getValue());
  }

  public String getSpecies() {
    return species.getValue();
  }

  public boolean factorFieldOther(ComboBox source) {
    return emptyFactor.equals(source.getValue());
  }

  public int getSpeciesAmount() {
    return Integer.parseInt(speciesNum.getValue());
  }

  public void setSkipStep(boolean b) {
    skip = b;
  }

  public boolean isSkipped() {
    return skip;
  }

}
