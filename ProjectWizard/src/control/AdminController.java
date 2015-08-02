package control;

import java.util.List;
import java.util.Map;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import logging.Log4j2Logger;
import main.OpenBisClient;
import views.AdminView;

public class AdminController {

  private AdminView view;
  private OpenBisClient openbis;
  private Map<String, String> taxMap;
  private Map<String, String> tissueMap;
  private List<String> measureTypes;
  private List<String> spaces;
  
  logging.Logger logger = new Log4j2Logger(AdminController.class);

  public AdminController(OpenBisClient openbis, Map<String, String> taxMap,
      Map<String, String> tissueMap, List<String> sampleTypes, List<String> spaces) {
    this.taxMap = taxMap;
    this.openbis = openbis;
    this.tissueMap = tissueMap;
    this.measureTypes = sampleTypes;
    this.spaces = spaces;
    this.view = new AdminView();
    init();
  }

  private void init() {
    Button.ClickListener cl = new Button.ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        // openbis.
        view.getSpace();
      }

    };
    view.getCreateSpace().addClickListener(cl);
  }

  public AdminView getView() {
    return view;
  }

}
