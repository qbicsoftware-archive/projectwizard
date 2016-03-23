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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import uicomponents.BarcodePreviewComponent;
import uicomponents.SheetOptionComponent;

import logging.Log4j2Logger;
import main.ProjectwizardUI;
import model.ExperimentBarcodeSummaryBean;
import model.ExperimentBean;
import model.SampleToBarcodeFieldTranslator;
import model.SortBy;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import componentwrappers.CustomVisibilityComponent;
import control.BarcodeController;
import control.Functions;
import control.Functions.NotificationType;
import control.PrintReadyRunnable;

/**
 * View class for the Sample Sheet and Barcode pdf creation
 * 
 * @author Andreas Friedrich
 * 
 */
public class WizardBarcodeView extends VerticalLayout {

  logging.Logger logger = new Log4j2Logger(WizardBarcodeView.class);
  /**
   * 
   */
  private static final long serialVersionUID = 5688919972212199869L;
  private ComboBox spaceBox;
  private ComboBox projectBox;
  private Table experimentTable;
  private OptionGroup sortby;

  private Component tabsTab;
  private TabSheet tabs;

  private BarcodePreviewComponent tubePreview;

  private SheetOptionComponent sheetPreview;
  private Button prepareBarcodes;
  private Button printTubeCodes;

  private ProgressBar bar;
  private Label info;
  private Button download;

  /**
   * Creates a new component view for barcode creation
   * 
   * @param spaces List of available openBIS spaces
   * @param isAdmin
   */
  public WizardBarcodeView(List<String> spaces, boolean isAdmin) {
    SampleToBarcodeFieldTranslator translator = new SampleToBarcodeFieldTranslator();
    setSpacing(true);
    setMargin(true);

    spaceBox = new ComboBox("Project", spaces);
    spaceBox.setStyleName(ProjectwizardUI.boxTheme);
    spaceBox.setNullSelectionAllowed(false);
    spaceBox.setImmediate(true);

    projectBox = new ComboBox("Sub-Project");
    projectBox.setStyleName(ProjectwizardUI.boxTheme);
    projectBox.setEnabled(false);
    projectBox.setImmediate(true);
    projectBox.setNullSelectionAllowed(false);

    addComponent(ProjectwizardUI.questionize(spaceBox, "Name of the project", "Project Name"));
    addComponent(ProjectwizardUI.questionize(projectBox, "QBiC 5 letter project code",
        "Sub-Project"));

    experimentTable = new Table("Sample Overview");
    experimentTable.setStyleName(ValoTheme.TABLE_SMALL);
    experimentTable.setPageLength(1);
    experimentTable.setContainerDataSource(new BeanItemContainer<ExperimentBarcodeSummaryBean>(
        ExperimentBarcodeSummaryBean.class));
    experimentTable.setSelectable(true);
    experimentTable.setMultiSelect(true);
    mapCols();
    addComponent(ProjectwizardUI.questionize(experimentTable,
        "This table gives an overview of tissue samples and extracted materials"
            + " for which barcodes can be printed. You can select one or multiple rows.",
        "Sample Overview"));

    sortby = new OptionGroup("Sort Barcodes By");
    sortby.addItems(SortBy.values());
    sortby.setValue(SortBy.BARCODE_ID);
    addComponent(sortby);

    sheetPreview = new SheetOptionComponent(translator);
    tubePreview = new BarcodePreviewComponent(translator);

    tabs = new TabSheet();
    tabs.setStyleName(ValoTheme.TABSHEET_FRAMED);
    tabs.addTab(sheetPreview, "Sample Sheet");
    tabs.addTab(tubePreview, "Barcode Stickers");
    tabsTab = new CustomVisibilityComponent(tabs);
    tabsTab.setVisible(false);
    addComponent(ProjectwizardUI.questionize(tabsTab,
        "Prepare an A4 sample sheet or qr codes for sample tubes.", "Barcode Preparation"));

    info = new Label();
    bar = new ProgressBar();
    bar.setVisible(false);
    addComponent(info);
    addComponent(bar);

    prepareBarcodes = new Button("Prepare Barcodes");
    prepareBarcodes.setEnabled(false);
    addComponent(prepareBarcodes);

    printTubeCodes = new Button("Print Barcodes");
    printTubeCodes.setVisible(isAdmin);
    printTubeCodes.setEnabled(false);
    addComponent(printTubeCodes);

    download = new Button("Download");
    download.setEnabled(false);
    addComponent(download);
  }

  private void mapCols() {
    experimentTable.setColumnHeader("amount", "Samples");
    experimentTable.setColumnHeader("bio_Type", "Type");
    experimentTable.setColumnHeader("experiment", "Experiment");
  }

  public WizardBarcodeView() {
    SampleToBarcodeFieldTranslator translator = new SampleToBarcodeFieldTranslator();

    spaceBox = new ComboBox();
    projectBox = new ComboBox();

    experimentTable = new Table("Experiments");
    experimentTable.setStyleName(ValoTheme.TABLE_SMALL);
    experimentTable.setPageLength(1);
    experimentTable.setContainerDataSource(new BeanItemContainer<ExperimentBean>(
        ExperimentBean.class));
    experimentTable.setSelectable(true);
    experimentTable.setMultiSelect(true);
    mapCols();
    addComponent(experimentTable);

    tubePreview = new BarcodePreviewComponent(translator);
    addComponent(tubePreview);

    prepareBarcodes = new Button("Prepare Barcodes");
    prepareBarcodes.setEnabled(false);
    addComponent(prepareBarcodes);

    info = new Label();
    bar = new ProgressBar();
    addComponent(info);
    addComponent(bar);
  }

