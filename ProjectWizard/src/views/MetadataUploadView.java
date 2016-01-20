package views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uicomponents.UploadComponent;

import logging.Log4j2Logger;
import main.OpenBisClient;
import main.ProjectwizardUI;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.themes.ValoTheme;

public class MetadataUploadView extends VerticalLayout {

  OptionGroup typeOfData = new OptionGroup("Type of Metadata", new ArrayList<String>(Arrays.asList(
      "Experiments", "Samples")));
  Label info;
  // ConditionsPanel pane;
  // Button dlTemplate;
  UploadComponent upload;
  Button send;

  OpenBisClient openbis;
  Map<String, Object> metadata;

  private logging.Logger logger = new Log4j2Logger(MetadataUploadView.class);

  public MetadataUploadView(OpenBisClient openbis) {
    this.openbis = openbis;
    setSpacing(true);
    setMargin(true);
    addComponent(typeOfData);
    info = new Label();
    addComponent(info);
    // dlTemplate = new Button("Download Template");
    // dlTemplate.setEnabled(false);
    // addComponent(dlTemplate);
    upload =
        new UploadComponent("Upload Metadata CSV", "Upload", ProjectwizardUI.tmpFolder, "meta_",
            1000);
    upload.setVisible(false);
    addComponent(upload);
    send = new Button("Send to Openbis");
    send.setVisible(false);
    addComponent(send);
    initListeners();
  }

  private void initListeners() {
    typeOfData.addValueChangeListener(new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        upload.setVisible(true);
      }
    });
    upload.addFinishedListener(new FinishedListener() {

      @Override
      public void uploadFinished(FinishedEvent event) {
        if (upload.wasSuccess())
          try {
            send.setVisible(parse(upload.getFile()));
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
      }
    });
    send.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        logger.info("Ingesting metadata");
        openbis.ingest("DSS1", "update-sample-metadata", metadata);
      }
    });
  }

  protected boolean parse(File file) throws IOException {
    boolean updateExperiments = typeOfData.getValue().equals("Experiments");
    metadata = new HashMap<String, Object>();
    List<String> ids = new ArrayList<String>();

    FileReader input = new FileReader(file);
    BufferedReader b = new BufferedReader(input);
    String line = null;
    List<String> header = Arrays.asList(b.readLine().trim().split("\t"));
    List<String[]> data = new ArrayList<String[]>();
    while ((line = b.readLine()) != null) {
      data.add(line.replace("\n", "").split("\t"));
    }
    String id = data.get(0)[header.indexOf("Identifier")];
    Project p = openbis.getProjectByCode(id.substring(0, 5));
    List<Sample> sampsOfProject = new ArrayList<Sample>();
    List<Experiment> expOfProject = new ArrayList<Experiment>();
    if (updateExperiments) {
      metadata.put("Project", p.getIdentifier());
      metadata.put("Experiment", "");
      expOfProject = openbis.getExperimentsForProject(p);
    } else {
      sampsOfProject = openbis.getSamplesOfProject(p.getIdentifier());
    }

    Map<String, Map<String, String>> typesInProject = new HashMap<String, Map<String, String>>();
    Map<String, String> entCodeToType = new HashMap<String, String>();

    if (updateExperiments)
      for (Experiment e : expOfProject) {
        String type = e.getExperimentTypeCode();
        entCodeToType.put(e.getCode(), type);
        if (!typesInProject.containsKey(type)) {
          typesInProject.put(type,
              openbis.getLabelsofProperties(openbis.getExperimentTypeByString(type)));
        }
      }
    else
      for (Sample s : sampsOfProject) {
        String type = s.getSampleTypeCode();
        entCodeToType.put(s.getCode(), type);
        if (!typesInProject.containsKey(type)) {
          typesInProject.put(type,
              openbis.getLabelsofProperties(openbis.getSampleTypeByString(type)));
        }
      }

    for (int i = 1; i < header.size(); i++) {
      //TODO collect properties
      String type = header.get(i);
      Map<String, String> curTypeMap = new HashMap<String, String>();
      for (String[] entData : data) {
        String code = entData[0];
        ids.add(code);
        String entType = entCodeToType.get(code);
        if (entData[i].length() > 0) {
          if (typesInProject.get(entType).keySet().contains(type)) {
            curTypeMap.put(code, entData[i]);
          } else {
            String error = type + " is not part of sample type " + entType + " (" + code + ")";
            logger.error(error);
            Notification n = new Notification(error);
            n.setStyleName(ValoTheme.NOTIFICATION_CLOSABLE);
            n.setDelayMsec(-1);
            n.show(UI.getCurrent().getPage());
            input.close();
            b.close();
            return false;
          }
        }
      }
      if (curTypeMap.size() > 0)
        metadata.put(type, curTypeMap);
    }
    input.close();
    b.close();
    metadata.put("identifiers", ids);
    metadata.put("types", new ArrayList<String>(header.subList(1, header.size())));
    logger.info("Parsed metadata: ");
    logger.info(metadata.toString());
    return true;
  }
}
