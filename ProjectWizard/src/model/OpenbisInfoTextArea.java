package model;

import com.vaadin.ui.TextArea;

@SuppressWarnings("serial")
public class OpenbisInfoTextArea  extends AOpenbisInfoComponent {

	public OpenbisInfoTextArea(String label, String description) {
		super(description, new TextArea(label));
	}
	
	public OpenbisInfoTextArea(String label, String description, String width, String height) {
		super(description, new TextArea(label), width);
		super.setSize(width, height);
	}
}
