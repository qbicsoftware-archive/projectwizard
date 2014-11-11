package control;

import ui.BarcodeView;
import ui.UploadRegisterStep;

public class RegisteredSamplesReadyRunnable implements Runnable {

  private UploadRegisterStep view;

  public RegisteredSamplesReadyRunnable(UploadRegisterStep view) {
    this.view = view;
  }

  @Override
  public void run() {
    view.registrationDone();
  }
}
