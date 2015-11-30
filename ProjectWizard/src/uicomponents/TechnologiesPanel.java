package uicomponents;

import java.util.ArrayList;
import java.util.List;

import main.ProjectwizardUI;
import model.TestSampleInformation;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;

/**
 * Composite UI component for inputting an arbitrary number of experimental conditions
 * 
 * @author Andreas Friedrich
 * 
 */
public class TechnologiesPanel extends HorizontalLayout {

  private static final long serialVersionUID = -1578503116738309380L;
  List<String> options;
  List<TechChooser> choosers;
  Button.ClickListener buttonListener;
  ValueChangeListener poolListener;
  ValueChangeListener proteinListener;
  GridLayout buttonGrid;
  Button add;
  Button remove;

  OptionGroup conditionsSet;

  /**
   * Create a new Conditions Panel component to select experimental conditions
   * 
   * @param techOptions List of different possible conditions
   * @param conditionsSet (empty) option group that makes it possible to listen to the conditions
   *        inside this component from the outside
   * @param proteinListener
   */
  public TechnologiesPanel(List<String> techOptions, OptionGroup conditionsSet,
      ValueChangeListener poolListener, ValueChangeListener proteinListener) {
    this.options = techOptions;

    this.conditionsSet = conditionsSet;
    this.conditionsSet.addItem("set");
    add = new Button();
    remove = new Button();
    ProjectwizardUI.iconButton(add, FontAwesome.PLUS_SQUARE);
    ProjectwizardUI.iconButton(remove, FontAwesome.MINUS_SQUARE);
    initListener();
    this.poolListener = poolListener;
    this.proteinListener = proteinListener;

    choosers = new ArrayList<TechChooser>();
    TechChooser c = new TechChooser(techOptions);
    c.setImmediate(true);
    c.addPoolListener(poolListener);
    c.addProteinListener(proteinListener);
    choosers.add(c);
    addComponent(c);
    c.showHelpers();

    buttonGrid = new GridLayout(1, 2);
    buttonGrid.setSpacing(true);
    buttonGrid.addComponent(add);
    buttonGrid.addComponent(remove);
    addComponent(buttonGrid);
    setSpacing(true);
  }

  private void initListener() {
    buttonListener = new Button.ClickListener() {

      private static final long serialVersionUID = 2240224129259577437L;

      @Override
      public void buttonClick(ClickEvent event) {
        if (event.getButton().equals(add))
          add();
        else
          remove();
      }
    };
    add.addClickListener(buttonListener);
    remove.addClickListener(buttonListener);
  }

  public boolean poolingSet() {
    boolean res = false;
    for (TechChooser c : choosers) {
      res |= c.poolingSet();
    }
    return res;
  }

  public List<TestSampleInformation> getTechInfo() {
    List<TestSampleInformation> res = new ArrayList<TestSampleInformation>();
    for (TechChooser c : choosers) {
      if (c.isSet())
        res.add(c.getChosenTechInfo());
    }
    return res;
  }

  private void add() {
    choosers.get(choosers.size() - 1).hideHelpers();
    TechChooser c = new TechChooser(options);
    c.addPoolListener(poolListener);
    c.addProteinListener(proteinListener);
    choosers.add(c);

    c.showHelpers();
    removeComponent(buttonGrid);
    addComponent(c);
    addComponent(buttonGrid);
  }

  private void remove() {
    int size = choosers.size();
    if (size > 1) {
      TechChooser last = choosers.get(size - 1);
      last.reset();
      removeComponent(last);
      choosers.remove(last);
      last.removePoolListener(poolListener);
      last.removeProteinListener(proteinListener);
      choosers.get(size - 2).showHelpers();
    }
  }

  public boolean isValid() {
    for (TechChooser c : choosers) {
      if (c.isSet())
        return true;
    }
    return false;
  }

  public void resetInputs() {
    for (TechChooser c : choosers) {
      c.reset();
    }
  }

}
