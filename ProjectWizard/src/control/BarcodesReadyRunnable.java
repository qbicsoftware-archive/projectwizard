package control;

import ui.BarcodeView;

/**
 * Runnable that calls a method in the view after the background thread has finished creating barcodes
 * @author Andreas Friedrich
 *
 */
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
