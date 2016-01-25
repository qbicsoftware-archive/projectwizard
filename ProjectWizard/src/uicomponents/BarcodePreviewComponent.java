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
package uicomponents;

import java.util.ArrayList;
import java.util.Arrays;

import logging.Log4j2Logger;
import main.ProjectwizardUI;
import model.SampleToBarcodeFieldTranslator;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class BarcodePreviewComponent extends VerticalLayout {

  private logging.Logger logger = new Log4j2Logger(BarcodePreviewComponent.class);

  /**
   * 
   */
  private static final long serialVersionUID = -1785004617342053619L;
  TextField code;
  TextField info1;
  TextField info2;
  TextField person;
  TextField tel;
  private OptionGroup codedName;
  private ComboBox select1;
  private ComboBox select2;
  private CheckBox overwrite;
  // private TextField codedNameField;

  Sample example;
  SampleToBarcodeFieldTranslator translator;

  public BarcodePreviewComponent(SampleToBarcodeFieldTranslator translator) {
    this.translator = translator;
    setSpacing(true);
    setMargin(true);

    Resource res = new ThemeResource("img/qrtest.png");
    Image qr = new Image(null, res);
    Image qr2 = new Image(null, res);

    code = new TextField();
    info1 = new TextField();
    info2 = new TextField();
    person = new TextField("", "");
    tel = new TextField("", "QBiC: +4970712972163");

    codedName = new OptionGroup("Add IDs to code & files");
    codedName.addItems(Arrays.asList("QBiC ID", "Lab ID", "Secondary Name"));
    codedName.setImmediate(true);
    codedName.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
    codedName.select("QBiC ID");

    code.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
    code.setWidth("400px");
    styleInfoField(info1);
    styleInfoField(info2);
    styleInfoField(person);
    styleInfoField(tel);

    VerticalLayout box = new VerticalLayout();
    box.setHeight("110px");
    box.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
    box.addComponent(info1);
    box.addComponent(info2);
    box.addComponent(tel);
    box.addComponent(person);
    box.setWidth("168px");

    GridLayout grid = new GridLayout(3, 5);

    grid.addComponent(qr, 0, 2, 0, 4);
    grid.addComponent(qr2, 2, 2, 2, 4);
    grid.addComponent(code, 0, 0, 2, 0);
    grid.addComponent(box, 1, 1, 1, 4);
    grid.setColumnExpandRatio(0, 1);
    grid.setColumnExpandRatio(2, 1);
    setFieldsReadOnly(true);
    select1 =
        new ComboBox("First Info", new ArrayList<String>(Arrays.asList("Tissue/Extr. Material",
            "Secondary Name", "QBiC ID", "Lab ID")));
    select1.setStyleName(ProjectwizardUI.boxTheme);
    select1.setImmediate(true);
    select1.select("Tissue/Extr. Material");
    select2 =
        new ComboBox("Second Info", new ArrayList<String>(Arrays.asList("Tissue/Extr. Material",
            "Secondary Name", "QBiC ID", "Lab ID")));
    select2.select("Secondary Name");
    select2.setImmediate(true);
    select2.setStyleName(ProjectwizardUI.boxTheme);

    ValueChangeListener vc = new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = -7466519211904860012L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        refresh();
      }
    };
    codedName.addValueChangeListener(vc);
    select1.addValueChangeListener(vc);
    select2.addValueChangeListener(vc);

    HorizontalLayout designBox = new HorizontalLayout();
    designBox.addComponent(select1);
    designBox.addComponent(select2);
    designBox.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
    designBox.setSpacing(true);

    VerticalLayout previewBox = new VerticalLayout();
    previewBox.setWidth("430px");
    previewBox.setStyleName(ValoTheme.LAYOUT_CARD);
    previewBox.setCaption("Barcode Example");
    previewBox.addComponent(grid);

    addComponent(previewBox);
    addComponent(codedName);
    addComponent(designBox);

    overwrite = new CheckBox("Overwrite existing Tube Barcode Files");
    addComponent(ProjectwizardUI.questionize(overwrite,
        "Overwrites existing files of barcode stickers. This is useful when "
            + "the design was changed after creating them.", "Overwrite Sticker Files"));
  }

  private void styleInfoField(TextField tf) {
    tf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
    tf.setWidth("168px");
    tf.addStyleName("barcode-preview");
  }

  public void update(String s1, String s2) {
    info1.setValue(s1);
    info2.setValue(s2);
  }

  private void setFieldsReadOnly(boolean b) {
    person.setReadOnly(b);
    tel.setReadOnly(b);
    code.setReadOnly(b);
    info1.setReadOnly(b);
    info2.setReadOnly(b);
  }

  public void setExample(Sample sample) {
    example = sample;
    refresh();
  }

  public void refresh() {
    setFieldsReadOnly(false);
    code.setValue(example.getCode());
    code.setValue(getCodeString(example));
    info1.setValue(getInfo(select1, example));
    info2.setValue(getInfo(select2, example));
    setFieldsReadOnly(true);
  }

  public String getCodeString(Sample s) {
    return translator.getCodeString(s, (String) codedName.getValue());
  }

  public String getInfo(ComboBox b, Sample s) {
    return translator.buildInfo(b, s, null, true);
  }

  public String getInfo1(Sample s) {
    return getInfo(select1, s);
  }

  public String getInfo2(Sample s) {
    return getInfo(select2, s);
  }

  // private String buildInfo(ComboBox select, Sample s) {
  // Map<String, String> map = s.getProperties();
  // String in = "";
  // if (select.getValue() != null)
  // in = select.getValue().toString();
  // String res = "";
  // switch (in) {
  // case "Tissue/Extr. Material":
  // if (map.containsKey("Q_PRIMARY_TISSUE"))
  // res = map.get("Q_PRIMARY_TISSUE");
  // else
  // res = map.get("Q_SAMPLE_TYPE");
  // break;
  // case "Secondary Name":
  // res = map.get("Q_SECONDARY_NAME");
  // break;
  // case "QBiC ID":
  // res = s.getCode();
  // }
  // if (res == null)
  // return "";
  // return res.substring(0, Math.min(res.length(), 22));
  // }
  //
  // public String getCodeString(Sample sample) {
  // Map<String, String> map = sample.getProperties();
  // String res = "";
  // // @SuppressWarnings("unchecked")
  // // Set<String> selection = (Set<String>) codedName.getValue();
  // // for (String s : selection) {
  // String s = (String) codedName.getValue();
  // if (!res.isEmpty())
  // res += "_";
  // switch (s) {
  // case "QBiC ID":
  // res += sample.getCode();
  // break;
  // case "Secondary Name":
  // res += map.get("Q_SECONDARY_NAME");
  // break;
  // case "Lab ID":
  // res += map.get("Q_EXTERNALDB_ID");
  // break;
  // }
  // // }
  // res = fixFileName(res);
  // return res.substring(0, Math.min(res.length(), 21));
  // }
  //
  // private String fixFileName(String res) {
  // res = res.replace("null", "");
  // res = res.replace(";", "_");
  // res = res.replace("#", "_");
  // res = res.replace(" ", "_");
  // while (res.contains("__"))
  // res = res.replace("__", "_");
  // return res;
  // }

  public boolean overwrite() {
    return overwrite.getValue();
  }
}
