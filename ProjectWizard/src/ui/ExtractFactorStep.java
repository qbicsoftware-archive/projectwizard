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

public class ExtractFactorStep implements WizardStep {
  
  boolean skip = false;

  VerticalLayout main;
  List<OpenbisInfoTextArea> factorInstances;
  List<ComboBox> tissueInstances;
  Set<String> tissueOptions;

  public ExtractFactorStep(Set<String> tissueOptions) {
    main = new VerticalLayout();
    main.setMargin(true);
    main.addComponent(new Label(
        "Please fill in which cases of each extraction condition exist in your study."));
    this.tissueOptions = tissueOptions;
    factorInstances = new ArrayList<OpenbisInfoTextArea>();
    tissueInstances = new ArrayList<ComboBox>();
  }

  @Override
  public String getCaption() {
    return "Extraction Conditions";
  }

  public void initFactorFields(List<String> factors) {
    for (int i = 0; i < factors.size(); i++) {
      String f = factors.get(i);
      if (!f.equals("Tissue")) {
        OpenbisInfoTextArea a =
            new OpenbisInfoTextArea(f + " (Condition " + (i + 1) + ")",
                "Fill in the different cases of condition " + f + ", one per line.", "70", "60");
        factorInstances.add(a);
        main.addComponent(a.getInnerComponent());
      }
    }
  }

  public void initTissueFactorField(int amount) {
    for (int i = 1; i <= amount; i++) {
      ComboBox b = new ComboBox("Tissue " + i, tissueOptions);
      tissueInstances.add(b);
      main.addComponent(b);
    }
  }

  public void resetFactorFields() {
    for (OpenbisInfoTextArea a : factorInstances) {
      main.removeComponent(a.getInnerComponent());
    }
    factorInstances = new ArrayList<OpenbisInfoTextArea>();
    for (ComboBox b : tissueInstances) {
      main.removeComponent(b);
    }
    tissueInstances = new ArrayList<ComboBox>();
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
    if (this.tissueInstances != null) {
      List<String> species = new ArrayList<String>();
      for (ComboBox b : this.tissueInstances) {
        species.add((String) b.getValue());
      }
      res.add(species);
    }
    return res;
  }

  public void setSkipStep(boolean b) {
    skip = b;
  }

  public boolean isSkipped() {
    return skip;
  }
}
