package componentwrappers;

import com.vaadin.ui.TextArea;

/**
 * Composite UI component of a TextArea containing rollover text and other information
 * @author Andreas Friedrich
 *
 */
public class OpenbisInfoTextArea  extends AOpenbisInfoComponent {

  private static final long serialVersionUID = -2810188120490576124L;

  public OpenbisInfoTextArea(String label, String description) {
		super(description, new TextArea(label));
	}
	
	public OpenbisInfoTextArea(String label, String description, String width, String height) {
		super(description, new TextArea(label), width);
		super.setSize(width, height);
	}
}
