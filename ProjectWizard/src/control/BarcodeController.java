package control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.types.CommandlineJava.SysProperties;

import main.BarcodeCreator;
import main.OpenBisClient;
import model.ExperimentBarcodeSummaryBean;
import model.ExperimentBean;
import model.FileType;
import model.IBarcodeBean;
import model.NewModelBarcodeBean;
import model.NewSampleModelBean;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ui.BarcodeView;

/**
 * Controller class for the Barcode Creation Tab
 * @author Andreas Friedrich
 *
 */
public class BarcodeController {

  private BarcodeView view;
  private OpenBisClient openbis;
  private BarcodeCreator creator;

  ArrayList<IBarcodeBean> barcodeBeans;

  private List<String> barcodeExperiments = new ArrayList<String>(Arrays.asList(
      "Q_SAMPLE_EXTRACTION", "Q_SAMPLE_PREPARATION"));

  public BarcodeController(BarcodeView bw, OpenBisClient openbis, String barcodeScripts,
      String pathVar) {
    view = bw;
    this.openbis = openbis;
    creator = new BarcodeCreator(barcodeScripts, pathVar);
  }

  public void init() {
    /**
     * Button listeners
     */
    Button.ClickListener cl = new Button.ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        String src = event.getButton().getCaption();
        if (src.equals("Download Sample Sheet")) {
           creator.createAndDLSheet(barcodeBeans, view.getSorter());
        }
        if (src.equals("Download Tube Barcodes")) {
          creator.zipAndDownloadBarcodes(barcodeBeans);
        }
        if (src.equals("Reset Selection")) {
          barcodeBeans = null;
          view.reset();
        }
        if (src.equals("Prepare Barcodes")) {
          view.creationPressed();
          barcodeBeans = getSamplesFromExperimentSummaries(view.getExperiments());
          creator.findOrCreateBarcodesWithProgress(barcodeBeans, view.getProgressBar(),
              view.getProgressInfo(), new BarcodesReadyRunnable(view));
        }
      }
    };
    for (Button b : view.getButtons())
      b.addClickListener(cl);

    /**
     * Space selection listener
     */
    ValueChangeListener spaceSelectListener = new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        view.resetProjects();
        String space = view.getSpaceCode();
        if (space != null) {
          List<String> projects = new ArrayList<String>();
          for (Project p : openbis.getProjectsOfSpace(space)) {
            projects.add(p.getCode());
          }
          view.setProjectCodes(projects);
        }
      }

    };
    view.getSpaceBox().addValueChangeListener(spaceSelectListener);

    /**
     * Project selection listener
     */

    ValueChangeListener projectSelectListener = new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        view.resetExperiments();
        String project = view.getProjectCode();
        if (project != null) {
          List<ExperimentBarcodeSummaryBean> beans = new ArrayList<ExperimentBarcodeSummaryBean>();
          for (Experiment e : openbis.getExperimentsOfProjectByCode(project)) {
            String type = e.getExperimentTypeCode();
            if (barcodeExperiments.contains(type)) {
              String expCode = e.getCode();
              List<Sample> samples = openbis.getSamplesofExperiment(e.getCode());
              int numOfSamples = samples.size();
              List<String> ids = new ArrayList<String>();
              for (Sample s : samples) {
                ids.add(s.getCode());
              }
              String bioType = "unknown";
              if (type.equals(barcodeExperiments.get(0))) {
                bioType = samples.get(0).getProperties().get("Q_PRIMARY_TISSUE");
              }
              if (type.equals(barcodeExperiments.get(1))) {
                bioType = samples.get(0).getProperties().get("Q_SAMPLE_TYPE");
              }
              beans.add(new ExperimentBarcodeSummaryBean(Functions.getBarcodeRange(ids), bioType,
                  Integer.toString(numOfSamples), expCode));
            }
          }
          view.setExperiments(beans);
        }
      }

    };
    view.getProjectBox().addValueChangeListener(projectSelectListener);

    /**
     * Experiment selection listener
     */

    ValueChangeListener expSelectListener = new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        view.resetSamples();
        Collection<ExperimentBarcodeSummaryBean> exps = view.getExperiments();
        List<NewSampleModelBean> beans = new ArrayList<NewSampleModelBean>();
        if (exps.size() > 0) {
          view.enableCreation(true);
        } else {
          view.enableCreation(false);
        }
      }

    };
    view.getExperimentTable().addValueChangeListener(expSelectListener);

  }

  protected ArrayList<IBarcodeBean> getSamplesFromExperimentSummaries(
      Collection<ExperimentBarcodeSummaryBean> experiments) {
    ArrayList<NewSampleModelBean> samples = new ArrayList<NewSampleModelBean>();
    for (ExperimentBarcodeSummaryBean b : experiments) {
      for (Sample s : openbis.getSamplesofExperiment(b.getExperiment())) {
        String type = s.getSampleTypeCode();
        String bioType = "unknown";
        if (type.equals("Q_BIOLOGICAL_SAMPLE")) {
          bioType = s.getProperties().get("Q_PRIMARY_TISSUE");
        }
        if (type.equals("Q_TEST_SAMPLE")) {
          bioType = s.getProperties().get("Q_SAMPLE_TYPE");
        }
        samples.add(new NewSampleModelBean(s.getCode(), s.getProperties().get("Q_SECONDARY_NAME"),
            bioType));
      }
    }
    return translateBeans(samples);
  }

  protected ArrayList<IBarcodeBean> translateBeans(Collection<NewSampleModelBean> samples) {
    List<Sample> samplePool = openbis.getSamplesOfProject(view.getProjectCode());
    Map<String, ArrayList<String>> parentMap = openbis.getParentMap(samplePool);
    ArrayList<IBarcodeBean> res = new ArrayList<IBarcodeBean>();
    for (NewSampleModelBean s : samples) {
      res.add(new NewModelBarcodeBean(s.getCode(), s.getSecondary_Name(), s.getType(), parentMap
          .get(s.getCode())));
    }
    return res;
  }

}
