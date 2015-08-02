package componentwrappers;

import java.util.List;

import main.ProjectwizardUI;

import com.vaadin.ui.ComboBox;

/**
 * Composite UI component of a ComboBox containing rollover text and other information
 * 
 * @author Andreas Friedrich
 * 
 */
public class OpenbisInfoComboBox extends AOpenbisInfoComponent {

  /**
   * 
   */
  private static final long serialVersionUID = 3322782176115907368L;

  public OpenbisInfoComboBox(String label, String description, List<String> data) {
    super(description, new ComboBox(label, data));
    inner.setStyleName(ProjectwizardUI.boxTheme);
  }
}
