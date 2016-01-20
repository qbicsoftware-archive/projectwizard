package uicomponents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import main.ProjectwizardUI;

import properties.Factor;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

/**
 * Composite UI component to input values of Property instances and their units
 * 
 * @author Andreas Friedrich
 * 
 */
public class ConditionPropertyPanel extends VerticalLayout {

  private static final long serialVersionUID = 3320102983685470217L;
  String condition;
  OptionGroup type;
  TextArea values;
  ComboBox unit;

  /**
   * Create a new Condition Property Panel
   * 
   * @param condition The name of the condition selected
   * @param units An EnumSet of units (e.g. SI units)
   */
  public ConditionPropertyPanel(String condition, EnumSet<properties.Unit> units) {
    this.condition = condition;
    type = new OptionGroup("", new ArrayList<String>(Arrays.asList("Continuous", "Categorical")));
//    type = new CustomVisibilityComponent(new OptionGroup("", new ArrayList<String>(Arrays.asList("Continuous", "Categorical"))));

    values = new TextArea("Values");
    values.setWidth("300px");
    values.setInputPrompt("Please input different occurrences of the condition "+condition+",\n" +
    		"one per row.");
    values.setImmediate(true);
    values.setRequired(true);
    values.setRequiredError("Please input at least one condition.");

    unit = new ComboBox("Unit", units);
    unit.setStyleName(ProjectwizardUI.boxTheme);
    unit.setEnabled(false);
    unit.setVisible(false);
    unit.setNullSelectionAllowed(false);
    NullValidator uv =
        new NullValidator("If the condition is continuous, a unit must be selected.", false);
    unit.addValidator(uv);
    unit.setValidationVisible(false);

    initListener();
    addComponent(values);
    addComponent(ProjectwizardUI.questionize(type,"Continuous variables can be measured and have units, " +
    		"e.g. discrete time points, categorical variables don't, e.g. different genotypes.","Variable Type"));
    addComponent(unit);
    setSpacing(true);
  }

  private void initListener() {
    ValueChangeListener typeListener = new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = -6989982426500636013L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        boolean on = type != null && type.getValue().toString().equals("Continuous");
        unit.setEnabled(on);
        unit.setVisible(on);
        unit.setValidationVisible(on);
        if (!on)
          unit.select(unit.getNullSelectionItemId());
      }
    };
    type.addValueChangeListener(typeListener);
  }

  /**
   * Returns all conditions with their units as a list
   * 
   * @return
   */
  public List<Factor> getFactors() {
    List<Factor> res = new ArrayList<Factor>();
    String unitVal = "";
    if (unit.getValue() != null)
      unitVal = ((properties.Unit) unit.getValue()).getValue();
    for (String val : values.getValue().split("\n")) {
      res.add(new Factor(condition.toLowerCase(), val, unitVal));
    }
    return res;
  }

  public boolean unitSet() {
    return unit.getValue() != null;
  }

  public boolean isContinuous() {
    return type.getValue() != null && type.getValue().toString().equals("Continuous");
  }

  public ComboBox getUnitsBox() {
    return unit;
  }

  public TextArea getInputArea() {
    return values;
  }

  public OptionGroup getUnitTypeSelect() {
    return type;
  }
}
