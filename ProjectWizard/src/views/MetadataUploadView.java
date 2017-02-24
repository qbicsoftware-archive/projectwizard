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
package views;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;

import parser.XMLParser;
import properties.Factor;

import uicomponents.UploadComponent;

import logging.Log4j2Logger;
import main.IOpenBisClient;
import main.ProjectwizardUI;
import uicomponents.Styles;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import control.Functions;
import control.Functions.NotificationType;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Upload.FinishedEvent;

import au.com.bytecode.opencsv.CSVReader;

public class MetadataUploadView extends VerticalLayout {

  private OptionGroup typeOfData =
      new OptionGroup("Type of Metadata", new ArrayList<String>(Arrays.asList("Samples")));

  private Label info;
  // ConditionsPanel pane;
  // Button dlTemplate;
  private UploadComponent upload;
  private Button send;

  private XMLParser xmlParser = new XMLParser();
  private IOpenBisClient openbis;
  private Map<String, Object> metadata;
  private List<String> customProperties =
      new ArrayList<String>(Arrays.asList("IGNORE (removes column)", "[Experimental Condition]"));

  private logging.Logger logger = new Log4j2Logger(MetadataUploadView.class);
  private Table sampleTable;
  private Button reload;

  private Map<String, Sample> codesToSamples;
  private String barcodeColName;

  private List<String> collisions;

  private List<String> codesInTSV;

  public MetadataUploadView(IOpenBisClient openbis) {
    this.openbis = openbis;
    setSpacing(true);
    setMargin(true);
    info = new Label("Sorry, under construction!");
    addComponent(info);
    addComponent(typeOfData);
    // dlTemplate = new Button("Download Template");
    // dlTemplate.setEnabled(false);
    // addComponent(dlTemplate);
    upload = new UploadComponent("Upload Metadata (tab-separated)", "Upload",
        ProjectwizardUI.tmpFolder, "meta_", 10000);
    upload.setVisible(false);
    addComponent(upload);
    reload = new Button("Reset columns");
    reload.setVisible(false);
    addComponent(reload);
    reload.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        try {
          parseTSV(upload.getFile());
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });

