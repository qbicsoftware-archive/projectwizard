package control;

import ui.BarcodeView;

public class BarcodesReadyRunnable implements Runnable {

  private BarcodeView view;

  public BarcodesReadyRunnable(BarcodeView view) {
    this.view = view;
  }

  @Override
  public void run() {
    view.creationDone();
  }
}
