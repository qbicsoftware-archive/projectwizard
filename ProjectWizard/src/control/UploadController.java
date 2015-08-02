package control;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import views.StandaloneTSVImport;

import logging.Log4j2Logger;
import main.OpenbisCreationController;
import main.SamplePreparator;
import model.ProjectInfo;

import com.vaadin.ui.Button;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;


public class UploadController {

  private StandaloneTSVImport view;
  private OpenbisCreationController openbisCreator;
  private ProjectInfo projectInfo;

  logging.Logger logger = new Log4j2Logger(UploadController.class);

  public UploadController(StandaloneTSVImport tsvImport, OpenbisCreationController creator) {
    view = tsvImport;
    this.openbisCreator = creator;
  }

  public void init() {
    final Uploader uploader = new Uploader();
    Upload upload = new Upload("Upload a tsv here", uploader);
    upload.setButtonCaption("Upload");
    // Listen for events regarding the success of upload.
    upload.addFailedListener(uploader);
    upload.addSucceededListener(uploader);
    FinishedListener uploadFinListener = new FinishedListener() {
      /**
       * 
       */
      private static final long serialVersionUID = -8413963075202260180L;

      public void uploadFinished(FinishedEvent event) {
        String error = uploader.getError();
        File file = uploader.getFile();
        view.resetSummary();
        if (file.getPath().endsWith("up_")) {
          String msg = "No file selected.";
          logger.warn(msg);
          view.setError(msg);
          if (!file.delete())
            logger.error("uploaded tmp file " + file.getAbsolutePath() + " could not be deleted!");
        } else {
          if (error == null || error.isEmpty()) {
            view.clearError();
            String msg = "Upload successful!";
            logger.info(msg);
            view.setError(msg);
            try {
              view.setRegEnabled(false);
              SamplePreparator prep = new SamplePreparator();
              if (prep.processTSV(file, false)) {
                view.setSummary(prep.getSummary());
                view.setProcessed(prep.getProcessed());
                view.setRegEnabled(true);
                projectInfo = prep.getProjectInfo();
              } else {
                logger.error("Error parsing tsv: " + prep.getError());
                view.setError(prep.getError());
              }
            } catch (IOException e) {
              e.printStackTrace();
            }
          } else {
            view.setError(error);
            if (!file.delete())
              logger
                  .error("uploaded tmp file " + file.getAbsolutePath() + " could not be deleted!");
          }
        }
      }
    };
    upload.addFinishedListener(uploadFinListener);
    view.initUpload(upload);

    Button.ClickListener cl = new Button.ClickListener() {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      /**
       * 
       */

      @Override
      public void buttonClick(ClickEvent event) {
        String src = event.getButton().getCaption();
        if (src.equals("Register All")) {
          view.getRegisterButton().setEnabled(false);
          openbisCreator.registerProjectWithExperimentsAndSamplesBatchWise(view.getSamples(),
              projectInfo.getDescription(), projectInfo.getSecondaryName(), view.getProgressBar(),
              view.getProgressLabel(), new RegisteredSamplesReadyRunnable(view));
        }
      }
    };
    view.getRegisterButton().addClickListener(cl);
  }

}
