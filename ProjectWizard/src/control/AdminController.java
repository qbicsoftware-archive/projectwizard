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
package control;

import java.util.List;
import java.util.Map;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import logging.Log4j2Logger;
import main.OpenBisClient;
import views.AdminView;

@Deprecated
public class AdminController {

  private AdminView view;
  private OpenBisClient openbis;
  private Map<String, String> taxMap;
  private Map<String, String> tissueMap;
  private List<String> measureTypes;
  private List<String> spaces;
  
  logging.Logger logger = new Log4j2Logger(AdminController.class);

  public AdminController(OpenBisClient openbis, Map<String, String> taxMap,
      Map<String, String> tissueMap, List<String> sampleTypes, List<String> spaces) {
    this.taxMap = taxMap;
    this.openbis = openbis;
    this.tissueMap = tissueMap;
    this.measureTypes = sampleTypes;
    this.spaces = spaces;
//    this.view = new AdminView();
    init();
  }

  private void init() {
    Button.ClickListener cl = new Button.ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        // openbis.
        view.getSpace();
      }

    };
    view.getCreateSpace().addClickListener(cl);
  }

  public AdminView getView() {
    return view;
  }

}
