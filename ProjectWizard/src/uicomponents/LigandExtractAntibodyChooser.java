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
package uicomponents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import logging.Log4j2Logger;
import main.ProjectwizardUI;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import control.ExperimentImportController;

public class LigandExtractAntibodyChooser extends VerticalLayout {
  private static final long serialVersionUID = 7196121933289471757L;
  private ComboBox antibodyChooser;
  private TextField antiBodyMass;
  private List<HorizontalLayout> helpers;

  logging.Logger logger = new Log4j2Logger(LigandExtractAntibodyChooser.class);

  /**
   * Creates a new ligand extraction chooser component
   * 
   * @param antiBodies List of different possible antibodies
   */
  public LigandExtractAntibodyChooser(Map<String, String> antiBodies) {
    antibodyChooser = new ComboBox("Antibody", antiBodies.keySet());
    antibodyChooser.setStyleName(ProjectwizardUI.boxTheme);
    antibodyChooser.setRequired(true);
    antibodyChooser.setImmediate(true);
    antibodyChooser.setNullSelectionAllowed(false);
    antiBodyMass = new TextField("Antibody Mass [mg]");
    antiBodyMass.setWidth("60px");
    antiBodyMass.setRequired(true);
    antiBodyMass.setStyleName(ProjectwizardUI.fieldTheme);
    setSpacing(true);
    helpers = new ArrayList<HorizontalLayout>();
    HorizontalLayout help1 =
        ProjectwizardUI.questionize(antibodyChooser,
            "Choose the type of antibody used for this experiment.", "Antibody");
    addComponent(help1);
    HorizontalLayout help2 =
        ProjectwizardUI.questionize(antiBodyMass, "Mass of this antibody in milligrams.",
            "Antibody Mass");
    addComponent(help2);
    helpers.add(help1);
    helpers.add(help2);
  }

  public boolean isSet() {
    return antibodyChooser.getItemIds().contains(antibodyChooser.getValue())
        && antiBodyMass.getValue() != null;
  }

//  public AntibodyProtocolInformation getChosenAntibodyInfo() {
//    String ab = antibodyChooser.getValue().toString();
//    int mass = -1;
//    try {
//      mass = Integer.parseInt(antiBodyMass.getValue());
//    } catch (NumberFormatException e) {
//      logger.warn("User input was not a number.");
//    }
//    return new AntibodyProtocolInformation(ab, mass);
//  }

  public void showHelpers() {
    for (HorizontalLayout h : helpers)
      for (Component c : h)
        if (c instanceof PopupView)
          c.setVisible(true);
  }

  public void hideHelpers() {
    for (HorizontalLayout h : helpers)
      for (Component c : h)
        if (c instanceof PopupView)
          c.setVisible(false);
  }

  public void reset() {
    antibodyChooser.setValue(antibodyChooser.getNullSelectionItemId());
  }

  public void addProteinListener(ValueChangeListener proteinListener) {
    this.antibodyChooser.addValueChangeListener(proteinListener);
  }

  public void removeProteinListener(ValueChangeListener proteinListener) {
    this.antibodyChooser.removeValueChangeListener(proteinListener);
  }

}
