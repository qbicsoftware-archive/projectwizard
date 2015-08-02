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
  Map<String, String> tissueMap;

  GridLayout grid;
  ConditionsPanel c;

  String emptyFactor = "Other (please specify)";
  List<String> suggestions = new ArrayList<String>(Arrays.asList("Extraction time", "Tissue",
      "Growth Medium", "Radiation", "Treatment", emptyFactor));

  OpenbisInfoTextField tissueNum;

  OpenbisInfoTextField extractReps;

  public ConditionsPanel getCondPanel() {
    return c;
  }

  public OptionGroup conditionsSet() {
    return conditionsSet;
  }

  /**
   * Create a new Extraction step for the wizard
   * 
   * @param tissueMap A map of available tissues (codes and labels)
   */
  public ExtractionStep(Map<String, String> tissueMap) {
    main = new VerticalLayout();
    main.setMargin(true);
    main.setSpacing(true);
    Label header = new Label("Sample Extracts");
    main.addComponent(ProjectwizardUI
        .questionize(
            header,
            "Extracts are individual tissue or other samples taken from organisms and used in the experiment. "
                + "You can input (optional) experimental variables, e.g. extraction times or treatments, that differ between different groups "
                + "of extracts.", "Sample Extracts"));
    Label info =
        new Label(
            "Extracts are individual tissue or other samples taken from organisms and used in the experiment. "
                + "You can input (optional) experimental variables, e.g. extraction times or treatments, that differ between different groups "
                + "of extracts.");
    info.setWidth("500px");
    info.setStyleName("info");
    // main.addComponent(info);

    this.tissueMap = tissueMap;
    ArrayList<String> tissues = new ArrayList<String>();
    tissues.addAll(tissueMap.keySet());
    Collections.sort(tissues);
    tissue =
        new OpenbisInfoComboBox("Tissue",
            "If different tissues are a study variables, leave this empty", tissues);
    tissue.getInnerComponent().setRequired(true);
    tissueNum =
        new OpenbisInfoTextField(
            "How many different tissue types are there in this sample extraction?", "", "50px", "2");
    tissueNum.getInnerComponent().setVisible(false);
    tissueNum.getInnerComponent().setEnabled(false);
    c =
        new ConditionsPanel(suggestions, emptyFactor, "Tissue",
            (ComboBox) tissue.getInnerComponent(), true, conditionsSet,
            (TextField) tissueNum.getInnerComponent());
    main.addComponent(c);

    main.addComponent(tissueNum.getInnerComponent());
    main.addComponent(tissue.getInnerComponent());

    extractReps =
        new OpenbisInfoTextField(
            "Extracted replicates per patient/animal/plant per experimental variable.",
            "Number of extractions per individual defined in the last step. "
                + "Technical replicates are added later!", "50px", "1");
    main.addComponent(extractReps.getInnerComponent());
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
    if (skip || tissueReady() && replicatesReady())
      return true;
    else {
      Notification n = new Notification("Please input tissue(s) and number of replicates.");
      n.setStyleName(ValoTheme.NOTIFICATION_CLOSABLE);
      n.setDelayMsec(-1);
      n.show(UI.getCurrent().getPage());
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
