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
package incubator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.AOpenbisSample;

import org.vaadin.teemu.wizards.WizardStep;

import uicomponents.DragDropPoolComponent;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import incubator.NewController.Steps;

public class PoolingStep implements WizardStep {

  private VerticalLayout main;
  private Steps type;
  private Label info;
  private DragDropPoolComponent pooling;
  private List<DragDropPoolComponent> poolings;
  private TabSheet instances;

  public PoolingStep(Steps poolStep) {
    instances = new TabSheet();
    instances.setStyleName(ValoTheme.TABSHEET_FRAMED);
    poolings = new ArrayList<DragDropPoolComponent>();
    this.type = poolStep;
    main = new VerticalLayout();
    main.setSpacing(true);
    main.setMargin(true);
//    info =
//        new Label("Drag and Drop Samples that should be part of the pooling sample. "
//            + "You can add additional pools if needed.");
//    info.setStyleName("info");
//    info.setWidth("350px");
//    main.addComponent(info);
    pooling = new DragDropPoolComponent(getPoolPrefix(poolStep));
    main.addComponent(instances);
  }

  private String getPoolPrefix(Steps poolStep) {
    String name;
    switch (type) {
      case Extract_Pooling:
        name = "Extr-";
        break;
      case Test_Sample_Pooling:
        name = "Prep-";
        break;
      default:
        name = "";
        break;
    }
    return name;
  }

  @Override
  public String getCaption() {
    String name;
    switch (type) {
      case Extract_Pooling:
        name = "Extr. Pooling";
        break;
      case Test_Sample_Pooling:
        name = "Prep. Pooling";
        break;
      default:
        name = "";
        break;
    }
    return name;
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    if (pooling.getPools().isEmpty()) {
      Notification n =
          new Notification(
              "Please create at least one pool containing samples or uncheck pooling in the previous step.");
      n.setStyleName(ValoTheme.NOTIFICATION_CLOSABLE);
      n.setDelayMsec(-1);
      n.show(UI.getCurrent().getPage());
      return false;
    } else
      return true;
  }

  @Override
  public boolean onBack() {
    resetStep();
    return true;
  }

  public void setSamples(List<List<AOpenbisSample>> sampleGroups, Steps testSamplePooling) {
    for (List<AOpenbisSample> group : sampleGroups) {
      String type = group.get(0).getValueMap().get("Q_SAMPLE_TYPE");
      pooling = new DragDropPoolComponent(getPoolPrefix(testSamplePooling));
      pooling.initConditionsAndSetSamples(group);
      poolings.add(pooling);
      instances.addTab(pooling, type);
    }
  }

  public Map<String, List<AOpenbisSample>> getPools() {
    Map<String, List<AOpenbisSample>> res = new HashMap<String, List<AOpenbisSample>>();
    for (DragDropPoolComponent pooling : poolings) {
      res.putAll(pooling.getPools());
    }
    return res;
  }

  public void resetStep() {
    poolings = new ArrayList<DragDropPoolComponent>();
    main.removeComponent(instances);
    instances = new TabSheet();
    main.addComponent(instances);
  }

  public Object getPoolingSamples() {
    // TODO Auto-generated method stub
    return null;
  }

}
