package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import model.ExperimentBarcodeSummaryBean;
import model.ExperimentBean;
import model.NewSampleModelBean;
import model.SampleCodeComparator;
import model.SampleDescriptionComparator;
import model.SortBy;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class BarcodeView extends VerticalLayout {

  private ComboBox spaceBox;
  private ComboBox projectBox;
  private Table experimentTable;
  private Button prepareButton;
  private ProgressBar bar;
  private Label info;
  private Button sheetDownloadButton;
  private Button pdfDownloadButton;
  private Button resetButton;
  private OptionGroup comparators;

  private GridLayout grid;

  public BarcodeView(List<String> spaces) {
    setSpacing(true);
    setMargin(true);

    spaceBox = new ComboBox("Space Name", spaces);
    spaceBox.setNullSelectionAllowed(false);
    spaceBox.setImmediate(true);

    projectBox = new ComboBox("Project Code");
    projectBox.setEnabled(false);
    projectBox.setImmediate(true);
    projectBox.setNullSelectionAllowed(false);

    addComponent(spaceBox);
    addComponent(projectBox);

    experimentTable = new Table("Experiments");
    experimentTable.setPageLength(7);
    experimentTable.setContainerDataSource(new BeanItemContainer<ExperimentBean>(
        ExperimentBean.class));
    experimentTable.setSelectable(true);
    experimentTable.setMultiSelect(true);
    addComponent(experimentTable);
    //
    // sampleTable = new Table("Samples");
    // sampleTable.setContainerDataSource(new BeanItemContainer<NewSampleModelBean>(
    // NewSampleModelBean.class));
    // sampleTable.setSelectable(true);
    // sampleTable.setMultiSelect(true);
    // addComponent(sampleTable);

    prepareButton = new Button("Prepare Barcodes");
    prepareButton.setEnabled(false);

    // grid = new GridLayout(2, 3);
    // grid.addComponent(spaceBox, 0, 0);
    // grid.addComponent(projectBox, 0, 1);
    // grid.addComponent(experimentTable, 0, 2);
    // grid.addComponent(sampleTable, 1, 0, 1, 2);
    // grid.setSpacing(true);
    // addComponent(grid);
    addComponent(prepareButton);

    info = new Label();
    bar = new ProgressBar();
    addComponent(info);
    addComponent(bar);

    sheetDownloadButton = new Button("Download Sample Sheet");
    sheetDownloadButton.setEnabled(false);
    pdfDownloadButton = new Button("Download Tube Barcodes");
    pdfDownloadButton.setEnabled(false);
    resetButton = new Button("Reset Selection");
    resetButton.setEnabled(false);
    comparators = new OptionGroup("Sort Sheet by");
    Comparator desc = SampleDescriptionComparator.getInstance();
    Comparator id = SampleCodeComparator.getInstance();
    comparators.addItems(SortBy.values());
    addComponent(comparators);
    addComponent(sheetDownloadButton);
    addComponent(pdfDownloadButton);
    addComponent(resetButton);
  }

  public void enableExperiments(boolean enable) {
    experimentTable.setEnabled(enable);
  }

  public void creationPressed() {
    enableExperiments(false);
    spaceBox.setEnabled(false);
    projectBox.setEnabled(false);
    prepareButton.setEnabled(false);
  }

  public void creationDone() {
    enableExperiments(true);
    sheetDownloadButton.setEnabled(true);
    pdfDownloadButton.setEnabled(true);
    resetButton.setEnabled(true);
  }

  public void reset() {
    sheetDownloadButton.setEnabled(false);
    pdfDownloadButton.setEnabled(false);
    resetButton.setEnabled(false);
    spaceBox.setEnabled(true);
    projectBox.setEnabled(true);
  }

  public void resetProjects() {
    projectBox.removeAllItems();
    projectBox.setEnabled(false);
    resetExperiments();
  }

  public void resetExperiments() {
    experimentTable.removeAllItems();
    prepareButton.setEnabled(false);
    resetSamples();
  }

  public void resetSamples() {
    // sampleTable.removeAllItems();
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
    if (c.size() == 1)
      experimentTable.select(c.getIdByIndex(0));
  }

  @SuppressWarnings("unchecked")
  public Collection<ExperimentBarcodeSummaryBean> getExperiments() {
    return (Collection<ExperimentBarcodeSummaryBean>) experimentTable.getValue();
  }

  public List<Button> getButtons() {
    return new ArrayList<Button>(Arrays.asList(this.prepareButton, this.sheetDownloadButton,
        this.pdfDownloadButton, this.resetButton));
  }

  public ProgressBar getProgressBar() {
    return bar;
  }

  public Label getProgressInfo() {
    return info;
  }

  public void enableCreation(boolean enable) {
    prepareButton.setEnabled(enable);
  }

  public SortBy getSorter() {
    return (SortBy) comparators.getValue();
  }
}
