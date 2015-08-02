package uicomponents;

import java.util.ArrayList;
import java.util.List;

import main.ProjectwizardUI;
import model.TestSampleInformation;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.VerticalLayout;
import componentwrappers.OpenbisInfoTextField;

public class TechChooser extends VerticalLayout {
  private static final long serialVersionUID = 7196121933289471757L;
  private ComboBox chooser;
  private OpenbisInfoTextField replicates;
  private CheckBox pool;
  private List<HorizontalLayout> helpers;

  /**
   * Creates a new condition chooser component
   * 
   * @param options List of different possible conditions
   * @param other Name of the "other" condition, which when selected will enable an input field for
   *        free text
   * @param special Name of a "special" condition like species for the entity input, which when
   *        selected will disable the normal species input because there is more than one instance
   * @param nullSelectionAllowed true, if the conditions may be empty
   */
  public TechChooser(List<String> options) {
    chooser = new ComboBox("Measurement Type", options);
    chooser.setStyleName(ProjectwizardUI.boxTheme);
    replicates = new OpenbisInfoTextField("Techn. Replicates", "", "50px", "1");
    pool = new CheckBox("Pool Samples");
    setSpacing(true);
    helpers = new ArrayList<HorizontalLayout>();
    HorizontalLayout help1 =
        ProjectwizardUI.questionize(chooser, "Choose the material that is measured.",
            "Measurement Types");
    addComponent(help1);
    HorizontalLayout help2 =
        ProjectwizardUI.questionize(replicates.getInnerComponent(),
            "Number of technical replicates (1 means no replicates)", "Replicates");
    addComponent(help2);
    HorizontalLayout help3 =
        ProjectwizardUI.questionize(pool, "Select if multiple samples are pooled into a single "
            + "sample before measurement.", "Pooling");
    addComponent(help3);
    helpers.add(help1);
    helpers.add(help2);
    helpers.add(help3);
  }

  public void addPoolListener(ValueChangeListener l) {
    this.pool.addValueChangeListener(l);
  }

  public boolean chooserSet() {
    return chooser.getValue() != null;
  }

  public boolean isSet() {
    return chooser.getItemIds().contains(chooser.getValue()) && replicates.getValue() != null;
  }

  public TestSampleInformation getChosenTechInfo() {
    return new TestSampleInformation(chooser.getValue().toString(), pool.getValue(),
        Integer.parseInt(replicates.getValue()));
  }

  public void showHelpers() {
    for (HorizontalLayout h : helpers)
      for (Component c : h)
        if (c instanceof PopupView)
          c.setVisible(true);
  }

  public void hideHelpers() {
    for (HorizontalLayout h : helpers)
      for (Component c : h)
        if (c instanceof PopupView)
          c.setVisible(false);
  }

  public void reset() {
    pool.setValue(false);
    chooser.setValue(chooser.getNullSelectionItemId());
  }

  public void removePoolListener(ValueChangeListener poolListener) {
    this.pool.removeValueChangeListener(poolListener);
  }
}
