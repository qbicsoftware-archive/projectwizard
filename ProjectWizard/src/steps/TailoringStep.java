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
package steps;


import java.util.List;

import main.ProjectwizardUI;
import model.AOpenbisSample;

import org.vaadin.teemu.wizards.WizardStep;

import uicomponents.LabelingMethod;
import uicomponents.SummaryTable;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Wizard Step that shows a SummaryTable of the prepared samples and can be used to edit and delete
 * samples
 * 
 * @author Andreas Friedrich
 * 
 */
public class TailoringStep implements WizardStep {

  boolean skip = false;

  private VerticalLayout main;
  private SummaryTable tab;

  // Pooling
  private CheckBox poolSelect;

  /**
   * Create a new Experiment Tailoring step
   * 
   * @param name Title of this step
   */
  public TailoringStep(String name, boolean pooling) {
    main = new VerticalLayout();
    main.setSpacing(true);
    main.setMargin(true);
    Label header = new Label(name + " Tailoring");
    main.addComponent(ProjectwizardUI.questionize(header, "Here you can delete " + name
        + " that are not part of the"
        + " experiment. You can change the secondary name to something"
        + " more intuitive - experimental variables will be saved in additional columns.", name
        + " Tailoring"));

    if (pooling)
      initPooling(name);
    tab = new SummaryTable("Samples");
    tab.setVisible(false);
  }

  private void initPooling(String name) {
    poolSelect = new CheckBox();
    poolSelect.setCaption("Pool " + name);
    main.addComponent(ProjectwizardUI.questionize(poolSelect,
        "Select if multiple tissue extracts are pooled into a single sample "
            + "before measurement.", "Pooling"));
  }

  public void setSamples(List<AOpenbisSample> samples, LabelingMethod labelingMethod) {
    tab.removeAllItems();
    tab.initTable(samples, labelingMethod);
    tab.setVisible(true);
    tab.setPageLength(samples.size());
    main.addComponent(tab);
  }

  public List<AOpenbisSample> getSamples() {
    return tab.getSamples();
  }

  @Override
  public String getCaption() {
    return "Summary";
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    return skip || true;
  }

  @Override
  public boolean onBack() {
    return true;
  }

  public void setSkipStep(boolean b) {
    skip = b;
  }

  public boolean isSkipped() {
    return skip;
  }

  public CheckBox getPoolBox() {
    return poolSelect;
  }

  public boolean pool() {
    return poolSelect != null && poolSelect.getValue();
  }
}
