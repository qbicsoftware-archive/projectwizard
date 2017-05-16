package registration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import parser.LociParser;
import parser.XMLParser;
import processes.RegisteredSamplesReadyRunnable;
import properties.Factor;

import loci.GeneLocus;
import logging.Log4j2Logger;
import model.ISampleBean;
import model.OpenbisExperiment;

import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;

import concurrency.UpdateProgressBar;
import life.qbic.openbis.openbisclient.IOpenBisClient;


/**
 * Provides methods to register new entities to openBIS using the openBIS Client API. Also performs
 * some simple sanity checks before sending data to openBIS.
 * 
 * @author Andreas Friedrich
 * 
 */
public class OpenbisCreationController {
  final int RETRY_UNTIL_SECONDS_PASSED = 5;
  final int SPLIT_AT_ENTITY_SIZE = 100;
  private IOpenBisClient openbis;
  logging.Logger logger = new Log4j2Logger(OpenbisCreationController.class);


  public OpenbisCreationController(IOpenBisClient openbis) {
    this.openbis = openbis;
  }

  /**
   * Interact with an ingestion service script registered for the openBIS instance
   * 
   * @param ingestionService Name of the ingestions service script registered at openBIS
   * @param params HashMap of String parameter names and their arguments for the ingestion service
   */
  public void openbisGenericIngest(String ingestionService, HashMap<String, Object> params) {
    openbis.ingest("DSS1", ingestionService, params);
  }

