package model;

import java.util.List;

import com.vaadin.ui.ComboBox;

public class OpenbisInfoComboBox extends AOpenbisInfoComponent {
	
	public OpenbisInfoComboBox(String label, String description, List<String> data) {
		super(description, new ComboBox(label, data));
	}
}

