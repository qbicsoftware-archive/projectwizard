package steps;

import java.util.List;

import main.ProjectwizardUI;
import model.TestSampleInformation;

import org.vaadin.teemu.wizards.WizardStep;

import uicomponents.TechnologiesPanel;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Wizard Step to put in information about the Sample Preparation that leads to a list of Test
 * Samples
 * 
 * @author Andreas Friedrich
 * 
 */
public class TestStep implements WizardStep {

  private VerticalLayout main;
  private TechnologiesPanel techPanel;
  private List<String> sampleTypes;
  private CheckBox noMeasure;

  /**
   * Create a new Sample Preparation step for the wizard
   * 
   * @param sampleTypes Available list of sample types, e.g. Proteins, RNA etc.
   */
  public TestStep(List<String> sampleTypes) {
    this.sampleTypes = sampleTypes;
    main = new VerticalLayout();
    main.setMargin(true);
    main.setSpacing(true);
    main.setSizeUndefined();
    Label header = new Label("Material Types");
    main.addComponent(ProjectwizardUI
        .questionize(
            header,
            "Here you can specify what kind of material is extracted from the samples for measurement, how many measurements (techn. replicates) are taken per sample "
                + "and if there is pooling for some or all of the technologies used.",
            "Material Types"));
    noMeasure = new CheckBox("No further preparation of samples?");
    main.addComponent(ProjectwizardUI.questionize(noMeasure,
        "Check if no DNA etc. is extracted and measured, for example in tissue imaging.",
        "No Sample Preparation"));
  }

  @Override
  public String getCaption() {
    return "Material Types";
  }

  public CheckBox getNotMeasured() {
    return noMeasure;
  }

  public void changeTechPanel() {
    if(noMeasure.getValue())
      techPanel.resetInputs();
    techPanel.setEnabled(!noMeasure.getValue());
  }

  @Override
  public Component getContent() {
    return main;
  }

  @Override
  public boolean onAdvance() {
    if (techPanel.isValid() || noMeasure.getValue()) {
      return true;
    } else {
      Notification n =
          new Notification(
              "Please input at least one measurement technology and the number of replicates.");
      n.setStyleName(ValoTheme.NOTIFICATION_CLOSABLE);
      n.setDelayMsec(-1);
      n.show(UI.getCurrent().getPage());
      return false;
    }
  }

  @Override
  public boolean onBack() {
    return true;
  }

  public List<TestSampleInformation> getSampleTypes() {
    return techPanel.getTechInfo();
  }

  public boolean hasPools() {
    for (TestSampleInformation info : techPanel.getTechInfo())
      if (info.isPooled())
        return true;
    return false;
  }

  public void setPoolListener(ValueChangeListener testPoolListener) {
    techPanel = new TechnologiesPanel(sampleTypes, new OptionGroup(""), testPoolListener);
    main.addComponent(techPanel);
  }
}