  /**
   * Creates a space in openBIS and adds (existing) users with different rights using ingestion
   * scripts on the server
   * 
   * @param name The name of the space to create
   * @param userInfo HashMap of type HashMap<OpenbisSpaceUserRole,ArrayList<String>> containing one
   *        or more users of one or more types
   */
  public void registerSpace(String name, HashMap<OpenbisSpaceUserRole, ArrayList<String>> userInfo,
      String user) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("code", name);
    params.put("registration_user", user);
    for (OpenbisSpaceUserRole type : OpenbisSpaceUserRole.values()) {
      if (userInfo.containsKey(type))
        params.put(type.toString().toLowerCase(), userInfo.get(type));
    }
    // call ingestion service for space creation
    openbis.ingest("DSS1", "register-space", params);
  }

  /**
   * Create a project belonging to a space in openBIS using ingestion scripts on the server
   * 
   * @param space Existing space the project to create should resides in
   * @param name Name of the project to create
   * @param description Project description
   * @return false, if the specified space doesn't exist, resulting in failure, true otherwise
   */
  public boolean registerProject(String space, String name, String description, String user) {
    if (!openbis.spaceExists(space)) {
      logger.error(space + " does not exist!");
      return false;
    }
    logger.info("Creating project " + name + " in space " + space);
    if (description == null || description.isEmpty()) {
      description = "Created using the project wizard.";
      logger.warn("No project description input found. Setting standard info.");
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("user", user);
    params.put("code", name);
    params.put("space", space);
    params.put("desc", description);
    openbis.ingest("DSS1", "register-proj", params);
    return true;
  }

  /**
   * Create an experiment belonging to a project (and space) using ingestion scripts on the server
   * 
   * @param space Existing space in openBis
   * @param project Existing project in the space that this experiment will belong to
   * @param experimentType openBIS experiment type
   * @param name Experiment name
   * @param map Additional properties of the experiment
   * @return false, if the specified project doesn't exist, resulting in failure, true otherwise
   */
  public boolean registerExperiment(String space, String project, String experimentType,
      String name, Map<String, Object> map, String user) {
    logger.info("Creating experiment " + name);
    if (!openbis.projectExists(space, project)) {
      logger.error(project + " in " + space + " does not exist.");
      return false;
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("code", name);
    params.put("type", experimentType);
    params.put("project", project);
    params.put("space", space);
    params.put("properties", map);
    params.put("user", user);
    openbis.ingest("DSS1", "register-exp", params);
    return true;
  }

  protected void registerExperiment(String space, String proj, RegisterableExperiment exp,
      String projSecondaryName, String user) {
    String expCode = exp.getCode();
    final String projInfo = proj + "_INFO";
    if (!openbis.expExists(space, proj, projInfo)) {
      if (projSecondaryName != null && !projSecondaryName.isEmpty()) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("Q_SECONDARY_NAME", projSecondaryName);
        registerExperiment(space, proj, "Q_PROJECT_DETAILS", projInfo, properties, user);
      } else {
        logger.warn("No project secondary name found. Not creating Info Experiment.");
      }
    }
    int step = 100;
    int max = RETRY_UNTIL_SECONDS_PASSED * 1000;
    while (!openbis.projectExists(space, proj) && max > 0) {
      try {
        Thread.sleep(step);
        max -= step;
      } catch (InterruptedException e) {
        logger.error("thread sleep waiting for experiment creation interruped.");
        e.printStackTrace();
      }
    }
    if (!openbis.expExists(space, proj, expCode)) {
      logger.info("creating experiment " + expCode);
      registerExperiment(space, proj, exp.getType(), expCode, new HashMap<String, Object>(), user);
    }
  }

  protected boolean registerExperiments(String space, String proj,
      List<RegisterableExperiment> exps, String projSecondaryName, String user) {
    // final String projInfo = proj + "_INFO";
    // if (!openbis.expExists(space, proj, projInfo))
    // exps.add(new RegisterableExperiment(projInfo, "Q_PROJECT_DETAILS",
    // new ArrayList<ISampleBean>(Arrays.asList(new )), new HashMap<String, Object>()));
    int step = 100;
    int max = RETRY_UNTIL_SECONDS_PASSED * 1000;
    List<String> codes = new ArrayList<String>();
    List<String> types = new ArrayList<String>();
    List<Map<String, Object>> props = new ArrayList<Map<String, Object>>();
    for (RegisterableExperiment e : exps) {
      if (!openbis.expExists(space, proj, e.getCode())) {
        codes.add(e.getCode());
        types.add(e.getType());
        props.add(e.getProperties());
      }
    }
    if (codes.size() > 0) {
      while (!openbis.projectExists(space, proj) && max > 0) {
        try {
          Thread.sleep(step);
          max -= step;
        } catch (InterruptedException e) {
          logger.error("thread sleep waiting for experiment creation interruped.");
          e.printStackTrace();
        }
      }
      logger.info("Creating experiments " + codes);
      if (!openbis.projectExists(space, proj)) {
        logger.error(proj + " in " + space + " does not exist. Not creating experiments.");
        return false;
      }
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("codes", codes);
      params.put("types", types);
      params.put("project", proj);
      params.put("space", space);
      params.put("properties", props);
      params.put("user", user);
      openbis.ingest("DSS1", "register-exp", params);
    }
    return true;
  }

  public void prepareXMLProps(List<List<ISampleBean>> samples) {
    for (List<ISampleBean> list : samples) {
      for (ISampleBean s : list) {
        Map<String, String> metadata = s.getMetadata();
        XMLParser p = new XMLParser();
        LociParser lp = new LociParser();
        List<Factor> factors = new ArrayList<Factor>();
        if (metadata.get("XML_FACTORS") != null) {
          String[] fStrings = metadata.get("XML_FACTORS").split(";");
          for (String factor : fStrings) {
            if (factor.length() > 1) {
              String[] fields = factor.split(":");
              for (int i = 0; i < fields.length; i++)
                fields[i] = fields[i].trim();
              String lab = fields[0].replace(" ", "");
              String val = fields[1];
              if (fields.length > 2)
                factors.add(new Factor(lab, val, fields[2]));
              else
                factors.add(new Factor(lab, val));
            }
          }
          try {
            metadata.put("Q_PROPERTIES", p.toString(p.createXMLFromFactors(factors)));
          } catch (JAXBException e) {
            e.printStackTrace();
          }
        }
        metadata.remove("XML_FACTORS");

        List<GeneLocus> loci = new ArrayList<GeneLocus>();
        if (metadata.get("XML_LOCI") != null) {
          String[] lStrings = metadata.get("XML_LOCI").split(";");
          for (String locus : lStrings) {
            if (locus.length() > 1) {
              String[] fields = locus.split(":");
              for (int i = 0; i < fields.length; i++)
                fields[i] = fields[i].trim();
              String lab = fields[0];
              String[] alleles = fields[1].split("/");
              loci.add(new GeneLocus(lab, new ArrayList<String>(Arrays.asList(alleles))));
            }
          }
          try {
            metadata.put("Q_LOCI", lp.toString(lp.createXMLFromLoci(loci)));
          } catch (JAXBException e) {
            e.printStackTrace();
          }
        }
        metadata.remove("XML_LOCI");
      }
    }
  }

  private List<List<ISampleBean>> splitSamplesIntoBatches(List<ISampleBean> samples,
      int targetSize) {
    List<List<ISampleBean>> res = new ArrayList<List<ISampleBean>>();
    int size = samples.size();
    if (size < targetSize)
      return new ArrayList<List<ISampleBean>>(Arrays.asList(samples));
    for (int i = 0; i < size / targetSize; i++) {
      int from = i * targetSize;
      int to = (i + 1) * targetSize;
      res.add(samples.subList(from, to));
      if (to > size - targetSize && to != size)
        res.add(samples.subList(to, size));
    }
    return res;
  }

  /**
   * this is the one normally called!
   * 
   * @param tsvSampleHierarchy
   * @param description
   * @param secondaryName
   * @param bar
   * @param info
   * @param ready
   * @param user
   */
  public void registerProjectWithExperimentsAndSamplesBatchWise(
      final List<List<ISampleBean>> tsvSampleHierarchy, final String description,
      final String secondaryName, final List<OpenbisExperiment> informativeExperiments,
      final Map<String, Map<String, Object>> mhcExperimentProperties, final ProgressBar bar,
      final Label info, final Runnable ready, final String user, final boolean isPilot) {

    logger.debug("User sending samples: " + user);
    Thread t = new Thread(new Runnable() {
      volatile int current = -1;

      @Override
      public void run() {
        info.setCaption("Collecting information");
        UI.getCurrent().access(new UpdateProgressBar(bar, info, 0.01));
        RegisterableProject p = new RegisterableProject(tsvSampleHierarchy, description,
            secondaryName, mhcExperimentProperties, informativeExperiments, isPilot);
        // if(mhcExperimentProperties != null)
        // p.addExperimentFromProperties(mhcExperimentProperties, "Q_MHC_LIGAND_EXTRACTION");
        List<RegisterableExperiment> exps = p.getExperiments();
        String space = p.getSpace().toUpperCase();
        String project = p.getProjectCode();
        String desc = p.getDescription();

        int splitSteps = 0;
        // find out which experiments have so many samples they should be sent in multiple packages
        for (RegisterableExperiment exp : exps) {
          splitSteps += exp.getSamples().size() / (SPLIT_AT_ENTITY_SIZE + 1);
        }

        final int todo = exps.size() + splitSteps + 1;// TODO huge number of samples should be split
                                                      // into groups
        // of 50 or 100. this needs to be reflected in the progress
        // bar
        current++;
        double frac = current * 1.0 / todo;
        info.setCaption("Registering Project and Experiments");
        UI.getCurrent().access(new UpdateProgressBar(bar, info, frac));
        if (!openbis.projectExists(space, project))
          registerProject(space, project, desc, user);
        registerExperiments(space, project, exps, secondaryName, user);

        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          logger.error("thread sleep waiting for experiment creation interruped.");
          e.printStackTrace();
        }
        for (RegisterableExperiment exp : exps) {
          List<ISampleBean> level = exp.getSamples();
          info.setCaption("Registering samples");
          current++;
          frac = current * 1.0 / todo;
          UI.getCurrent().access(new UpdateProgressBar(bar, info, frac));
          if (level.size() > SPLIT_AT_ENTITY_SIZE) {
            for (List<ISampleBean> batch : splitSamplesIntoBatches(level, SPLIT_AT_ENTITY_SIZE)) {
              registerSampleBatchInETL(batch, user);
              try {
                Thread.sleep(50);
              } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
              current++;
              frac = current * 1.0 / todo;
              UI.getCurrent().access(new UpdateProgressBar(bar, info, frac));
            }
          } else
            registerSampleBatchInETL(level, user);
          if (level.size() > 0) {
            ISampleBean last = level.get(level.size() - 1);
            logger.info("waiting for last sample to reach openbis");
            int step = 50;
            int max = RETRY_UNTIL_SECONDS_PASSED * 1000;
            while (!openbis.sampleExists(last.getCode()) && max > 0) {
              try {
                Thread.sleep(step);
                max -= step;
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
          }
        }
        current++;
        frac = current * 1.0 / todo;
        UI.getCurrent().access(new UpdateProgressBar(bar, info, frac));

        UI.getCurrent().setPollInterval(-1);
        UI.getCurrent().access(ready);
      }
    });
    t.start();
    UI.getCurrent().setPollInterval(100);
  }

  public boolean registerSampleBatchInETL(List<ISampleBean> samples, String user) {
    String s = null;
    String p = null;
    String e = null;
    if (samples.size() == 0)
      return false;
    // to speed up things only the first sample and its experiment is checked for existence, might
    // lead to errors
    ISampleBean first = samples.get(0);
    if (!first.getExperiment().equals(e)) {
      s = first.getSpace();
      p = first.getProject();
      e = first.getExperiment();
      if (!openbis.expExists(s, p, e)) {
        logger.error(e + " not found in " + p + " (" + s
            + ") Stopping registration of this sample batch. This will most likely lead to openbis errors or lost samples!");
        return false;
      }
    }
    Map<String, Object> params = new HashMap<String, Object>();
    for (ISampleBean sample : samples) {
      if (openbis.sampleExists(sample.getCode())) {
        logger.warn(sample.getCode() + " already exists in " + p
            + " Removing this sample from registration process.");
      } else {
        String space = sample.getSpace();
        String project = sample.getProject();
        String exp = sample.getExperiment();
        ArrayList<String> parents = sample.fetchParentIDs();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("code", sample.getCode());
        map.put("space", space);
        map.put("project", project);
        map.put("experiment", exp);
        map.put("user", user);
        map.put("type", sample.getType());
        if (!sample.getSecondaryName().isEmpty()) {
          map.put("Q_SECONDARY_NAME", sample.getSecondaryName());
        }
        if (!parents.isEmpty())
          map.put("parents", parents);
        map.put("metadata", sample.getMetadata());
        params.put(sample.getCode(), map);
      }
    }
    logger.info("Sending batch of new samples to Ingestion Service.");
    openbis.ingest("DSS1", "register-sample-batch", params);
    return true;
  }

  /**
   * register a single sample in openbis. space, project, experiment and type have to exist in
   * openbis! no parents can be registered in this way!
   * 
   * @param code
   * @param space
   * @param project
   * @param experiment
   * @param type
   * @param user
   * @param metadata
   * @return
   */
  public boolean registerSample(String code, String space, String project, String exp, String type,
      String user, Map<String, Object> metadata) {
    Map<String, Object> params = new HashMap<String, Object>();
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("code", code);
    map.put("space", space);
    map.put("project", project);
    map.put("experiment", exp);
    map.put("user", user);
    map.put("type", type);
    map.put("metadata", metadata);
    params.put(code, map);
    openbis.ingest("DSS1", "register-sample-batch", params);
    return true;
  }

}
