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
package incubator;


import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.JAXBException;

import logging.Log4j2Logger;
import main.OpenBisClient;
import main.OpenbisCreationController;
import main.SamplePreparator;
import model.AOpenbisSample;
import model.AttachmentConfig;
import model.ExperimentBean;
import model.ExperimentType;
import model.NewSampleModelBean;

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

import com.vaadin.data.Validator;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.CompositeValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.ValoTheme;

import control.SampleNameValidator;

import incubator.ConditionInstanceStep;
import incubator.EntityStep;
import incubator.FinishStep;
import incubator.PoolingStep;
import incubator.ProjectContextStep;
import incubator.TailoringStep;
import incubator.TestStep;
import incubator.SummaryRegisterStep;
import io.DBVocabularies;
import uicomponents.ProjectInformationComponent;
import processes.AttachmentMover;
import processes.RegisteredSamplesReadyRunnable;
import properties.Factor;
import steps.ExtractionStep;

/**
 * Controller for the sample/experiment creation wizard
 * 
 * @author Andreas Friedrich
 * 
 */
public class NewController {

  private OpenBisClient openbis;
  private DBVocabularies vocabs;
  private OpenbisCreationController openbisCreator;
  private Wizard w;
  private Map<Steps, WizardStep> steps;
  private NewAggregator dataAggregator;
  private boolean bioFactorInstancesSet = false;
  private boolean extractFactorInstancesSet = false;
  private boolean extractPoolsSet = false;
  private boolean testPoolsSet = false;
  private FileDownloader tsvDL;
  private FileDownloader graphDL;
  SamplePreparator prep = new SamplePreparator();

  logging.Logger logger = new Log4j2Logger(NewController.class);

  private AttachmentConfig attachConfig;

  private boolean inheritEntities;
  private boolean inheritExtracts;

  /**
   * 
   * @param openbis OpenBisClient API
   * @param taxMap Map containing the NCBI taxonomy (labels and ids) taken from openBIS
   * @param tissueMap Map containing the tissue
   * @param sampleTypes List containing the different sample (technology) types
   * @param spaces List of space names existing in openBIS
   * @param dataMoverFolder for attachment upload
   * @param uploadSize
   */
  public NewController(OpenBisClient openbis, DBVocabularies vocabs,
      AttachmentConfig attachmentConfig) {
    this.openbis = openbis;
    this.openbisCreator = new OpenbisCreationController(openbis);
    this.vocabs = vocabs;
    this.attachConfig = attachmentConfig;
  }

  // Functions to add steps to the wizard depending on context
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  private void setUpLoadStep() {
    w.addStep(steps.get(Steps.Registration)); // tsv upload and registration
  }

  private void setInheritEntities() {
    w.addStep(steps.get(Steps.Entity_Tailoring)); // entity negative selection
    w.addStep(steps.get(Steps.Extraction)); // extract first step
    setInheritExtracts();
  }

  private void setInheritExtracts() {
    w.addStep(steps.get(Steps.Extract_Tailoring)); // extracts negative selection
    w.addStep(steps.get(Steps.Test_Samples)); // test samples first step
    setUpLoadStep();
  }

  private void setExtractsPooling() {
    w.addStep(steps.get(Steps.Extract_Pooling)); // pooling step
    setTestStep();
  }

  private void setTestStep() {
    w.addStep(steps.get(Steps.Test_Samples)); // test samples first step
    setUpLoadStep();
  }

  private void setTestsPooling() {
    w.addStep(steps.get(Steps.Test_Sample_Pooling));
    setUpLoadStep();
  }

  private void setCreateEntities() {
    w.addStep(steps.get(Steps.Entities)); // entities first step
    setInheritEntities();
  }

  private void setEntityConditions() {
    w.addStep(steps.get(Steps.Entity_Conditions)); // entity conditions
    setInheritEntities();
  }

  private void setExtractConditions() {
    w.addStep(steps.get(Steps.Extract_Conditions)); // extract conditions
    setInheritExtracts();
  }

