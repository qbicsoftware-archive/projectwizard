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
import java.util.List;
import java.util.Map;

import logging.Log4j2Logger;
import main.ProjectwizardUI;
import main.SampleSummaryBean;
import model.ISampleBean;

import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.WizardStep;

import views.IRegistrationView;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import componentwrappers.CustomVisibilityComponent;

/**
 * Wizard Step to downloadTSV and upload the TSV file to and from and register samples and context
 * 
 * @author Andreas Friedrich
 * 
 */
public class SummaryRegisterStep implements WizardStep, IRegistrationView {

  private VerticalLayout main;
  private Button downloadTSV;
  private Button register;
  private Button downloadGraph;
  private Table summary;
  private List<List<ISampleBean>> samples;
  private Label registerInfo;
  private ProgressBar bar;
  private CustomVisibilityComponent summaryComponent;
  logging.Logger logger = new Log4j2Logger(SummaryRegisterStep.class);
  private boolean registrationComplete = false;

  public SummaryRegisterStep() {
    main = new VerticalLayout();
    main.setMargin(true);
    main.setSpacing(true);
    Label header = new Label("Sample Registration");
    main.addComponent(ProjectwizardUI.questionize(header,
        "Here you can download a spreadsheet of the samples in your experiment "
            + "and register your project in the database. "
            + "Registering samples may take a few seconds.", "Sample Registration"));

    summary = new Table("Summary");
    summary.addContainerProperty("Type", String.class, null);
    summary.addContainerProperty("Number of Samples", Integer.class, null);
    summary.setStyleName(ValoTheme.TABLE_SMALL);
    summary.setPageLength(1);

    summaryComponent =
        new CustomVisibilityComponent(ProjectwizardUI.questionize(summary,
            "This is a summary of samples for Sample Sources/Patients, Tissue Extracts and "
                + "samples that will be measured.", "Experiment Summary"));
    summaryComponent.setVisible(false);
    main.addComponent(summaryComponent.getInnerComponent());

    downloadTSV = new Button("Download Spreadsheet");
    downloadTSV.setEnabled(false);
    HorizontalLayout tsvInfo = new HorizontalLayout();
    tsvInfo.addComponent(downloadTSV);
    main.addComponent(ProjectwizardUI
        .questionize(
            tsvInfo,
            "You can download a technical spreadsheet to register your samples at a later time instead. More informative spreadsheets are available in the next step.",
            "TSV Download"));

    downloadGraph = new Button("Download Graph");
    downloadGraph.setEnabled(false);
    // main.addComponent(downloadGraph);

    register = new Button("Register All");
    register.setEnabled(false);
    main.addComponent(register);


    registerInfo = new Label();
    bar = new ProgressBar();
    main.addComponent(registerInfo);
    main.addComponent(bar);
  }

//  public void setSummary(ArrayList<SampleSummaryBean> arrayList) {
//    summaryComponent.setVisible(false);
//    BeanItemContainer<SampleSummaryBean> c =
//        new BeanItemContainer<SampleSummaryBean>(SampleSummaryBean.class);
//    c.addAll(arrayList);
//    summary.setPageLength(arrayList.size());
//    summary.setContainerDataSource(c);
//    summaryComponent.setVisible(true);
//    enableDownloads(true);
//  }

  public void setSummary(ArrayList<SampleSummaryBean> beans) {
    int i = 0;
    for(SampleSummaryBean b : beans) {
      i++;
      int amount = Integer.parseInt(b.getAmount());
      String type = "Unknown";
      String sampleType = b.getType();
      switch (sampleType) {
        case "Biological Source":
          type = "Sample Sources";
          break;
        case "Extracted Samples":
          type = "Sample Extracts";
          break;
        case "Prepared Samples":
          type = "Sample Preparations";
          break;
        default:
          break;
      }
      summary.addItem(new Object[] {type, amount}, i);
    }
    summary.setPageLength(i);
    summaryComponent.setVisible(true);
    enableDownloads(true);
  }

  public void enableDownloads(boolean enabled) {
    downloadTSV.setEnabled(enabled);
    downloadGraph.setEnabled(enabled);
  }

  public Button getDownloadButton() {
    return this.downloadTSV;
  }

  @Override
  public String getCaption() {
    return "Registration";
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    return registrationComplete();
  }

  private boolean registrationComplete() {
    return registrationComplete;
  }

  @Override
  public boolean onBack() {
    return true;
  }

  public Button getRegisterButton() {
    return this.register;
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
    logger.info("Sample registration complete!");
    Notification n =
        new Notification(
            "Registration of samples complete. Press 'next' for additional options.");
    n.setStyleName(ValoTheme.NOTIFICATION_CLOSABLE);
    n.setDelayMsec(-1);
    n.show(UI.getCurrent().getPage());
    registrationComplete = true;
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

  public Button getGraphButton() {
    return downloadGraph;
  }

}
