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
import java.util.Set;

import main.ProjectwizardUI;
import model.TestSampleInformation;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.VerticalLayout;
import componentwrappers.OpenbisInfoTextField;

public class TechChooser extends VerticalLayout {
  private static final long serialVersionUID = 7196121933289471757L;
  private ComboBox chooser;
  private OpenbisInfoTextField replicates;
  private ComboBox person;
  private CheckBox pool;
  private List<HorizontalLayout> helpers;

  /**
   * Creates a new condition chooser component
   * 
   * @param options List of different possible conditions
   * @param other Name of the "other" condition, which when selected will enable an input field for
   *        free text
   * @param special Name of a "special" condition like species for the entity input, which when
   *        selected will disable the normal species input because there is more than one instance
   * @param nullSelectionAllowed true, if the conditions may be empty
   */
  public TechChooser(List<String> options, Set<String> persons) {
    chooser = new ComboBox("Analyte", options);
    chooser.setStyleName(ProjectwizardUI.boxTheme);
    replicates = new OpenbisInfoTextField("Replicates", "", "50px", "1");
    person = new ComboBox("Contact Person", persons);
    person.setStyleName(ProjectwizardUI.boxTheme);
    pool = new CheckBox("Pool/Multiplex Samples");
    setSpacing(true);
    helpers = new ArrayList<HorizontalLayout>();
    HorizontalLayout help1 =
        ProjectwizardUI.questionize(chooser, "Choose the analyte that is measured.", "Analytes");
    addComponent(help1);
    HorizontalLayout help2 = ProjectwizardUI.questionize(replicates.getInnerComponent(),
        "Number of prepared replicates (1 means no replicates) of this analyte", "Replicates");
    addComponent(help2);
    HorizontalLayout help3 = ProjectwizardUI.questionize(person,
        "Person responsible for this part of the experiment", "Contact Person");
    addComponent(help3);
    HorizontalLayout help4 = ProjectwizardUI.questionize(pool,
        "Select if multiple samples are pooled into a single " + "sample before measurement.",
        "Pooling");
    addComponent(help4);
    helpers.add(help1);
    helpers.add(help2);
    helpers.add(help3);
    helpers.add(help4);
  }

  public boolean isSet() {
    return chooser.getItemIds().contains(chooser.getValue()) && replicates.getValue() != null;
  }

  public TestSampleInformation getChosenTechInfo() {
    String p = null;
    if (person.getValue() != null)
      p = person.getValue().toString();
    return new TestSampleInformation(chooser.getValue().toString(), pool.getValue(),
        Integer.parseInt(replicates.getValue()), p);
  }

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
    pool.setValue(false);
    chooser.setValue(chooser.getNullSelectionItemId());
  }

  public void addPoolListener(ValueChangeListener l) {
    this.pool.addValueChangeListener(l);
  }

  public void removePoolListener(ValueChangeListener poolListener) {
    this.pool.removeValueChangeListener(poolListener);
  }

  public void addProteinListener(ValueChangeListener proteinListener) {
    this.chooser.addValueChangeListener(proteinListener);
  }

  public void removeProteinListener(ValueChangeListener proteinListener) {
    this.chooser.removeValueChangeListener(proteinListener);
  }

  public boolean poolingSet() {
    return pool.getValue();
  }

  public void addMHCListener(ValueChangeListener mhcLigandListener) {
    this.chooser.addValueChangeListener(mhcLigandListener);
  }

  public void removeMHCListener(ValueChangeListener mhcLigandListener) {
    this.chooser.removeValueChangeListener(mhcLigandListener);
  }
}