    send = new Button("Send to Openbis");
    send.setEnabled(false);
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
            send.setVisible(parseTSV(upload.getFile()));
            reload.setVisible(true);
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
      }
    });
    send.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        try {
          ingestTable();
          Functions.notification("Done!", "Your metadata was sent to openBIS.",
              NotificationType.SUCCESS);
        } catch (Exception e) {
          Functions.notification("Something went wrong!",
              "Sorry, your metadata could not be registered. Please contact a delevoper.",
              NotificationType.ERROR);
        }
      }
    });
  }

  protected void ingestTable() throws IllegalArgumentException, JAXBException {
    metadata = new HashMap<String, Object>();
    List<String> types = new ArrayList<String>();
    List<String> conditions = new ArrayList<String>();
    List<String> codes = new ArrayList<String>();
    for (Object col : sampleTable.getContainerPropertyIds()) {
      String colName = checkSelection(col);
      if (!colName.equals("Properties -->")) {
        if (colName.startsWith("Condition: ")) {
          colName = colName.replace("Condition: ", "");
          conditions.add(colName);
        } else
          types.add(colName);
        Map<String, String> curTypeMap = new HashMap<String, String>();
        for (Object row : sampleTable.getItemIds()) {
          int id = (int) row;
          if (id != -1) {
            String bc = getBarcodeInRow(id);
            String val = parseLabelCell(id, col);
            if (!codes.contains(bc))
              codes.add(bc);
            curTypeMap.put(bc, val);
          }
        }
        metadata.put(colName, curTypeMap);
      }
    }
    Map<String, String> xmlPropertyMap = new HashMap<String, String>();
    for (String code : codes) {
      List<Factor> newFactors = new ArrayList<Factor>();
      List<Factor> factors =
          xmlParser.getFactorsFromXML(codesToSamples.get(code).getProperties().get("Q_PROPERTIES"));
      for (String condition : conditions) {
        for (Factor f : factors) {
          if (!conditions.contains(f.getLabel())) {
            newFactors.add(f);
          }
        }
        Map<String, String> condMap = (HashMap<String, String>) metadata.get(condition);
        newFactors.add(new Factor(condition, condMap.get(code)));
      }
      if (!newFactors.isEmpty())
        xmlPropertyMap.put(code, xmlParser.toString(xmlParser.createXMLFromFactors(newFactors)));
    }
    for (String condition : conditions) {
      metadata.remove(condition);
    }
    if (!xmlPropertyMap.isEmpty()) {
      types.add("Q_PROPERTIES");
      metadata.put("Q_PROPERTIES", xmlPropertyMap);
    }
    metadata.put("identifiers", codes);
    metadata.put("types", types);
    System.out.println(metadata);// TODO test
    logger.info("Ingesting metadata");
    openbis.ingest("DSS1", "update-sample-metadata", metadata);
  }

  protected boolean parseTSV(File file) throws IOException {
    if (sampleTable != null) {
      sampleTable.removeAllItems();
      this.removeComponent(sampleTable);
    }
    CSVReader reader = new CSVReader(new FileReader(file), '\t');
    String error = "";
    ArrayList<String[]> data = new ArrayList<String[]>();
    String[] nextLine;
    int rowID = 0;
    while ((nextLine = reader.readNext()) != null) {
      rowID++;
      if (data.isEmpty() || nextLine.length == data.get(0).length) {
        data.add(nextLine);
      } else {
        error = "Wrong number of columns in row " + rowID
            + " Please make sure every row fits the header row.";
        Functions.notification("Parsing Error", error, NotificationType.ERROR);
        reader.close();
        return false;
      }
    }
    reader.close();
    String[] header = data.get(0);
    data.remove(0);
    int barcodeCol = -1;
    String projectCode = "";
    for (int j = 0; j < header.length; j++) {
      String word = data.get(0)[j];
      if (Functions.isQbicBarcode(word) || word.contains("ENTITY-")) {// TODO
        barcodeCol = j;
        barcodeColName = header[barcodeCol];
        projectCode = word.substring(0, 5);
      }
    }
    if (barcodeCol == -1) {
      error =
          "No barcode column found. Make sure one column contains QBiC Barcodes to map your information to existing samples!";
      Functions.notification("File Incomplete", error, NotificationType.ERROR);
      return false;
    }
    List<Sample> projectSamples =
        openbis.getSamplesWithParentsAndChildrenOfProjectBySearchService(projectCode);
    codesToSamples = new HashMap<String, Sample>();
    Map<String, List<String>> sampleTypeToAttributes = new HashMap<String, List<String>>();
    Map<String, DataTypeCode> propertyToType = new HashMap<String, DataTypeCode>();
    for (Sample s : projectSamples) {
      codesToSamples.put(s.getCode(), s);
    }
    codesInTSV = new ArrayList<String>();
    for (int i = 0; i < data.size(); i++) {
      String bc = data.get(i)[barcodeCol];
      if (!codesToSamples.containsKey(bc)) {
        Functions.notification("Sample not found!",
            "Sample with code " + bc + " was not found in openBIS.", NotificationType.ERROR);
        return false;
      }
      String type = codesToSamples.get(bc).getSampleTypeCode();
      if (!sampleTypeToAttributes.containsKey(type)) {
        List<PropertyType> props =
            openbis.listPropertiesForType(openbis.getSampleTypeByString(type));
        List<String> propertyCodes = new ArrayList<String>();
        for (PropertyType p : props) {
          String code = p.getCode();
          DataTypeCode dataType = p.getDataType();
          switch (dataType) {
            case CONTROLLEDVOCABULARY: // TODO
              break;
            case MATERIAL:
              break;
            case TIMESTAMP:
              break;
            case XML:
              break;
            default:
              propertyToType.put(code, dataType);
              propertyCodes.add(code);
          }
        }
        sampleTypeToAttributes.put(type, propertyCodes);
      }
      codesInTSV.add(bc);
    }
    for (String type : sampleTypeToAttributes.keySet()) {
      Set<String> options = new HashSet<String>();
      options.addAll(customProperties);
      options.addAll(sampleTypeToAttributes.get(type));
      // options.removeAll(hiddenProperties);
      sampleTable = new Table(type + " Samples");
      sampleTable.setWidth("100%");
      sampleTable.setStyleName(Styles.tableTheme);
      sampleTable.addContainerProperty(header[barcodeCol], String.class, null);
      for (int i = 0; i < header.length; i++) {
        if (i != barcodeCol) {
          sampleTable.addContainerProperty(header[i], Component.class, null);
        }
      }
      List<Object> row = new ArrayList<Object>();
      for (int i = 0; i < header.length; i++) {
        if (i != barcodeCol) {
          String headline = header[i];
          ComboBox attributeOptions = new ComboBox("", options);
          attributeOptions.setStyleName(Styles.boxTheme);
          attributeOptions.setImmediate(true);
          attributeOptions.setInputPrompt("<Select Attribute>");
          attributeOptions.setWidth("100%");
          attributeOptions.setFilteringMode(FilteringMode.CONTAINS);
          attributeOptions.setNullSelectionAllowed(false);
          attributeOptions.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
              String val = (String) attributeOptions.getValue();
              if (val.equals("[Experimental Condition]"))
                createConditionWindow(attributeOptions);
              else {
                if (val.equals("IGNORE (removes column)"))
                  sampleTable.removeContainerProperty(headline);
                else {
                  DataTypeCode dType = propertyToType.get(val);
                  if (dType.equals(DataTypeCode.REAL) || dType.equals(DataTypeCode.INTEGER)) {
                    checkForNumberConsistency(headline, dType);
                  }
                }
                reactToTableChange();
              }
            }
          });
          row.add(attributeOptions);
        } else {
          row.add("Properties -->");
        }
      }
      sampleTable.addItem(row.toArray(), -1);
      for (int i = 0; i < codesInTSV.size(); i++) {
        row = new ArrayList<Object>();
        row.add(codesInTSV.get(i));
        for (int j = 0; j < header.length; j++) {
          if (j != barcodeCol) {
            row.add(new Label(data.get(i)[j]));
          }
        }
        sampleTable.addItem(row.toArray(), i);
      }
      addComponent(sampleTable);
      addComponent(send);
      reactToTableChange();
    }
    return true;
  }

  protected void checkForNumberConsistency(String headline, DataTypeCode dType) {
    boolean consistent = true;
    boolean needsDelimiterChange = false;
    for (Object item : sampleTable.getItemIds()) {
      int id = (int) item;
      String val = parseLabelCell(id, headline);
      if (!val.isEmpty()) {
        if (id != -1) {
          if (dType.equals(DataTypeCode.INTEGER)) {
            try {
              Integer.parseInt(val);
            } catch (NumberFormatException e) {
              consistent = false;
            }
          }
          if (dType.equals(DataTypeCode.REAL)) {
            // try normal parse
            try {
              Double.parseDouble(val);
            } catch (NumberFormatException e) {
              // normal parse unsuccessful, check for different delimiter
              NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
              Number number;
              try {
                number = format.parse(val);
                // worked, needs different delimiter
                needsDelimiterChange = true;
              } catch (ParseException e1) {
                // didn't work, not a double value
                consistent = false;
              }
            }
          }
        }
      }
    }
    if (consistent) {
      if (needsDelimiterChange)
        createDelimiterChangeDialogue(headline);
    } else {
      createNotRightTypeDialogue(headline);
    }
  }

  private void createNotRightTypeDialogue(String headline) {
    Window subWindow = new Window(" Wrong data type!");
    subWindow.setWidth("400px");

    VerticalLayout layout = new VerticalLayout();
    layout.setSpacing(true);
    layout.setMargin(true);
    Label preInfo = new Label("Data in this column doesn't fit the attribute type.");
    layout.addComponent(preInfo);
    Button ok = new Button("Ignore Column.");
    Button no = new Button("Select different attribute.");
    ok.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        sampleTable.removeContainerProperty(headline);
      }
    });
    no.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        resetAttribute(headline);
      }
    });
    layout.addComponent(ok);
    layout.addComponent(no);

    subWindow.setContent(layout);
    // Center it in the browser window
    subWindow.center();
    subWindow.setModal(true);
    subWindow.setIcon(FontAwesome.BOLT);
    subWindow.setResizable(false);
    ProjectwizardUI ui = (ProjectwizardUI) UI.getCurrent();
    ui.addWindow(subWindow);
  }

  protected void resetAttribute(String headline) {
    Item item = sampleTable.getItem(-1);
    Object cell = item.getItemProperty(headline).getValue();
    ComboBox c = ((ComboBox) cell);
    c.setNullSelectionAllowed(true);
    c.select(null);
    c.setNullSelectionAllowed(false);
  }

  private void createDelimiterChangeDialogue(String headline) {
    Window subWindow = new Window(" Unexpected number format");
    subWindow.setWidth("400px");

    VerticalLayout layout = new VerticalLayout();
    layout.setSpacing(true);
    layout.setMargin(true);
    Label preInfo = new Label("The decimal delimiter of this type needs to be replaced with '.'.");
    layout.addComponent(preInfo);
    Button ok = new Button("Change numbers in this column.");
    Button no = new Button("Select different attribute.");
    ok.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        changeDelimiterInCol(headline);
      }
    });
    no.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        resetAttribute(headline);
      }
    });
    layout.addComponent(ok);
    layout.addComponent(no);

    subWindow.setContent(layout);
    // Center it in the browser window
    subWindow.center();
    subWindow.setModal(true);
    subWindow.setIcon(FontAwesome.QUESTION);
    subWindow.setResizable(false);
    ProjectwizardUI ui = (ProjectwizardUI) UI.getCurrent();
    ui.addWindow(subWindow);
  }

  protected void changeDelimiterInCol(String headline) {
    for (Object item : sampleTable.getItemIds()) {
      int id = (int) item;
      if (id != -1) {
        String val = parseLabelCell(id, headline);
        writeLabelCell(id, headline, val.replace(",", "."));
      }
    }
  }

  private void reactToTableChange() {
    fillCollisionsList();
    styleTable();
    showStatus();
  }

  private void fillCollisionsList() {
    collisions = new ArrayList<String>();
    for (Object propertyId : sampleTable.getContainerPropertyIds()) {
      for (Object itemId : sampleTable.getItemIds()) {
        String type = checkSelection(propertyId);
        // type set
        if (type != null && !propertyId.equals(barcodeColName)) {
          // check for data in openbis that would be overwritten
          String collision = getCollisionOrNull(propertyId, itemId);
          if (collision != null) {
            collisions.add(collision);
          }
        }
      }
    }
  }

  private void showStatus() {
    boolean ready = true;
    for (Object colName : sampleTable.getContainerPropertyIds()) {
      String selected = checkSelection(colName);
      ready &= selected != null && !selected.isEmpty();
    }
    if (ready) {
      System.out.println(collisions);
      if (collisions.size() > 0) {
        Window subWindow = new Window(" Collisions found!");
        subWindow.setWidth("400px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setMargin(true);
        Label preInfo = new Label("The following entries exist and can be overwritten:");
        layout.addComponent(preInfo);
        TextArea tf = new TextArea();
        tf.setWidth("350px");
        tf.setValue(StringUtils.join(collisions, ""));
        tf.setStyleName(Styles.areaTheme);
        layout.addComponent(tf);
        Label info = new Label(
            "You can either remove the columns in question (choose 'ignore column') before sending it to openBIS or overwrite the metadata.");
        Button ok = new Button("Got it!");
        ok.addClickListener(new ClickListener() {

          @Override
          public void buttonClick(ClickEvent event) {
            subWindow.close();
          }
        });
        layout.addComponent(info);
        layout.addComponent(ok);

        subWindow.setContent(layout);
        // Center it in the browser window
        subWindow.center();
        subWindow.setModal(true);
        subWindow.setIcon(FontAwesome.BOLT);
        subWindow.setResizable(false);
        ProjectwizardUI ui = (ProjectwizardUI) UI.getCurrent();
        ui.addWindow(subWindow);
      } else {
        Functions.notification("No collisions found!",
            "You can update the metadata in openBIS without overwriting something. To do so press 'Send to openBIS'",
            NotificationType.DEFAULT);
      }
      send.setEnabled(true);
    } else
      send.setEnabled(false);
  }

  protected void createConditionWindow(ComboBox box) {
    Window subWindow = new Window(" Experimental Condition Name");
    subWindow.setWidth("300px");
    // subWindow.setHeight("100px");

    VerticalLayout layout = new VerticalLayout();
    layout.setSpacing(true);
    layout.setMargin(true);
    TextField tf = new TextField();
    tf.setRequired(true);
    tf.setStyleName(Styles.fieldTheme);
    RegexpValidator factorLabelValidator = new RegexpValidator("[A-Za-z][_A-Za-z0-9]*",
        "Experimental variable must start with a letter and contain only letters, numbers or underscores ('_')");
    tf.setValidationVisible(true);
    tf.addValidator(factorLabelValidator);
    Button send = new Button("Ok");
    send.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        if (tf.isValid()) {
          String name = "Condition: " + tf.getValue();
          box.addItem(name);
          box.select(name);
          subWindow.close();
        } else {
          String error = "Please input a name for this condition.";
          if (!tf.isEmpty())
            error = factorLabelValidator.getErrorMessage();
          Functions.notification("Missing Input", error, NotificationType.DEFAULT);
        }
      }
    });
    layout.addComponent(tf);
    layout.addComponent(send);

    subWindow.setContent(layout);
    // Center it in the browser window
    subWindow.center();
    subWindow.setModal(true);
    subWindow.setIcon(FontAwesome.FLASK);
    subWindow.setResizable(false);
    ProjectwizardUI ui = (ProjectwizardUI) UI.getCurrent();
    ui.addWindow(subWindow);
  }

  private void styleTable() {
    // Set cell style generator
    sampleTable.setCellStyleGenerator(new Table.CellStyleGenerator() {

      @Override
      public String getStyle(Table source, Object itemId, Object propertyId) {
        String type = null;
        if (propertyId != null) {
          // barcode col
          if (propertyId.equals(barcodeColName))
            return "blue-hue1";
          // combobox col
          type = checkSelection(propertyId);
        }
        // not set yet
        if (type == null)
          return "red-hue";
        else {
          // check for data in openbis that would be overwritten
          String collision = getCollisionOrNull(propertyId, itemId);
          if (collision != null) {
            collisions.add(collision);
            return "yellow-hue";
          } else
            return "blue-hue1";
        }
      }
    });
  }

  private String parseLabelCell(int id, Object propertyId) {
    Item item = sampleTable.getItem(id);
    Label l = (Label) item.getItemProperty(propertyId).getValue();
    return l.getValue();
  }

  private void writeLabelCell(int id, Object propertyId, String text) {
    Item item = sampleTable.getItem(id);
    Label l = (Label) item.getItemProperty(propertyId).getValue();
    l.setValue(text);
  }

  private String getBarcodeInRow(int id) {
    Item item = sampleTable.getItem(id);
    String bc = (String) item.getItemProperty(barcodeColName).getValue();
    return bc;
  }

  protected String getCollisionOrNull(Object propertyId, Object itemId) {
    String type = checkSelection(propertyId);
    String res = null;

    int id = (int) itemId;
    if (id != -1) {
      String val = parseLabelCell(id, propertyId);
      String barcode = getBarcodeInRow(id);
      String openbisVal = "";
      if (type.startsWith("Condition: "))
        openbisVal =
            parseXMLConditionValue(codesToSamples.get(barcode).getProperties().get("Q_PROPERTIES"),
                type.replace("Condition: ", ""));
      else
        openbisVal = codesToSamples.get(barcode).getProperties().get(type);
      boolean empty = openbisVal == null || openbisVal.isEmpty() || val == null || val.isEmpty();
      boolean same = val != null && val.equals(openbisVal);
      boolean collision = (!empty && !same);
      if (collision) {
        res = barcode + ": " + openbisVal + " --> " + val + "\n";
      }
    }
    return res;
  }

  private String parseXMLConditionValue(String xml, String label) {
    List<Factor> factors = new ArrayList<Factor>();
    try {
      factors = xmlParser.getFactorsFromXML(xml);
    } catch (JAXBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    String res = "";
    for (Factor f : factors) {
      if (f.getLabel().equals(label)) {
        res = f.getValue();
        if (f.hasUnit())
          res += " " + f.getUnit();
      }
    }
    return res;
  }

  protected String checkSelection(Object propertyId) {
    Item item = sampleTable.getItem(-1);
    Object cell = item.getItemProperty(propertyId).getValue();
    if (cell instanceof ComboBox)
      return (String) ((ComboBox) cell).getValue();
    else
      return cell.toString();
  }

}
