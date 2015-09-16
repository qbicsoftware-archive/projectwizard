package processes;

import steps.FinishStep;

public class MoveUploadsReadyRunnable implements Runnable {

  FinishStep view;

  public MoveUploadsReadyRunnable(FinishStep view) {
    this.view = view;
  }

  @Override
  public void run() {
    view.fileCommitDone();
  }

}
