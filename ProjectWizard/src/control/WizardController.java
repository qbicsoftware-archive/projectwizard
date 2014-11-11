package control;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import main.OpenBisClient;
import main.OpenbisCreationController;
import main.SamplePreparator;
import model.ExperimentBean;
import model.ISampleBean;
import model.NewSampleModelBean;
import model.OpenbisExperiment;

import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.WizardStep;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.ComboBox;

import ui.CreateDataStep;
import ui.EntityStep;
import ui.ExtractFactorStep;
import ui.ExtractionStep;
import ui.BioFactorStep;
import ui.ProjectContextStep;
import ui.TestStep;
import ui.UploadRegisterStep;

public class WizardController {

  OpenBisClient openbis;
  OpenbisCreationController openbisCreator;
  Wizard w;
  List<WizardStep> steps;
  int lastStep = 1;
  WizardDataAggregator dataAggregator;
  boolean bioFactorInstancesSet = false;
  boolean extractFactorInstancesSet = false;
  Map<String, String> taxMap;
  Map<String, String> tissueMap;
  List<String> measureTypes;
  List<String> spaces;

  public WizardController(OpenBisClient openbis, Map<String, String> taxMap,
      Map<String, String> tissueMap, List<String> sampleTypes, List<String> spaces) {
    this.openbis = openbis;
    this.openbisCreator = new OpenbisCreationController(openbis);
    this.taxMap = taxMap;
    this.tissueMap = tissueMap;
    this.measureTypes = sampleTypes;
    this.spaces = spaces;
  }

  public boolean projectHasBioEntities(String spaceCode, String code) {
    if (!openbis.projectExists(spaceCode, code))
      return false;
    for (Experiment e : openbis.getExperimentsOfProjectByCode(code)) {
      if (e.getExperimentTypeCode().equals("Q_EXPERIMENTAL_DESIGN"))
        return openbis.getSamplesofExperiment(e.getIdentifier()).size() > 0;
    }
    return false;
  }

  public boolean projectHasExtracts(String spaceCode, String code) {
    if (!openbis.projectExists(spaceCode, code))
      return false;
    for (Experiment e : openbis.getExperimentsOfProjectByCode(code)) {
      if (e.getExperimentTypeCode().equals("Q_SAMPLE_EXTRACTION"))
        return openbis.getSamplesofExperiment(e.getIdentifier()).size() > 0;
    }
    return false;
  }

  public Wizard getWizard() {
    return w;
  }

