package componentwrappers;

import com.vaadin.ui.TextField;

public class StandardTextField extends TextField {

  /**
   * 
   */
  private static final long serialVersionUID = -7931461514790712645L;

  public StandardTextField(String caption) {
    super(caption);
    super.setHeight("31px");
  }

  public StandardTextField(String label, String value) {
    super(label, value);
    super.setHeight("31px");
  }

  public StandardTextField() {
    super();
    super.setHeight("31px");
  }

}
