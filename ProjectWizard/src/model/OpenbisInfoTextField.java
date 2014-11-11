package model;

import com.vaadin.ui.TextField;

@SuppressWarnings("serial")
public class OpenbisInfoTextField extends AOpenbisInfoComponent {

  public OpenbisInfoTextField(String label, String description) {
    super(description, new TextField(label));
  }

  public OpenbisInfoTextField(String label, String description, String width) {
    super(description, new TextField(label), width);
  }

  public OpenbisInfoTextField(String label, String description, String width, String value) {
    super(description, new TextField(label,value), width);
  }
}
