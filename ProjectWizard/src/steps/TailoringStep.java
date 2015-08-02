package steps;

import java.util.List;

import main.ProjectwizardUI;
import model.AOpenbisSample;

import org.vaadin.teemu.wizards.WizardStep;

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
  private Label info;

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

    // info = new Label();
    // info.setWidth("350px");
    // info.setStyleName("info");
    // main.addComponent(info);
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

  public void setSamples(List<AOpenbisSample> samples) {
    tab.removeAllItems();
    tab.initTable(samples);
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
