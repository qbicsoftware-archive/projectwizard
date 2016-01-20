package uicomponents;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.ComboBox;

public class LabelingMethod {

  private String name;
  private List<String> reagents;

  public LabelingMethod(String name, List<String> reagents) {
    this.name = name;
    this.reagents = reagents;
  }

  public String getName() {
    return name;
  }

  public List<String> getReagents() {
    return reagents;
  }

  @Override
  public String toString() {
    return name;
  }

}
