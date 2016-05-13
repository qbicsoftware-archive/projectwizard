/*******************************************************************************
 * QBiC Project Wizard enables users to create hierarchical experiments including different study
 * conditions using factorial design. Copyright (C) "2016" Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package componentwrappers;

import java.util.List;

import main.ProjectwizardUI;

import com.vaadin.shared.ui.combobox.FilteringMode;
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
    ((ComboBox) inner).setFilteringMode(FilteringMode.CONTAINS);
  }
}
