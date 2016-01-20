package steps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import main.ProjectwizardUI;

import org.vaadin.teemu.wizards.WizardStep;

import uicomponents.ConditionsPanel;

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

/**
 * Wizard Step to model the biological entities of an experiment
 * 
 * @author Andreas Friedrich
 * 
 */
public class EntityStep implements WizardStep {

  boolean skip = false;
  OptionGroup conditionsSet = new OptionGroup("dummy");

  VerticalLayout main;

  OpenbisInfoComboBox species;
  Map<String, String> speciesMap;

  GridLayout grid;
  ConditionsPanel c;

  String emptyFactor = "Other (please specify)";
  List<String> suggestions = new ArrayList<String>(Arrays.asList("Age", "Genotype", "Health State",
      "Phenotype", "Species", "Treatment", emptyFactor));

  OpenbisInfoTextField speciesNum;

  OpenbisInfoTextField bioReps;

  public ConditionsPanel getCondPanel() {
    return c;
  }

  public OptionGroup isConditionsSet() {
    return conditionsSet;
  }

  /**
   * Create a new Entity step for the wizard
   * 
   * @param speciesMap A map of available species (codes and labels)
   */
  public EntityStep(Map<String, String> speciesMap) {
    main = new VerticalLayout();
    main.setMargin(true);
    main.setSpacing(true);
    Label header = new Label("Sample Sources");
    main.addComponent(ProjectwizardUI
        .questionize(
            header,
            "Sample sources are individual patients, animals or plants that are used in the experiment. "
                + "You can input (optional) experimental variables, e.g. genotypes, that differ between different experimental groups.",
            "Sample Sources"));
    this.speciesMap = speciesMap;
    ArrayList<String> openbisSpecies = new ArrayList<String>();
    openbisSpecies.addAll(speciesMap.keySet());
    Collections.sort(openbisSpecies);
    species =
        new OpenbisInfoComboBox("Species",
            "If there are samples of different species, leave this empty", openbisSpecies);
    species.getInnerComponent().setRequired(true);
    speciesNum =
        new OpenbisInfoTextField("How many different species are there in this project?", "",
            "50px", "2");
    speciesNum.getInnerComponent().setVisible(false);
    speciesNum.getInnerComponent().setEnabled(false);
    c =
        new ConditionsPanel(suggestions, emptyFactor, "Species",
            (ComboBox) species.getInnerComponent(), true, conditionsSet,
            (TextField) speciesNum.getInnerComponent());
    main.addComponent(c);
    main.addComponent(speciesNum.getInnerComponent());
    main.addComponent(species.getInnerComponent());

    bioReps =
        new OpenbisInfoTextField(
            "How many identical biological replicates (e.g. animals) per group are there?",
            "Number of (biological) replicates for each group."
                + "Technical replicates are added later!", "50px", "1");
    main.addComponent(bioReps.getInnerComponent());
  }

  @Override
  public String getCaption() {
    return "Sample Sources";
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    if (skip || speciesReady() && replicatesReady() && c.isValid())
      return true;
    else {
      Notification n = new Notification("Please fill in the required fields.");
      n.setStyleName(ValoTheme.NOTIFICATION_CLOSABLE);
      n.setDelayMsec(-1);
      n.show(UI.getCurrent().getPage());
      return false;
    }
  }

  private boolean replicatesReady() {
    return !bioReps.getValue().isEmpty();
  }

  private boolean speciesReady() {
    String s = species.getValue();
    return speciesIsFactor() || (s != null && !species.getValue().isEmpty());
  }

  @Override
  public boolean onBack() {
    return true;
  }

  public boolean speciesIsFactor() {
    return !species.getInnerComponent().isEnabled();
  }

  public void enableSpeciesFactor(boolean enable) {
    speciesNum.getInnerComponent().setEnabled(enable);
    speciesNum.getInnerComponent().setVisible(enable);
    if (enable)
      species.getInnerComponent().setValue(null);
  }

  public List<String> getFactors() {
    return c.getConditions();
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
