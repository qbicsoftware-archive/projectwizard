package views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import uicomponents.BarcodePreviewComponent;

import main.ProjectwizardUI;
import model.ExperimentBarcodeSummaryBean;
import model.ExperimentBean;
import model.SortBy;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * View class for the Sample Sheet and Barcode pdf creation
 * 
 * @author Andreas Friedrich
 * 
 */
public class WizardBarcodeView extends VerticalLayout {

  /**
   * 
   */
  private static final long serialVersionUID = 5688919972212199869L;
  private ComboBox spaceBox;
  private ComboBox projectBox;
  private Table experimentTable;
  private BarcodePreviewComponent preview;
  private Button prepareButton;
  private ProgressBar bar;
  private Label info;
  private Button sheetDownloadButton;
  private Button pdfDownloadButton;
  private OptionGroup prepSelect;
  private CheckBox overwrite;

  // private OptionGroup comparators;

  /**
   * Creates a new component view for barcode creation
   * 
   * @param spaces List of available openBIS spaces
   */
  public WizardBarcodeView(List<String> spaces) {
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
    experimentTable.setContainerDataSource(new BeanItemContainer<ExperimentBean>(
        ExperimentBean.class));
    experimentTable.setSelectable(true);
    experimentTable.setMultiSelect(true);
    addComponent(ProjectwizardUI.questionize(experimentTable,
        "This table gives an overview of tissue samples and extracted materials"
            + " for which barcodes can be printed. You can select one or multiple rows.",
        "Sample Overview"));

    prepSelect = new OptionGroup("Prepare");
    prepSelect.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
    prepSelect.addItems(Arrays.asList("Sample Sheet Barcodes", "Sample Tube Barcodes"));
    prepSelect.setMultiSelect(true);
    addComponent(ProjectwizardUI.questionize(prepSelect,
        "Prepare barcodes for the A4 sample sheet and/or qr codes for sample tubes.",
        "Barcode Preparation"));

    overwrite = new CheckBox("Overwrite existing Tube Barcode Files");
    addComponent(ProjectwizardUI.questionize(overwrite,
        "Overwrites existing files of barcode stickers. This is useful when "
            + "the design was changed after creating them.", "Overwrite Sticker Files"));

    preview = new BarcodePreviewComponent();
    addComponent(preview);

    // comparators = new OptionGroup("Sort Sheet by");
    // comparators.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
    // comparators.addItems(SortBy.values());
    // comparators.setValue(SortBy.ID);
    // addComponent(comparators);

    prepareButton = new Button("Prepare Barcodes");
    prepareButton.setEnabled(false);
    addComponent(prepareButton);

    info = new Label();
    bar = new ProgressBar();
    addComponent(info);
    addComponent(bar);

    sheetDownloadButton = new Button("Download Sheet");
    sheetDownloadButton.setEnabled(false);
    pdfDownloadButton = new Button("Download Barcodes");
    pdfDownloadButton.setEnabled(false);

    HorizontalLayout dlBox = new HorizontalLayout();
    dlBox.addComponent(sheetDownloadButton);
    dlBox.addComponent(pdfDownloadButton);
    dlBox.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
    dlBox.setSpacing(true);
    addComponent(dlBox);
  }

  public WizardBarcodeView() {
    experimentTable = new Table("Experiments");
    experimentTable.setStyleName(ValoTheme.TABLE_SMALL);
    experimentTable.setPageLength(1);
    experimentTable.setContainerDataSource(new BeanItemContainer<ExperimentBean>(
        ExperimentBean.class));
    experimentTable.setSelectable(true);
    experimentTable.setMultiSelect(true);
    addComponent(experimentTable);

    prepSelect = new OptionGroup("Prepare");
    prepSelect.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
    prepSelect.addItems(Arrays.asList("Sample Sheet Barcodes", "Sample Tube Barcodes"));
    prepSelect.setMultiSelect(true);
    addComponent(prepSelect);

    overwrite = new CheckBox("Overwrite existing Tube Barcode Files");
    addComponent(ProjectwizardUI.questionize(overwrite,
        "Overwrites existing files of barcode stickers. This is useful when "
            + "the design was changed after creating them.", "Overwrite Sticker Files"));

    preview = new BarcodePreviewComponent();
    addComponent(preview);

    prepareButton = new Button("Prepare Barcodes");
    prepareButton.setEnabled(false);
    addComponent(prepareButton);

    info = new Label();
    bar = new ProgressBar();
    addComponent(info);
    addComponent(bar);

    sheetDownloadButton = new Button("Download Sheet");
    sheetDownloadButton.setEnabled(false);
    pdfDownloadButton = new Button("Download Barcodes");
    pdfDownloadButton.setEnabled(false);

    HorizontalLayout dlBox = new HorizontalLayout();
    dlBox.addComponent(sheetDownloadButton);
    dlBox.addComponent(pdfDownloadButton);
    dlBox.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
    dlBox.setSpacing(true);
    addComponent(dlBox);
  }

  public boolean getOverwrite() {
    return overwrite.getValue();
  }

  public void enableExperiments(boolean enable) {
    experimentTable.setEnabled(enable);
  }

  public void creationPressed() {
    enableExperiments(false);
    spaceBox.setEnabled(false);
    projectBox.setEnabled(false);
    prepareButton.setEnabled(false);
    prepSelect.setEnabled(false);
  }

  public void reset() {
    sheetDownloadButton.setEnabled(false);
    pdfDownloadButton.setEnabled(false);
    spaceBox.setEnabled(true);
    projectBox.setEnabled(true);
    prepSelect.setEnabled(true);
  }

  public void resetProjects() {
    projectBox.removeAllItems();
    projectBox.setEnabled(false);
    resetExperiments();
  }

  public void resetExperiments() {
    experimentTable.setPageLength(1);
    experimentTable.removeAllItems();
    prepareButton.setEnabled(false);
    prepSelect.setEnabled(true);
    preview.setVisible(false);
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
    return new ArrayList<Button>(Arrays.asList(this.prepareButton));
  }

  public ProgressBar getProgressBar() {
    return bar;
  }

  public Label getProgressInfo() {
    return info;
  }

  public void enablePrep(boolean enable) {
    prepareButton.setEnabled(enable);
  }

  public void enableOtherButtons(boolean enable) {
    sheetDownloadButton.setEnabled(!enable);
    pdfDownloadButton.setEnabled(!enable);
  }

  public SortBy getSorter() {
    return SortBy.ID;
    // return (SortBy) comparators.getValue(); //TODO ?
  }

  public Button getButtonTube() {
    return pdfDownloadButton;
  }

  public Button getButtonSheet() {
    return sheetDownloadButton;
  }

  public OptionGroup getPrepOptionGroup() {
    return prepSelect;
  }

  public void creationDone() {
    enableExperiments(true);
  }

  public void sheetReady() {
    sheetDownloadButton.setEnabled(true);
  }

  public void tubesReady() {
    pdfDownloadButton.setEnabled(true);
  }

  public void resetSpace() {
    spaceBox.setValue(null);
  }

  public void disablePreview() {
    preview.setVisible(false);
  }

  public void enablePreview(Sample sample) {
    preview.setExample(sample);
    preview.setVisible(true);
  }

  public String getCodedString(Sample s) {
    return preview.getCodeString(s);
  }

  public String getInfo1(Sample s) {
    return preview.getInfo1(s);
  }

  public String getInfo2(Sample s) {
    return preview.getInfo2(s);
  }
}