  private void resetNextSteps() {
    // Notification n =
    // new Notification(
    // "Steps updated",
    // "One of your choices has added or removed steps from the wizard. You can see your progress at
    // the top.");
    // n.setStyleName(ValoTheme.NOTIFICATION_CLOSABLE);
    // n.setDelayMsec(-1);
    List<WizardStep> steps = w.getSteps();
    List<WizardStep> copy = new ArrayList<WizardStep>();
    copy.addAll(steps);
    boolean isNew = false;
    for (int i = 0; i < copy.size(); i++) {
      WizardStep cur = copy.get(i);
      if (isNew) {
        w.removeStep(cur);
      }
      if (w.isActive(cur))
        isNew = true;
    }
  }

  public Wizard getWizard() {
    return w;
  }

  public static enum Steps {
    Project_Context, Entities, Entity_Conditions, Entity_Tailoring, Extraction, Extract_Conditions, Extract_Tailoring, Extract_Pooling, Test_Samples, Test_Sample_Pooling, Registration, Finish;
  }

  /**
   * Initialize all possible steps in the wizard and the listeners used
   */
  public void init(final String user) {
    this.w = new Wizard();
    w.getFinishButton().setVisible(false);
    w.getFinishButton().setStyleName(ValoTheme.BUTTON_DANGER);
    w.getCancelButton().setStyleName(ValoTheme.BUTTON_DANGER);

    final ProjectContextStep contextStep =
        new ProjectContextStep(openbis, vocabs.getSpaces(), vocabs.getPeople().keySet());
    final EntityStep entStep = new EntityStep(vocabs.getTaxMap());
    final ConditionInstanceStep entCondInstStep =
        new ConditionInstanceStep(vocabs.getTaxMap().keySet(), "Species", "Biol. Variables");
    final TailoringStep negStep1 = new TailoringStep("Sample Sources", false);
    final ExtractionStep extrStep = new ExtractionStep(vocabs.getTissueMap(),
        vocabs.getCellLinesMap(), vocabs.getPeople().keySet());
    final ConditionInstanceStep extrCondInstStep =
        new ConditionInstanceStep(vocabs.getTissueMap().keySet(), "Tissues", "Extr. Variables");
    final TailoringStep negStep2 = new TailoringStep("Sample Extracts", true);
    final TestStep techStep = new TestStep(vocabs);
    final SummaryRegisterStep regStep = new SummaryRegisterStep();
    final PoolingStep poolStep1 = new PoolingStep(Steps.Extract_Pooling);
    final PoolingStep poolStep2 = new PoolingStep(Steps.Test_Sample_Pooling);
    final FinishStep finishStep = new FinishStep(w, attachConfig);

    steps = new HashMap<Steps, WizardStep>();
    steps.put(Steps.Project_Context, contextStep);
    steps.put(Steps.Entities, entStep);
    steps.put(Steps.Entity_Conditions, entCondInstStep);
    steps.put(Steps.Entity_Tailoring, negStep1);
    steps.put(Steps.Extraction, extrStep);
    steps.put(Steps.Extract_Conditions, extrCondInstStep);
    steps.put(Steps.Extract_Tailoring, negStep2);
    steps.put(Steps.Extract_Pooling, poolStep1);
    steps.put(Steps.Test_Samples, techStep);
    steps.put(Steps.Test_Sample_Pooling, poolStep2);
    steps.put(Steps.Registration, regStep);
    steps.put(Steps.Finish, finishStep);

    // this.dataAggregator = new NewAggregator(steps, openbis, taxMap, tissueMap);
    this.dataAggregator = new NewAggregator();
    w.addStep(contextStep);

    Button.ClickListener cl = new Button.ClickListener() {
      /**
       * 
       */
      private static final long serialVersionUID = -8427457552926464653L;

      @Override
      public void buttonClick(ClickEvent event) {
        String src = event.getButton().getCaption();
        if (src.equals("Register All")) {
          regStep.getRegisterButton().setEnabled(false);
          ProjectContextStep context = (ProjectContextStep) steps.get(Steps.Project_Context);
          String desc = context.getDescription();
          String expSecondaryName = context.getExpSecondaryName();
          // TODO
          openbisCreator.registerProjectWithExperimentsAndSamplesBatchWise(regStep.getSamples(),
              desc, expSecondaryName, null, null, regStep.getProgressBar(),
              regStep.getProgressLabel(), new RegisteredSamplesReadyRunnable(regStep), user);
          w.addStep(steps.get(Steps.Finish));
        }
      }
    };
    regStep.getDownloadButton().addClickListener(cl);
    regStep.getRegisterButton().addClickListener(cl);

    /**
     * Project context (radio buttons) listener
     */

    ValueChangeListener projectContextListener = new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 5972535836592118817L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        // contextStep.enableExpName(false);
        if (contextStep.getProjectContext().getValue() != null) {
          resetNextSteps();
          OptionGroup projectContext = contextStep.getProjectContext();
          List<String> contextOptions = contextStep.getContextOptions();
          List<ExperimentBean> experiments = contextStep.getExperiments();
          String context = (String) projectContext.getValue();
          List<ExperimentBean> beans = new ArrayList<ExperimentBean>();
          // inherit from bio entities
          if (contextOptions.get(1).equals(context)) {
            for (ExperimentBean b : experiments) {
              if (b.getExperiment_type().equals(ExperimentType.Q_EXPERIMENTAL_DESIGN.toString()))
                beans.add(b);
            }
            setInheritEntities();
            setInheritEntities(true);
            setInheritExtracts(false);
          }
          // inherit from sample extraction
          if (contextOptions.get(2).equals(context)) {
            for (ExperimentBean b : experiments) {
              if (b.getExperiment_type().equals(ExperimentType.Q_SAMPLE_EXTRACTION.toString()))
                beans.add(b);
            }
            setInheritExtracts();
            setInheritEntities(false);
            setInheritExtracts(true);
          }
          // new context
          if (contextOptions.get(0).equals(context)) {
            setCreateEntities();
            setInheritEntities(false);
            setInheritExtracts(false);
            contextStep.hideExperiments();
            // contextStep.enableExpName(true);
          }
          // copy context
          // if (contextOptions.get(3).equals(context)) {
          // beans.addAll(context);
          // setUpLoadStep();
          // }
          // read only tsv creation
          if (contextOptions.get(3).equals(context)) {
            for (ExperimentBean b : experiments) {
              if (b.getExperiment_type().equals(ExperimentType.Q_EXPERIMENTAL_DESIGN.toString()))
                beans.add(b);
            }
            setUpLoadStep();
          }
          if (beans.size() > 0)
            contextStep.showExperiments(beans);
        }
      }
    };
    contextStep.getProjectContext().addValueChangeListener(projectContextListener);

    /**
     * Listeners for pooling samples
     */
    ValueChangeListener poolingListener = new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 2393762547426343668L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        resetNextSteps();
        if (negStep2.pool()) {
          setExtractsPooling();
        } else {
          setTestStep();
        }
      }
    };
    negStep2.getPoolBox().addValueChangeListener(poolingListener);

    ValueChangeListener testPoolListener = new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 2393762547426343668L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        resetNextSteps();
        if (techStep.hasPools()) {
          setTestsPooling();
        } else {
          setUpLoadStep();
        }
      }
    };
    techStep.initTestStep(testPoolListener);

    /**
     * Listeners for entity and extract conditions
     */
    ValueChangeListener entityConditionSetListener = new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 2393762547426343668L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        resetNextSteps();
        if (entStep.isConditionsSet().getValue() != null) {
          setEntityConditions();
        } else {
          setInheritEntities();
        }
      }
    };
    entStep.isConditionsSet().addValueChangeListener(entityConditionSetListener);

    ValueChangeListener extractConditionSetListener = new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 4879458823482873630L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        resetNextSteps();
        if (extrStep.conditionsSet().getValue() != null) {
          setExtractConditions();
        } else {
          setInheritExtracts();
        }
      }
    };
    extrStep.conditionsSet().addValueChangeListener(extractConditionSetListener);

    WizardProgressListener wl = new WizardProgressListener() {
      @Override
      public void wizardCompleted(WizardCompletedEvent event) {}

      @Override
      public void wizardCancelled(WizardCancelledEvent event) {}

      @Override
      public void stepSetChanged(WizardStepSetChangedEvent event) {}

      /**
       * Reactions to step changes in the wizard
       */
      @Override
      public void activeStepChanged(WizardStepActivationEvent event) {
        // Context Step
        if (event.getActivatedStep().equals(contextStep)) {
          // contextStep.allowNext(false);
          regStep.enableDownloads(false);
        }
        // Entity Setup Step
        if (event.getActivatedStep().equals(entStep)) {
          bioFactorInstancesSet = false;
          // }
        }
        // Entity Condition Instances Step
        if (event.getActivatedStep().equals(entCondInstStep)) {
          reloadConditionsPreviewTable(entCondInstStep, Integer.toString(entStep.getBioRepAmount()),
              new ArrayList<AOpenbisSample>());
          if (!bioFactorInstancesSet) {
            if (entStep.speciesIsFactor())
              entCondInstStep.initOptionsFactorField(entStep.getSpeciesAmount());
            entCondInstStep.initFactorFields(entStep.getFactors());
            initConditionListener(entCondInstStep, Integer.toString(entStep.getBioRepAmount()),
                new ArrayList<AOpenbisSample>());
            bioFactorInstancesSet = true;
          }
        }
        // Summary and Deletion of Entities
        if (event.getActivatedStep().equals(negStep1)) {
          negStep1.setSamples(createSamplesFromPreselection(entCondInstStep.getPreSelection()),
              null);
        }
        // Extract Setup Step
        if (event.getActivatedStep().equals(extrStep)) {
          dataAggregator.setEntities(negStep1.getSamples());
          extractFactorInstancesSet = false;
        }
        // Extract Factor Instances Step
        if (event.getActivatedStep().equals(extrCondInstStep)) {
          reloadConditionsPreviewTable(extrCondInstStep,
              Integer.toString(extrStep.getExtractRepAmount()), dataAggregator.getSampleLevel(0));
          if (!extractFactorInstancesSet) {
            if (extrStep.tissueIsFactor())
              extrCondInstStep.initOptionsFactorField(extrStep.getTissueAmount());
            extrCondInstStep.initFactorFields(extrStep.getFactors());
            initConditionListener(extrCondInstStep,
                Integer.toString(extrStep.getExtractRepAmount()), dataAggregator.getSampleLevel(0));
            extractFactorInstancesSet = true;
          }
        }
        // Summary and Deletion of Extracts
        if (event.getActivatedStep().equals(negStep2)) {
          extractPoolsSet = false;
          negStep2.setSamples(createSamplesFromPreselection(extrCondInstStep.getPreSelection()),
              extrStep.getLabelingMethod());
        }
        // Extract Pool Step
        if (event.getActivatedStep().equals(poolStep1)) {
          dataAggregator.resetExtracts(); // TODO needed?
          if (!extractPoolsSet) {
            poolStep1.setSamples(
                new ArrayList<List<AOpenbisSample>>(Arrays.asList(negStep2.getSamples())),
                Steps.Extract_Pooling);
            extractPoolsSet = true;
          }
        }
        // Test Setup Step
        if (event.getActivatedStep().equals(techStep)) {
          testPoolsSet = false;
          List<AOpenbisSample> all = new ArrayList<AOpenbisSample>();
          all.addAll(negStep2.getSamples()); // TODO needed
          dataAggregator.setSampleLevel(poolStep1.getPoolingSamples(), 4);
          dataAggregator.setExtracts(all);
        }
        // Test Pool Step
        if (event.getActivatedStep().equals(poolStep2)) {
          dataAggregator.resetTests();
          if (!testPoolsSet) {
            poolStep2.setSamples(dataAggregator.prepareTestSamples(), Steps.Test_Sample_Pooling);
            testPoolsSet = true;
          }
        }
        // TSV and Registration Step
        if (event.getActivatedStep().equals(regStep)) {
          regStep.enableDownloads(false);
          // Test samples were filled out
          if (w.getSteps().contains(steps.get(Steps.Test_Samples))) {
            if (!testPoolsSet)
              dataAggregator.prepareTestSamples();
            List<AOpenbisSample> all = new ArrayList<AOpenbisSample>();
            all.addAll(dataAggregator.getTests());
            all.addAll(dataAggregator.createPoolingSamples(poolStep2.getPools()));
            dataAggregator.setTests(all);
            createTSV();
            try {
              prep.processTSV(dataAggregator.getTSV(), false);
            } catch (IOException e) {
              e.printStackTrace();
            }
            armDownloadButtons(regStep.getDownloadButton(), regStep.getGraphButton());
            regStep.setSummary(prep.getSummary());
            regStep.setProcessed(prep.getProcessed());
          }
          if (regStep.summaryIsSet()) {
            regStep.setRegEnabled(true);
          }
          // Write TSV mode
          if (contextStep.fetchTSVModeSet()) {
            dataAggregator.parseAll();
            createTSV();
            try {
              prep.processTSV(dataAggregator.getTSV(), false);
            } catch (IOException e) {
              e.printStackTrace();
            }
            armDownloadButtons(regStep.getDownloadButton(), regStep.getGraphButton());
            regStep.setSummary(prep.getSummary());
            // logger.debug("set processed samples");
            // regStep.setProcessed(prep.getProcessed());
          }
        }
        if (event.getActivatedStep().equals(finishStep)) {
          // TODO info component
          String proj = dataAggregator.getProjectCode();
          Project p = openbis.getProjectByCode(proj);
          Map<String, List<Sample>> samplesByExperiment = new HashMap<String, List<Sample>>();
          for (Sample s : openbis.getSamplesOfProject(p.getIdentifier())) {
            String expID = s.getExperimentIdentifierOrNull();
            String exp = expID.substring(expID.lastIndexOf("/") + 1);
            if (samplesByExperiment.containsKey(exp)) {
              List<Sample> lis = samplesByExperiment.get(exp);
              lis.add(s);
              samplesByExperiment.put(exp, lis);
            } else {
              List<Sample> lis = new ArrayList<Sample>(Arrays.asList(s));
              samplesByExperiment.put(exp, lis);
            }
          }
          finishStep.setExperimentInfos(p.getSpaceCode(), proj, p.getDescription(),
              samplesByExperiment, openbis);
        }
      }
    };
    w.addListener(wl);
  }

  protected List<AOpenbisSample> createSamplesFromPreselection(Map<Object, Integer> preSelection) {
    // TODO Auto-generated method stub
    return null;
  }

  protected void createTSV() {
    dataAggregator.createTSV();
  }

  protected void initConditionListener(final ConditionInstanceStep step, final String amount,
      final List<AOpenbisSample> previousLevel) {

    ValueChangeListener listener = new ValueChangeListener() {
      /**
         * 
         */
      private static final long serialVersionUID = 7925081983580407077L;

      public void valueChange(ValueChangeEvent event) {
        reloadConditionsPreviewTable(step, amount, previousLevel);
      }
    };
    step.attachListener(listener);
  }

  protected void reloadConditionsPreviewTable(ConditionInstanceStep step, String amount,
      List<AOpenbisSample> previousLevel) {
    if (step.validInput()) {
      if (previousLevel.isEmpty())
        step.buildTable(preparePreviewPermutations(step.getFactors()), amount);
      else
        step.buildTable(preparePreviewPermutations(step.getFactors(), previousLevel), amount);
    } else {
      step.destroyTable();
    }

  }

  /**
   * Prepare all condition permutations for the user to set the amounts when conditions from a
   * previous tier are included
   * 
   * @param factorLists
   * @param previousTier Samples of the previous tier
   * @return
   */
  public List<String> preparePreviewPermutations(List<List<Factor>> factorLists,
      List<AOpenbisSample> previousTier) {
    List<String> permutations = new ArrayList<String>();
    for (AOpenbisSample e : previousTier) {
      List<List<String>> res = new ArrayList<List<String>>();
      String secName = e.getQ_SECONDARY_NAME();
      if (secName == null)
        secName = "";
      String condKey = "(" + e.getCode().split("-")[1] + ") " + secName;
      res.add(new ArrayList<String>(Arrays.asList(condKey)));
      for (List<Factor> instances : factorLists) {
        List<String> factorValues = new ArrayList<String>();
        for (Factor f : instances) {
          String name = f.getValue() + f.getUnit();
          factorValues.add(name);
        }
        res.add(factorValues);
      }
      permutations.addAll(generatePermutations(res));
    }
    return permutations;
  }

  /**
   * Prepare all condition permutations for the user to set the amounts
   * 
   * @param factorLists
   * @return
   */
  public List<String> preparePreviewPermutations(List<List<Factor>> factorLists) {
    List<List<String>> res = new ArrayList<List<String>>();
    for (List<Factor> instances : factorLists) {
      List<String> factorValues = new ArrayList<String>();
      for (Factor f : instances) {
        String name = f.getValue() + f.getUnit();
        factorValues.add(name);
      }
      res.add(factorValues);
    }
    List<String> permutations = generatePermutations(res);
    return permutations;
  }

  /**
   * Generates all permutations of a list of experiment conditions
   * 
   * @param lists Instance lists of different conditions
   * @return List of all possible permutations of the input conditions
   */
  public List<String> generatePermutations(List<List<String>> lists) {
    List<String> res = new ArrayList<String>();
    generatePermutationsHelper(lists, res, 0, "");
    return res;
  }

  /**
   * recursive helper
   */
  private void generatePermutationsHelper(List<List<String>> lists, List<String> result, int depth,
      String current) {
    String separator = "###";
    if (depth == lists.size()) {
      result.add(current);
      return;
    }
    for (int i = 0; i < lists.get(depth).size(); ++i) {
      if (current.equals(""))
        separator = "";
      generatePermutationsHelper(lists, result, depth + 1,
          current + separator + lists.get(depth).get(i));
    }
  }

  protected void armDownloadButtons(Button tsv, Button graph) {
    StreamResource tsvStream =
        getTSVStream(dataAggregator.getTSVContent(), dataAggregator.getTSVName());
    if (tsvDL == null) {
      tsvDL = new FileDownloader(tsvStream);
      tsvDL.extend(tsv);
    } else
      tsvDL.setFileDownloadResource(tsvStream);
    StreamResource graphStream = getGraphStream(prep.toGraphML(), "test");
    if (graphDL == null) {
      graphDL = new FileDownloader(graphStream);
      graphDL.extend(graph);
    } else
      graphDL.setFileDownloadResource(graphStream);
  }

  public StreamResource getGraphStream(final String content, String name) {
    StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
      @Override
      public InputStream getStream() {
        try {
          InputStream is = new ByteArrayInputStream(content.getBytes());
          return is;
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }
    }, String.format("%s.graphml", name));
    return resource;
  }

  public StreamResource getTSVStream(final String content, String name) {
    StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
      @Override
      public InputStream getStream() {
        try {
          InputStream is = new ByteArrayInputStream(content.getBytes());
          return is;
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }
    }, String.format("%s.tsv", name));
    return resource;
  }

  /**
   * set flag denoting the inheritance from entities existing in the system
   * 
   * @param inherit
   */
  public void setInheritEntities(boolean inherit) {
    this.inheritEntities = inherit;
  }

  /**
   * set flag denoting the inheritance from extracts existing in the system
   * 
   * @param inherit
   */
  public void setInheritExtracts(boolean inherit) {
    this.inheritExtracts = inherit;
  }

}
