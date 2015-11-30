package control;

import java.util.List;

import logging.Log4j2Logger;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

public class SampleCounter {

  private int entityID;
  private int barcodeID;
  private int expID;
  private String barcode;
  private String project;
  logging.Logger logger = new Log4j2Logger(SampleCounter.class);

  public SampleCounter(List<Sample> samples) {
    this(samples.get(0).getCode().substring(0, 5));
    for (Sample s : samples)
      increment(s);
  }

  public SampleCounter(String project) {
    entityID = 0;
    expID = 0;
    barcodeID = 1;
    this.project = project;
  }

  // TODO later updates (after initialization)
  public void increment(Sample s) {
    String code = s.getCode();
    try {
      String exp = s.getExperimentIdentifierOrNull().split(project + "E")[1];
      int expNum = Integer.parseInt(exp);
      if (expNum > expID)
        expID = expNum;
    } catch (Exception e) {
      logger.warn("While counting existing experiments in project " + project
          + " unfamiliar experiment identifier " + s.getExperimentIdentifierOrNull()
          + " was found. Either this project is atypical or your code sucks!");
    }
    if (Functions.isQbicBarcode(code)) {
      int num = Integer.parseInt(code.substring(5, 8));
      if (num >= barcodeID)
        barcodeID = num;
    } else if (s.getSampleTypeCode().equals(("Q_BIOLOGICAL_ENTITY"))) {
      int num = Integer.parseInt(s.getCode().split("-")[1]);
      if (num >= entityID)
        entityID = num;
    }
  }

  public String getNewExperiment() {
    expID++;
    return project + "E" + expID;
  }

  public String getNewEntity() {
    entityID++;
    return project + "ENTITY-" + Integer.toString(entityID);
  }

  public String getNewBarcode() {
    if (barcode == null) {
      barcode = project + Functions.createCountString(barcodeID, 3) + "A";
      barcode = barcode + Functions.checksum(barcode);
    }
    barcode = Functions.incrementSampleCode(barcode);
    return barcode;
  }

}
