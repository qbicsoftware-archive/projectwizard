/*******************************************************************************
 * QBiC Project Wizard enables users to create hierarchical experiments including different study conditions using factorial design.
 * Copyright (C) "2016"  Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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

import javax.xml.bind.JAXBException;

import parser.XMLParser;
import properties.Factor;

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
          } catch (IOException | JAXBException e) {
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

  protected boolean parse(File file) throws IOException, JAXBException {
    boolean updateExperiments = typeOfData.getValue().equals("Experiments");
    metadata = new HashMap<String, Object>();
    List<String> codes = new ArrayList<String>();

    FileReader input = new FileReader(file);
    BufferedReader b = new BufferedReader(input);
    String line = null;
    List<String> header = Arrays.asList(b.readLine().trim().split("\t"));
    List<String[]> data = new ArrayList<String[]>();
    while ((line = b.readLine()) != null) {
      data.add(line.replace("\n", "").split("\t"));
    }
    String firstCode = data.get(0)[header.indexOf("Identifier")];
    Project p = openbis.getProjectByCode(firstCode.substring(0, 5));
    List<Sample> sampsOfProject = new ArrayList<Sample>();
    Map<String, Sample> sampleMap = new HashMap<String, Sample>();
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
        sampleMap.put(s.getCode(), s);
        String type = s.getSampleTypeCode();
        entCodeToType.put(s.getCode(), type);
        if (!typesInProject.containsKey(type)) {
          typesInProject.put(type,
              openbis.getLabelsofProperties(openbis.getSampleTypeByString(type)));
        }
      }

    List<String> conditions = new ArrayList<String>();
    List<String> types = new ArrayList<String>();
    for (int i = 1; i < header.size(); i++) {
      // TODO collect properties
      String type = header.get(i);
      if (type.startsWith("Condition: ")) {
        type = type.replace("Condition: ", "").replace(" ", "");
        conditions.add(type);
      }
      types.add(type);
      // maps between sample codes and values of the currently handled metadata type
      Map<String, String> curTypeMap = new HashMap<String, String>();
      for (String[] entData : data) {
        String code = entData[0];
        if (!codes.contains(code))
          codes.add(code);
        String entType = entCodeToType.get(code);
        if (entData[i].length() > 0) {
          if (typesInProject.get(entType).keySet().contains(type) || conditions.contains(type)) {
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
    XMLParser xmlParser = new XMLParser();
    Map<String, String> xmlPropertyMap = new HashMap<String, String>();
    // for each sample
    for (String code : codes) {
      boolean change = false;
      List<Factor> factors =
          xmlParser.getFactorsFromXML(sampleMap.get(code).getProperties().get("Q_PROPERTIES"));
      // for each condition found in the update tsv
      for (String condition : conditions) {
        // check if it should be updated or added for this sample.
        if (metadata.containsKey(condition)) {
          Map<String, String> condMap = (Map<String, String>) metadata.get(condition);
          if (condMap.containsKey(code)) {
            String value = condMap.get(code);
            int factorIndex = 0;
            for (Factor f : factors) {
              if (f.getLabel().equals(condition)) {
                f = new Factor(condition, value);
                factors.set(factorIndex, f);
                change = true;
              }
              factorIndex++;
            }
          }
        }
      }
      if (change)
        xmlPropertyMap.put(code, xmlParser.toString(xmlParser.createXMLFromFactors(factors)));
    }
    for (String condition : conditions) {
      types.remove(condition);
      metadata.remove(condition);
    }
    if (!xmlPropertyMap.isEmpty()) {
      types.add("Q_PROPERTIES");
      metadata.put("Q_PROPERTIES", xmlPropertyMap);
    }
    input.close();
    b.close();
    metadata.put("identifiers", codes);
    metadata.put("types", types);
    logger.info("Parsed metadata: ");
    logger.info(metadata.toString());
    return true;
  }
}
