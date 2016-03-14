/*******************************************************************************
 * QBiC Project Wizard enables users to create hierarchical experiments including different study
 * conditions using factorial design. Copyright (C) "2016" Andreas Friedrich
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package control;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import processes.RegisteredSamplesReadyRunnable;

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


public class ExperimentImportController {

  private StandaloneTSVImport view;
  private OpenbisCreationController openbisCreator;
  private ProjectInfo projectInfo;
  private List<Map<String, Object>> msProperties;
  private Map<String, Map<String, Object>> mhcProperties;

  logging.Logger logger = new Log4j2Logger(ExperimentImportController.class);

  public ExperimentImportController(StandaloneTSVImport tsvImport, OpenbisCreationController creator) {
    view = tsvImport;
    this.openbisCreator = creator;
  }

  public void init(final String user) {
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
                msProperties = prep.getMSExperimentOrNull();
                mhcProperties = prep.getMHCExperimentsOrNull();
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
          //TODO multiple ms experiments
          Map<String, Object> msProps = null;
          if(msProperties!=null)
            msProps = msProperties.get(0);
          openbisCreator.registerProjectWithExperimentsAndSamplesBatchWise(view.getSamples(),
              projectInfo.getDescription(), projectInfo.getSecondaryName(), msProps, mhcProperties, view
                  .getProgressBar(), view.getProgressLabel(), new RegisteredSamplesReadyRunnable(
                  view), user);
        }
      }
    };
    view.getRegisterButton().addClickListener(cl);
  }
}
