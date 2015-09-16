package control;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

public class SampleCounter {

  private int entityID;
  private int barcodeID;
  private String barcode;
  private String project;

  public SampleCounter(List<Sample> samples) {
    this(samples.get(0).getCode().substring(0, 5));
    for (Sample s : samples)
      increment(s);
  }

  public SampleCounter(String project) {
    entityID = 1;
    barcodeID = 1;
    this.project = project;
  }

  // TODO later updates
  public void increment(Sample s) {
    String code = s.getCode();
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

  public String getNewEntity() {
    entityID++;
    return project + "ENTITY-" + Integer.toString(entityID);
  }

  public String getNewBarcode() {
    if (barcode == null)
      barcode = project + Functions.createCountString(barcodeID, 3) + "AX";// TODO
    barcode = Functions.incrementSampleCode(barcode);
    return barcode;
  }

}
