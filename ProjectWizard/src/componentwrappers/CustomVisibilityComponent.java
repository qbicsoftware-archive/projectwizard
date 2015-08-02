package componentwrappers;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.OptionGroup;

import control.VisibilityChangeListener;

public class CustomVisibilityComponent extends AbstractComponent {

  private List<VisibilityChangeListener> listeners = new ArrayList<VisibilityChangeListener>();

  protected AbstractComponent inner;

  public CustomVisibilityComponent(String caption, AbstractComponent comp) {
    this.setCaption(caption);
    this.inner = comp;
  }

  public CustomVisibilityComponent(String caption, AbstractComponent comp, String width) {
    this(caption, comp);
    comp.setWidth(width);
  }

  public CustomVisibilityComponent(AbstractComponent comp) {
    this.inner = comp;
  }

//  public Class<String> getType() {
//    return String.class;
//  }

  public AbstractComponent getInnerComponent() {
    return inner;
  }

  public void setEnabled(boolean b) {
    inner.setEnabled(b);
  }

  public void setSize(String width, String height) {
    inner.setHeight(height);
    inner.setWidth(width);
  }

  public String getValue() {
    if (inner instanceof AbstractField)
      return (String) ((AbstractField) inner).getValue();
    else
      return null;
  }


  public void addListener(VisibilityChangeListener toAdd) {
    listeners.add(toAdd);
  }

  @Override
  public void setVisible(boolean b) {
    inner.setVisible(b);
    // Notify everybody that may be interested.
    for (VisibilityChangeListener hl : listeners)
      hl.setVisible(b);
  }

  public void setNullSelectionAllowed(boolean b) {
    if (inner instanceof AbstractSelect)
      ((AbstractSelect) inner).setNullSelectionAllowed(b);
  }

  public void setItemEnabled(String string, boolean b) {
    if (inner instanceof OptionGroup)
      ((OptionGroup) inner).setItemEnabled(string, b);
  }

  public Object getNullSelectionItemId() {
    if (inner instanceof AbstractSelect)
      return ((AbstractSelect) inner).getNullSelectionItemId();
    return null;
  }

  public void select(Object item) {
    if (inner instanceof AbstractSelect)
      ((AbstractSelect) inner).select(item);
  }
}