  public void init() {
    this.w = new Wizard();
    final ProjectContextStep s1 = new ProjectContextStep(spaces);
    final EntityStep s2 = new EntityStep(taxMap);
    final BioFactorStep s3 = new BioFactorStep(taxMap.keySet());
    final ExtractionStep s4 = new ExtractionStep(tissueMap);
    final ExtractFactorStep s5 = new ExtractFactorStep(tissueMap.keySet());
    final TestStep s6 = new TestStep(measureTypes);
    final CreateDataStep s7 = new CreateDataStep();
    final UploadRegisterStep s8 = new UploadRegisterStep();
    List<WizardStep> steps =
        new ArrayList<WizardStep>(Arrays.asList(s1, s2, s3, s4, s5, s6, s7, s8));

    this.dataAggregator = new WizardDataAggregator(steps, openbis, taxMap, tissueMap);
    for (WizardStep s : steps) {
      w.addStep(s);
    }

    final Uploader tsvController = new Uploader();
    Upload upload = new Upload("Upload a tsv here", tsvController);
    // Use a custom button caption instead of plain "Upload".
    upload.setButtonCaption("Upload");
    // Listen for events regarding the success of upload.
    upload.addFailedListener(tsvController);
    upload.addSucceededListener(tsvController);
    FinishedListener uploadFinListener = new FinishedListener() {
      public void uploadFinished(FinishedEvent event) {
        String error = tsvController.getError();
        File file = tsvController.getFile();
        if (error == null || error.isEmpty()) {
          s8.clearError();
          try {
            s8.setRegEnabled(false);
            SamplePreparator prep = new SamplePreparator();
            prep.processTSV(file);
            s8.setSummary(prep.getSummary());
            s8.setProcessed(prep.getProcessed());
            s8.setRegEnabled(true);
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          }
        } else {
          s8.setError(error);
          if (!file.delete())
            System.err.println("File was not deleted!");
        }
      }
    };
    upload.addFinishedListener(uploadFinListener);
    s8.initUpload(upload);

    /**
     * Button listeners
     */
    Button.ClickListener cl = new Button.ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        String src = event.getButton().getCaption();
        if (src.equals("Download TSV")) {
          FileResource resource = null;
          try {
            File tsv = dataAggregator.createTSV();
            resource = new FileResource(tsv);
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          }
          Page.getCurrent().open(resource, "Download", true);
        }
        if (src.equals("Register All")) {
          for (OpenbisExperiment e : dataAggregator.getExperiments()) {
            String space = s1.getSpaceCode();
            String proj = s1.getProjectCode();
            String type = e.getType().toString();
            String code = e.getOpenbisName();
            if (!openbis.expExists(space, proj, code))
              openbisCreator.registerExperiment(space, proj, type, code);
          }
          List<List<ISampleBean>> hierarchy = new ArrayList<List<ISampleBean>>();
          for (List<List<ISampleBean>> midList : s8.getSamples()) {
            List<ISampleBean> collect = new ArrayList<ISampleBean>();
            for (List<ISampleBean> inner : midList) {
              collect.addAll(inner);
            }
            hierarchy.add(collect);
          }
          openbisCreator.registerSampleBatchLevelWiseWithProgress(hierarchy, s8.getProgressBar(),
              s8.getProgressLabel(), new RegisteredSamplesReadyRunnable(s8));
        }
      }
    };
    s7.getDownloadButton().addClickListener(cl);
    s8.getRegisterButton().addClickListener(cl);


    /**
     * Space selection listener
     */
    ValueChangeListener spaceSelectListener = new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        s1.resetProjects();
        String space = s1.getSpaceCode();
        if (space != null) {
          List<String> projects = new ArrayList<String>();
          for (Project p : openbis.getProjectsOfSpace(space)) {
            projects.add(p.getCode());
          }
          s1.setProjectCodes(projects);
        }
      }

    };
    s1.getSpaceBox().addValueChangeListener(spaceSelectListener);

    /**
     * Project selection listener
     */

    ValueChangeListener projectSelectListener = new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        s1.resetExperiments();
        String space = s1.getSpaceCode();
        String project = s1.getProjectCode();
        if (project != null) {
          s1.enableExtractContextOption(projectHasBioEntities(space, project));
          s1.enableMeasureContextOption(projectHasExtracts(space, project));
          List<ExperimentBean> beans = new ArrayList<ExperimentBean>();
          for (Experiment e : openbis.getExperimentsOfProjectByCode(project)) {
            int numOfSamples = openbis.getSamplesofExperiment(e.getCode()).size();
            beans.add(new ExperimentBean(e.getCode(), e.getExperimentTypeCode(), Integer
                .toString(numOfSamples)));
          }
          s1.setExperiments(beans);
        }
      }

    };
    s1.getProjectBox().addValueChangeListener(projectSelectListener);

    /**
     * Experiment selection listener
     */

    ValueChangeListener expSelectListener = new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        s1.resetSamples();
        ExperimentBean exp = s1.getExperimentName();
        if (exp != null) {
          String code = exp.getCode();
          List<NewSampleModelBean> beans = new ArrayList<NewSampleModelBean>();
          for (Sample s : openbis.getSamplesofExperiment(code)) {
            beans.add(new NewSampleModelBean(s.getCode(),
                s.getProperties().get("Q_SECONDARY_NAME"), s.getSampleTypeCode()));
          }
          s1.setSamples(beans);
        }
      }

    };
    s1.getExperimentTable().addValueChangeListener(expSelectListener);

    /**
     * Project context (radio buttons) listener
     */

    ValueChangeListener projectContextListener = new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        if (s1.getProjectContext().getValue() != null)
          s1.showExperiments();
      }
    };
    s1.getProjectContext().addValueChangeListener(projectContextListener);

    // changes to bio factor selection box
    ValueChangeListener entityStepListener = new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        ComboBox source = (ComboBox) event.getProperty();
        s2.enableOtherField(source, s2.factorFieldOther(source));
        s2.enableSpeciesField(!s2.speciesIsFactor());
      }

    };
    for (ComboBox b : s2.getBoxFactors())
      b.addValueChangeListener(entityStepListener);

    // changes to extraction factor selection box
    ValueChangeListener extractionStepListener = new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        ComboBox source = (ComboBox) event.getProperty();
        s4.enableOtherField(source, s4.factorFieldOther(source));
        s4.enableTissueField(!s4.tissueIsFactor());
      }

    };
    for (ComboBox b : s4.getBoxFactors())
      b.addValueChangeListener(extractionStepListener);

    WizardProgressListener wl = new WizardProgressListener() {
      @Override
      public void wizardCompleted(WizardCompletedEvent event) {}

      @Override
      public void wizardCancelled(WizardCancelledEvent event) {}

      @Override
      public void stepSetChanged(WizardStepSetChangedEvent event) {

      }

      @Override
      public void activeStepChanged(WizardStepActivationEvent event) {
        if (event.getActivatedStep().equals(s1)) {
          lastStep = 1;
          System.out.println("step1");
        }
        // Entity Setup Step
        if (event.getActivatedStep().equals(s2)) {
          int last = lastStep;
          lastStep = 2;
          System.out.println("step2");
          List<String> options = s1.getContextOptions();
          String context = (String) s1.getProjectContext().getValue();
          if (options.get(1).equals(context)) {
            dataAggregator.setInheritEntities(true);
            dataAggregator.setInheritExtracts(false);
            s2.setSkipStep(true);
            s3.setSkipStep(true);
            s4.setSkipStep(false);
            s5.setSkipStep(false);
          } else if (options.get(2).equals(context)) {
            dataAggregator.setInheritExtracts(true);
            dataAggregator.setInheritEntities(false);
            s2.setSkipStep(true);
            s3.setSkipStep(true);
            s4.setSkipStep(true);
            s5.setSkipStep(true);
          } else {
            dataAggregator.setInheritEntities(false);
            dataAggregator.setInheritExtracts(false);
            s2.setSkipStep(false);
            s3.setSkipStep(false);
            s4.setSkipStep(false);
            s5.setSkipStep(false);
          }
          if (s2.isSkipped()) {
            if (last > 2)
              w.back();
            else
              w.next();
          } else {
            s3.resetFactorFields();
            bioFactorInstancesSet = false;
          }
        }
        // Entity Factor Instances Step
        if (event.getActivatedStep().equals(s3)) {
          System.out.println("step3");
          int last = lastStep;
          lastStep = 3;
          // if (!bioFactorInstancesSet && !s2.speciesIsFactor())
          // s3.setSkipStep(true);
          // else
          // s3.setSkipStep(false);
          if (s3.isSkipped()) {
            if (last > 3)
              w.back();
            else
              w.next();
          } else {
            if (!bioFactorInstancesSet) {
              if (s2.speciesIsFactor())
                s3.initSpeciesFactorField(s2.getSpeciesAmount());
              s3.initFactorFields(s2.getFactors());
              bioFactorInstancesSet = true;
            }
          }
        }
        // Extract Setup Step
        if (event.getActivatedStep().equals(s4)) {
          System.out.println("step4");
          int last = lastStep;
          lastStep = 4;
          if (s4.isSkipped()) {
            if (last > 4)
              w.back();
            else
              w.next();
          } else {
            s5.resetFactorFields();
            extractFactorInstancesSet = false;
          }
        }
        // Extract Factor Instances Step
        if (event.getActivatedStep().equals(s5)) {
          System.out.println("step5");
          int last = lastStep;
          lastStep = 5;
          // if (!extractFactorInstancesSet && !s4.tissueIsFactor())
          // s5.setSkipStep(true);
          // else
          // s5.setSkipStep(false);
          if (s5.isSkipped()) {
            if (last > 5)
              w.back();
            else
              w.next();
          } else {
            if (!extractFactorInstancesSet) {
              if (s4.tissueIsFactor())
                s5.initTissueFactorField(s4.getTissueAmount());
              s5.initFactorFields(s4.getFactors());
              extractFactorInstancesSet = true;
            }
          }
        }
        // Test Setup Step
        if (event.getActivatedStep().equals(s6)) {
          int last = lastStep;
          lastStep = 6;
        }
        // TSV Download Step
        if (event.getActivatedStep().equals(s7)) {
          int last = lastStep;
          lastStep = 7;
          dataAggregator.prepareData();
        }
      }
    };
    w.addListener(wl);
  }
}
