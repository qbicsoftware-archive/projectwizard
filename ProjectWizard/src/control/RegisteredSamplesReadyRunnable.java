package control;

import ui.UploadRegisterStep;

/**
 * Runnable that calls a method in the view after the background thread has finished registering samples
 * @author Andreas Friedrich
 *
 */
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
