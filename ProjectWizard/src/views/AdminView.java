package views;

import com.vaadin.ui.Button;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class AdminView extends VerticalLayout {
  
  TabSheet tabs;
  TextField space;
  Button createSpace;
  
  public AdminView() {
    tabs = new TabSheet();
    
    VerticalLayout spaceView = new VerticalLayout();
    space = new TextField("Space");
    createSpace = new Button("Create Space");    
    spaceView.addComponent(space);
    spaceView.addComponent(createSpace);
    
    tabs.addTab(spaceView, "Create Space");
    
    VerticalLayout add  = new VerticalLayout();
    space = new TextField("Space");
    createSpace = new Button("Create Space");    
    spaceView.addComponent(space);
    spaceView.addComponent(createSpace);
    
    tabs.addTab(spaceView, "Create Space");
    
  }
  
  public String getSpace() {
    return space.getValue();
  }
  
  public Button getCreateSpace() {
    return createSpace;
  }

}
