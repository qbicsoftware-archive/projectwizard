package uicomponents;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import properties.Factor;
import sun.awt.HorizBagLayout;

import main.ProjectwizardUI;
import model.AOpenbisSample;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import componentwrappers.StandardTextField;

/**
 * Table to summarize prepared samples, remove them or adapt their secondary names
 * 
 * @author Andreas Friedrich
 * 
 */
public class SummaryTable extends VerticalLayout {

  /**
   * 
   */
  private static final long serialVersionUID = 3220178619365365177L;
  private Table table;
  private Map<String, AOpenbisSample> map;
  private String name;
  private boolean isotopes = false;
  private LabelingMethod labelingMethod;
  private HorizontalLayout deleteNames;

  public SummaryTable(String name) {
    setSpacing(true);
    this.name = name;
    table = new Table(name);
    Button clear = new Button("Remove Secondary Names");
    clear.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        removeSecondaryNames();
      }
    });
    deleteNames = new HorizontalLayout();
    deleteNames.addComponent(clear);
  }

  public List<AOpenbisSample> getSamples() {
    List<AOpenbisSample> res = new ArrayList<AOpenbisSample>();
    for (Object id : table.getItemIds()) {
      String key = (String) table.getItem(id).getItemProperty("ID").getValue();
      AOpenbisSample s = map.get(key);
      String secName = parseSecName(id);
      s.setQ_SECONDARY_NAME(secName);
      if (secName == null)
        secName = "";
      if (!secName.equals("DELETED")) {
        if (isotopes) {
          String method = labelingMethod.getName();
          String value = parseLabel(method, id);
          if (value != null)
            s.addFactor(new Factor(method.toLowerCase(), value));
        }
        res.add(s);
      }
    }
    return res;
  }

  private String parseLabel(String name, Object id) {
    return (String) ((ComboBox) table.getItem(id).getItemProperty(name).getValue()).getValue();
  }

  private String parseSecName(Object id) {
    return ((TextField) table.getItem(id).getItemProperty("Secondary Name").getValue()).getValue();
  }

  public void setPageLength(int size) {
    table.setPageLength(size);
  }

  public void removeAllItems() {
    removeAllComponents();
    map = new HashMap<String, AOpenbisSample>();
    table = new Table(name);
    addComponent(table);
    addComponent(ProjectwizardUI
        .questionize(
            deleteNames,
            "If you don't want to keep any of the proposed secondary names you can use this button to delete all of them.",
            "Clear Secondary Names"));
  }

  private void removeSecondaryNames() {
    for (Object id : table.getItemIds()) {
      TextField tf = (TextField) table.getItem(id).getItemProperty("Secondary Name").getValue();
      if (!tf.getValue().equals("DELETED"))
        tf.setValue("");
    }
  }

  public void initTable(List<AOpenbisSample> samples, LabelingMethod labelingMethod) {
    if (labelingMethod != null) {
      this.labelingMethod = labelingMethod;
      isotopes = true;
    }
    table.setStyleName(ProjectwizardUI.tableTheme);
    table.addContainerProperty("ID", String.class, null);
    table.setColumnWidth("ID", 35);
    table.addContainerProperty("Secondary Name", TextField.class, null);
    table.setImmediate(true);

    if (isotopes)
      table.addContainerProperty(labelingMethod.getName(), ComboBox.class, null);

    List<String> factorLabels = new ArrayList<String>();
    int maxCols = 0;
    AOpenbisSample mostInformative = samples.get(0);
    for (AOpenbisSample s : samples) {
      int size = s.getFactors().size();
      if (size > maxCols) {
        maxCols = size;
        mostInformative = s;
      }
    }
    List<Factor> factors = mostInformative.getFactors();
    for (int i = 0; i < factors.size(); i++) {
      String l = factors.get(i).getLabel();

      int j = 2;
      while (factorLabels.contains(l)) {
        l = factors.get(i).getLabel() + " (" + Integer.toString(j) + ")";
        j++;
      }
      factorLabels.add(l);
      table.addContainerProperty(l, String.class, null);
    }

    table.addContainerProperty("Customize", Button.class, null);
    table.setColumnWidth("Customize", 85);

    List<String> reagents = null;
    if (isotopes)
      reagents = labelingMethod.getReagents();
    for (int i = 0; i < samples.size(); i++) {
      AOpenbisSample s = samples.get(i);
      String id = Integer.toString(i);
      map.put(id, s);

      // The Table item identifier for the row.
      Integer itemId = new Integer(i);

      // Create a button and handle its click.
      Button delete = new Button();
      ProjectwizardUI.iconButton(delete, FontAwesome.TRASH_O);
      // delete.setWidth("15px");
      // delete.setHeight("30px");
      delete.setData(itemId);
      delete.addClickListener(new Button.ClickListener() {
        /**
         * 
         */
        private static final long serialVersionUID = 5414603256990177472L;

        @Override
        public void buttonClick(ClickEvent event) {
          Button b = event.getButton();
          Integer iid = (Integer) b.getData();
          TextField tf =
              (TextField) table.getItem(iid).getItemProperty("Secondary Name").getValue();
          if (tf.getValue().equals("DELETED")) {
            tf.setReadOnly(false);
            String id = (String) table.getItem(iid).getItemProperty("ID").getValue();
            tf.setValue(map.get(id).getQ_SECONDARY_NAME());
            b.setIcon(FontAwesome.TRASH_O);
          } else {
            tf.setValue("DELETED");
            tf.setReadOnly(true);
            b.setIcon(FontAwesome.UNDO);
          }
        }
      });

      // Create the table row.
      List<Object> row = new ArrayList<Object>();
      row.add(id);
      TextField tf = new StandardTextField();
      tf.setImmediate(true);
      tf.setValue(s.getQ_SECONDARY_NAME());
      row.add(tf);
      if (isotopes) {
        ComboBox cb = new ComboBox();
        cb.setImmediate(true);
        cb.addItems(reagents);
        cb.select(reagents.get(i % reagents.size()));
        row.add(cb);
      }
      int missing = maxCols - s.getFactors().size();
      for (Factor f : s.getFactors()) {
        String v = f.getValue();
        if (f.hasUnit())
          v += " " + f.getUnit();
        row.add(v);
      }
      for (int j = 0; j < missing; j++)
        row.add("");
      row.add(delete);
      table.addItem(row.toArray(new Object[row.size()]), itemId);
    }
  }
}
