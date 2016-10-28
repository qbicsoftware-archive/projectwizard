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
package views;

import java.util.ArrayList;
import java.util.List;

import logging.Log4j2Logger;
import main.SampleSummaryBean;
import model.ISampleBean;
import uicomponents.ExperimentSummaryTable;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

public class StandaloneTSVImport extends VerticalLayout implements IRegistrationView {

  /**
   * 
   */
  private static final long serialVersionUID = 5358966181721590658L;
  private Button register;
  private Label error;
  private Upload upload;
  private ExperimentSummaryTable summary;
  private List<List<ISampleBean>> samples;
  private Label registerInfo;
  private ProgressBar bar;

  logging.Logger logger = new Log4j2Logger(StandaloneTSVImport.class);

  public StandaloneTSVImport() {
    setMargin(true);
    setSpacing(true);

//    Label info =
//        new Label("Here you can upload (edited) spreadsheet files of a previously created project. "
//            + "Registering samples may take a few seconds.");
//    info.setIcon(FontAwesome.INFO);
//    info.setStyleName("info");
//    info.setWidth("250px");
    // addComponent(info);

    summary = new ExperimentSummaryTable();
    summary.setVisible(false);
    addComponent(summary);

    error = new Label();
    error.setWidth("250px");
    error.setIcon(FontAwesome.INFO);
    error.setVisible(false);
    error.setStyleName("info");
    register = new Button("Register All");
    register.setEnabled(false);
    addComponent(register);

    registerInfo = new Label();
    bar = new ProgressBar();
    addComponent(registerInfo);
    addComponent(bar);
  }

  public void initUpload(Upload upload) {
    this.upload = upload;
    addComponent(this.upload);
    addComponent(error);
  }

  public void setError(String error) {
    this.error.setValue(error);
    this.error.setVisible(true);
  }

  public void clearError() {
    this.error.setValue("");
    this.error.setVisible(false);
  }

  public Button getRegisterButton() {
    return this.register;
  }

  public void setSummary(ArrayList<SampleSummaryBean> beans) {
    summary.setSamples(beans);
    summary.setVisible(true);
  }

  public void setProcessed(List<List<ISampleBean>> processed) {
    samples = processed;
  }

  public void setRegEnabled(boolean b) {
    register.setEnabled(b);
  }

  public List<List<ISampleBean>> getSamples() {
    return samples;
  }

  public void registrationDone() {
    logger.info("Registration complete!");
  }

  public ProgressBar getProgressBar() {
    return bar;
  }

  public Label getProgressLabel() {
    return registerInfo;
  }

  public boolean summaryIsSet() {
    return (summary.size() > 0);
  }

  public void resetSummary() {
    summary.removeAllItems();
  }

}
