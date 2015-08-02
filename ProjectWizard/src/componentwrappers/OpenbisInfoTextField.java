package componentwrappers;

import main.ProjectwizardUI;

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.TextField;

/**
 * Composite UI component of a TextField containing rollover text and other information
 * 
 * @author Andreas Friedrich
 * 
 */
public class OpenbisInfoTextField extends AOpenbisInfoComponent {

  private static final long serialVersionUID = -7892628867973563002L;

  public OpenbisInfoTextField(String label, String description) {
    super(description, new StandardTextField(label));
    inner.setStyleName(ProjectwizardUI.fieldTheme);

  }

  public OpenbisInfoTextField(String label, String description, String width) {
    super(description, new StandardTextField(label), width);
    inner.setStyleName(ProjectwizardUI.fieldTheme);
  }

  public OpenbisInfoTextField(String label, String description, String width, String value) {
    super(description, new StandardTextField(label, value), width);
    inner.setStyleName(ProjectwizardUI.fieldTheme);
  }

  public void setValue(String s) {
    ((TextField) inner).setValue(s);
  }

  public void setMaxLength(int max) {
    ((TextField) inner).setMaxLength(max);
  }

  public void setInputPrompt(String string) {
    ((AbstractTextField) inner).setInputPrompt(string);
  }
}
