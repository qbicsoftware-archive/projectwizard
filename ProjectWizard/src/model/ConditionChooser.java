package model;

import java.util.List;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ConditionChooser extends VerticalLayout {

  private ComboBox chooser;
  private String other;
  private TextField freetext;

  public ConditionChooser(List<String> options, String other, boolean nullSelectionAllowed) {
    chooser = new ComboBox("Condition", options);
    chooser.setImmediate(true);
    chooser.setNullSelectionAllowed(nullSelectionAllowed);
    addComponent(chooser);
  }

  public void changed() {
    if (other.equals(chooser.getValue().toString())) {
      freetext = new TextField();
      addComponent(freetext);
    } else {
      removeComponent(freetext);
    }
  }

  public boolean isSet() {
    return (chooser.getValue() != null && !chooser.getValue().toString().equals(other))
        || !freetext.getValue().isEmpty();
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
}
