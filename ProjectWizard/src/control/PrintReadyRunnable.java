package control;

import model.IReadyRunnable;
import views.WizardBarcodeView;

public class PrintReadyRunnable implements IReadyRunnable {

  private WizardBarcodeView view;
  private boolean success = false;

  public PrintReadyRunnable(WizardBarcodeView view) {
    this.view = view;
  }

  @Override
  public void run() {
    view.printCommandsDone(this);
  }

  @Override
  public boolean wasSuccess() {
    return success;
  }

  @Override
  public void setSuccess(boolean b) {
    this.success = b;
  }

}