  // public boolean getOverwrite() {
  // return tubePreview.overwrite();
  // }

  public void enableExperiments(boolean enable) {
    experimentTable.setEnabled(enable);
  }

  public void creationPressed() {
    enableExperiments(false);
    spaceBox.setEnabled(false);
    projectBox.setEnabled(false);
    prepareBarcodes.setEnabled(false);
  }

  public void reset() {
    info.setValue("");
    download.setEnabled(false);
    spaceBox.setEnabled(true);
    projectBox.setEnabled(true);
  }

  public void resetProjects() {
    projectBox.removeAllItems();
    projectBox.setEnabled(false);
    resetExperiments();
  }

  public void resetExperiments() {
    experimentTable.setPageLength(1);
    experimentTable.removeAllItems();
    tabsTab.setVisible(false);
  }

  public String getSpaceCode() {
    return (String) spaceBox.getValue();
  }

  public String getProjectCode() {
    return (String) projectBox.getValue();
  }

  public ComboBox getSpaceBox() {
    return spaceBox;
  }

  public ComboBox getProjectBox() {
    return projectBox;
  }

  public Table getExperimentTable() {
    return experimentTable;
  }

  public void setProjectCodes(List<String> projects) {
    projectBox.addItems(projects);
    projectBox.setEnabled(true);
  }

  public void setExperiments(List<ExperimentBarcodeSummaryBean> beans) {
    BeanItemContainer<ExperimentBarcodeSummaryBean> c =
        new BeanItemContainer<ExperimentBarcodeSummaryBean>(ExperimentBarcodeSummaryBean.class);
    c.addAll(beans);
    experimentTable.setContainerDataSource(c);
    experimentTable.setPageLength(beans.size());
    if (c.size() == 1)
      experimentTable.select(c.getIdByIndex(0));
  }

  @SuppressWarnings("unchecked")
  public Collection<ExperimentBarcodeSummaryBean> getExperiments() {
    return (Collection<ExperimentBarcodeSummaryBean>) experimentTable.getValue();
  }

  public List<Button> getButtons() {
    return new ArrayList<Button>(Arrays.asList(this.prepareBarcodes, this.printTubeCodes));
  }

  public ProgressBar getProgressBar() {
    return bar;
  }

  public Label getProgressInfo() {
    return info;
  }

  public void enablePrep(boolean enable) {
    prepareBarcodes.setEnabled(enable);
    tabsTab.setVisible(enable);
  }

  public SortBy getSorter() {
    return (SortBy) sortby.getValue();
  }

  public void creationDone() {
    enableExperiments(true);
    bar.setVisible(false);
  }

  public void tubeCreationDone(int tubeCodes) {
    creationDone();
    setAvailableTubes(tubeCodes);
  }

  public void setAvailableTubes(int n) {
    printTubeCodes.setEnabled(n > 0);
    printTubeCodes.setCaption("Print Barcodes (" + n + ")");
  }

  public void sheetReady() {
    download.setEnabled(true);
  }

  public void tubesReady() {
    download.setEnabled(true);
  }

  public void resetSpace() {
    spaceBox.setValue(null);
  }

  public void disablePreview() {
    tubePreview.setVisible(false);
  }

  public void enablePreview(Sample sample) {
    tubePreview.setExample(sample);
    tubePreview.setVisible(true);
  }

  public String getCodedString(Sample s) {
    if (tabs.getSelectedTab() instanceof BarcodePreviewComponent)
      return tubePreview.getCodeString(s);
    else
      return s.getCode();
  }

  public String getInfo1(Sample s, String parents) {
    if (tabs.getSelectedTab() instanceof BarcodePreviewComponent)
      return tubePreview.getInfo1(s);
    else
      return sheetPreview.getInfo1(s, parents);
  }

  public String getInfo2(Sample s, String parents) {
    if (tabs.getSelectedTab() instanceof BarcodePreviewComponent)
      return tubePreview.getInfo2(s);
    else
      return sheetPreview.getInfo2(s, parents);
  }

  public TabSheet getTabs() {
    return tabs;
  }

  public Button getDownloadButton() {
    return download;
  }

  public List<String> getHeaders() {
    return sheetPreview.getHeaders();
  }

  public void initControl(BarcodeController barcodeController) {
    barcodeController.init(this);
  }

  public void enablePrint(boolean b) {
    this.printTubeCodes.setEnabled(b);
  }

  public void printCommandsDone(PrintReadyRunnable done) {
    if (done.wasSuccess())
      Functions.notification("Printing successful",
          "Your barcodes can be found in the printer room.", NotificationType.SUCCESS);
    else
      Functions.notification("Printing error", "There was a problem with contacting the printer.",
          NotificationType.ERROR);
  }
}
