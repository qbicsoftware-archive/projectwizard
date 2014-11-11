package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import model.OpenbisInfoTextArea;

import org.vaadin.teemu.wizards.WizardStep;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class BioFactorStep implements WizardStep {
  
  boolean skip = false;

  VerticalLayout main;
  List<OpenbisInfoTextArea> factorInstances;
  List<ComboBox> speciesInstances;
  Set<String> speciesOptions;

  public BioFactorStep(Set<String> speciesOptions) {
    main = new VerticalLayout();
    main.setMargin(true);
    main.addComponent(new Label("Please fill in which cases of each condition exist in your study."));
    this.speciesOptions = speciesOptions;
    
    factorInstances = new ArrayList<OpenbisInfoTextArea>();
    speciesInstances = new ArrayList<ComboBox>();
  }

  @Override
  public String getCaption() {
    return "Biological Conditions";
  }

  public void initFactorFields(List<String> factors) {
    for (int i = 0; i < factors.size(); i++) {
      String f = factors.get(i);
      if (!f.equals("Species")) {
        OpenbisInfoTextArea a =
            new OpenbisInfoTextArea(f + " (Condition " + (i + 1) + ")",
                "Fill in the different cases of condition " + f + ", one per line.", "70", "60");
        factorInstances.add(a);
        main.addComponent(a.getInnerComponent());
      }
    }
  }

  public void initSpeciesFactorField(int amount) {
    for (int i = 1; i <= amount; i++) {
      ComboBox b = new ComboBox("Species " + i, speciesOptions);
      speciesInstances.add(b);
      main.addComponent(b);
    }
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    return skip || true;
  }

  @Override
  public boolean onBack() {
    return true;
  }

  public List<List<String>> getFactorValues() {
    List<List<String>> res = new ArrayList<List<String>>();
    for (OpenbisInfoTextArea a : this.factorInstances) {
      res.add(Arrays.asList(a.getValue().split("\n")));
    }
    if (this.speciesInstances != null) {
      List<String> species = new ArrayList<String>();
      for (ComboBox b : this.speciesInstances) {
        species.add((String) b.getValue());
      }
      res.add(species);
    }
    return res;
  }

  public void resetFactorFields() {
    for(OpenbisInfoTextArea a : factorInstances) {
      main.removeComponent(a.getInnerComponent());
    }
    factorInstances = new ArrayList<OpenbisInfoTextArea>();
    for(ComboBox b : speciesInstances) {
      main.removeComponent(b);
    }
    speciesInstances = new ArrayList<ComboBox>();
  }

  public void setSkipStep(boolean b) {
    skip = b;
  }

  public boolean isSkipped() {
    return skip;
  }
}
