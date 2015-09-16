package model;

import java.util.Map;

import logging.Log4j2Logger;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.vaadin.ui.ComboBox;

import control.BarcodeController;

public class SampleToBarcodeFieldTranslator {

  private logging.Logger logger = new Log4j2Logger(BarcodeController.class);

  public String buildInfo(ComboBox select, Sample s, String parents) {
    Map<String, String> map = s.getProperties();
    String in = "";
    if (select.getValue() != null)
      in = select.getValue().toString();
    String res = "";
    switch (in) {
      case "Tissue/Extr. Material":
        if (map.containsKey("Q_PRIMARY_TISSUE"))
          res = map.get("Q_PRIMARY_TISSUE");
        else
          res = map.get("Q_SAMPLE_TYPE");
        break;
      case "Secondary Name":
        res = map.get("Q_SECONDARY_NAME");
        break;
      case "Parent Samples (Source)":
        if (parents == null) {
          logger
              .error("Calls from BarcodePreviewComponent should not be able to select Parent samples as Info field choice."
                  + "Setting from null to empty String to continue.");
          parents = "";
        }
        res = parents;
        break;
      case "QBiC ID":
        res = s.getCode();
    }
    if (res == null)
      return "";
    return res.substring(0, Math.min(res.length(), 22));
  }

  public String getCodeString(Sample sample, String codedName) {
    Map<String, String> map = sample.getProperties();
    String res = "";
    // @SuppressWarnings("unchecked")
    // Set<String> selection = (Set<String>) codedName.getValue();
    // for (String s : selection) {
    String s = codedName;
    if (!res.isEmpty())
      res += "_";
    switch (s) {
      case "QBiC ID":
        res += sample.getCode();
        break;
      case "Secondary Name":
        res += map.get("Q_SECONDARY_NAME");
        break;
      case "Lab ID":
        res += map.get("Q_EXTERNALDB_ID");
        break;
    }
    // }
    res = fixFileName(res);
    return res.substring(0, Math.min(res.length(), 21));
  }

  private String fixFileName(String res) {
    res = res.replace("null", "");
    res = res.replace(";", "_");
    res = res.replace("#", "_");
    res = res.replace(" ", "_");
    while (res.contains("__"))
      res = res.replace("__", "_");
    return res;
  }
}
