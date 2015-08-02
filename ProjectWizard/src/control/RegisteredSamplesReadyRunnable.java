package control;

import views.IRegistrationView;

/**
 * Class implementing the Runnable interface so it can be run and trigger a response in the view after the sample creation thread finishes
 * @author Andreas Friedrich
 *
 */
public class RegisteredSamplesReadyRunnable implements Runnable {

  private IRegistrationView view;

  public RegisteredSamplesReadyRunnable(IRegistrationView view) {
    this.view = view;
  }

  @Override
  public void run() {
    view.registrationDone();
  }
}
