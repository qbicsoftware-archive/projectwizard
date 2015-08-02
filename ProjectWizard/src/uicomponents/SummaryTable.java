package uicomponents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import properties.Factor;

import main.ProjectwizardUI;
import model.AOpenbisSample;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
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

  public SummaryTable(String name) {
    this.name = name;
    table = new Table(name);
  }

  public List<AOpenbisSample> getSamples() {
    List<AOpenbisSample> res = new ArrayList<AOpenbisSample>();
    for (Object id : table.getItemIds()) {
      String key = (String) table.getItem(id).getItemProperty("ID").getValue();
      AOpenbisSample s = map.get(key);
      String secName = parseSecName(table.getItem(id).getItemProperty("Secondary Name").getValue());
      s.setQ_SECONDARY_NAME(secName);
      if (secName == null)
        secName = "";
      if (!secName.equals("DELETED"))
        res.add(s);
    }
    return res;
  }

  private String parseSecName(Object value) {
    return ((TextField) value).getValue();
  }

  public void setPageLength(int size) {
    table.setPageLength(size);
  }

  public void removeAllItems() {
    removeComponent(table);
    map = new HashMap<String, AOpenbisSample>();
    table = new Table(name);
    addComponent(table);
  }

  public void initTable(List<AOpenbisSample> samples) {
    table.setStyleName(ProjectwizardUI.tableTheme);
    table.addContainerProperty("ID", String.class, null);
    table.setColumnWidth("ID", 35);
    table.addContainerProperty("Secondary Name", TextField.class, null);
    table.setImmediate(true);

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
    for (int i = 0; i < samples.size(); i++) {
      AOpenbisSample s = samples.get(i);
      String id = Integer.toString(i + 1);
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
