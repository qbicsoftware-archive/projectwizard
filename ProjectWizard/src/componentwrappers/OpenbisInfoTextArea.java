/*******************************************************************************
 * QBiC Project Wizard enables users to create hierarchical experiments including different study conditions using factorial design.
 * Copyright (C) "2016"  Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
