package uicomponents;

import java.util.List;


import main.ProjectwizardUI;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.CompositeValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import componentwrappers.StandardTextField;
import control.ProjectNameValidator;

/**
 * Composite UI component to choose a single condition of an experiment
 * @author Andreas Friedrich
 *
 */
public class ConditionChooser extends VerticalLayout {

  private static final long serialVersionUID = 7196121933289471757L;
  private ComboBox chooser;
  private String other;
  private String special;
  private boolean isSpecial;
  private TextField freetext;

  /**
   * Creates a new condition chooser component
   * @param options List of different possible conditions
   * @param other Name of the "other" condition, which when selected will enable an input field for free text
   * @param special Name of a "special" condition like species for the entity input, which when selected will disable the normal species input
   * because there is more than one instance
   * @param nullSelectionAllowed true, if the conditions may be empty
   */
  public ConditionChooser(List<String> options, String other, String special,
      boolean nullSelectionAllowed) {
    isSpecial = false;
    this.other = other;
    this.special = special;
    chooser = new ComboBox("Experimental Variable", options);
    chooser.setStyleName(ProjectwizardUI.boxTheme);
    chooser.setImmediate(true);
    chooser.setNullSelectionAllowed(nullSelectionAllowed);
    addComponent(chooser);
  }

  public void addListener(ValueChangeListener l) {
    this.chooser.addValueChangeListener(l);
  }

  public boolean factorIsSpecial() {
    return isSpecial;
  }

  public void changed() {
    String val = "";
    if (chooser.getValue() != null) {
      val = chooser.getValue().toString();
      if (val.equals(other)) {
        freetext = new TextField();
        freetext.setRequired(true);
        freetext.setStyleName(ProjectwizardUI.fieldTheme);
        RegexpValidator factorLabelValidator =
            new RegexpValidator("[A-Za-z][_A-Za-z0-9]*",
                "Experimental variable must start with a letter and contain only letters, numbers or underscores ('_')");
        freetext.addValidator(factorLabelValidator);
        freetext.setImmediate(true);
        freetext.setValidationVisible(true);
        addComponent(freetext);
      } else {
        if (this.components.contains(freetext))
          removeComponent(freetext);
      }
    } else {
      if (this.components.contains(freetext))
        removeComponent(freetext);
    }
    isSpecial = val.equals(special);
  }

  public boolean chooserSet() {
    return chooser.getValue() != null;
  }

  public boolean isSet() {
    if (chooser.getValue() == null)
      return false;
    else
      return !chooser.getValue().toString().equals(other) || !freetext.getValue().isEmpty();
  }
  
  public boolean isValid() {
    return !chooser.getValue().toString().equals(other) || freetext.isValid();
  }

  public String getCondition() {
    Object val = chooser.getValue();
    if (val == null)
      return null;
    else if (val.toString().equals(other))
      return freetext.getValue();
    else
      return val.toString();
  }

  public Object getBox() {
    return chooser;
  }

  public void reset() {
    chooser.select(chooser.getNullSelectionItemId());
  }
}
