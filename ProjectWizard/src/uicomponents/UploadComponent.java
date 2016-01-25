/*******************************************************************************
 * QBiC Project Wizard enables users to create hierarchical experiments including different study conditions using factorial design.
 * Copyright (C) "2016"  Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package uicomponents;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import logging.Log4j2Logger;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;

public class UploadComponent extends VerticalLayout implements Upload.SucceededListener,
    Upload.FailedListener, Upload.Receiver, Upload.ProgressListener, Upload.FinishedListener,
    StartedListener {

  protected Upload upload;
  protected String directory;
  protected String targetPrefix;
  protected File file;
  protected long maxSize; // In bytes. 100Kb = 100000
  protected ProgressBar progressIndicator; // May be null
  protected boolean cancelled = false;
  protected Long contentLength;
  protected Button cancelProcessing;
  protected HorizontalLayout processingLayout;

  private logging.Logger logger = new Log4j2Logger(UploadComponent.class);
  private boolean success;

  public UploadComponent(String fieldCaption, String buttonCaption, String directoryParam,
      String targetPrefix, int maxSize) {
    upload = new Upload(fieldCaption, null);
    this.addComponent(upload);
    this.maxSize = maxSize;
    upload.setReceiver(this);
    this.directory = directoryParam;
    this.targetPrefix = targetPrefix;
    upload.setButtonCaption(buttonCaption);
    upload.addSucceededListener(this);
    upload.addFailedListener(this);
    upload.addProgressListener(this);
    upload.addFinishedListener(this);
    upload.addStartedListener(this);

    processingLayout = new HorizontalLayout();
    processingLayout.setSpacing(true);
    processingLayout.setVisible(false);
    this.addComponent(processingLayout);

    progressIndicator = new ProgressBar();
    progressIndicator.setWidth("100%");
    processingLayout.addComponent(progressIndicator);

    cancelProcessing = new Button("cancel", new Button.ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        cancelled = true;
        upload.interruptUpload();
      }
    });
    cancelProcessing.setStyleName("small");
    processingLayout.addComponent(cancelProcessing);
  }

  @Override
  public OutputStream receiveUpload(String filename, String MIMEType) {
    FileOutputStream fos = null;
    file = new File(directory, targetPrefix + filename);

    try {
      fos = new FileOutputStream(file);
    } catch (final java.io.FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    return fos; // Return the output stream to write to
  }

  @Override
  public void updateProgress(long readBytes, long contentLength) {
    this.contentLength = contentLength;
    if (maxSize < contentLength) {
      upload.interruptUpload();
      return;
    }

    processingLayout.setVisible(true);
    progressIndicator.setValue(new Float(readBytes / (float) contentLength));
  }

  @Override
  public void uploadFinished(FinishedEvent event) {
    processingLayout.setVisible(false);
  }


  @Override
  public void uploadFailed(FailedEvent event) {
    processingLayout.setVisible(false);
    if (contentLength != null && maxSize < contentLength) {
      showNotification("File too large", "Your file is " + contentLength / 1000
          + "Kb long. Maximum file size is " + maxSize / 1000 + "Kb");
      logger.info("Upload was cancelled due to file exceeding size limit.");
    } else if (cancelled) {
      // Nothing to do...
    } else {
      logger.error("Upload cancelled due to error.");
      logger.error("event.getReason().getStackTrace().toString()");
      showNotification("There was a problem uploading your file.", event.getReason()
          .getStackTrace().toString());
    }

    try {
      file.delete();
    } catch (Exception e) {
      // Silent exception. If we can't delete the file, it's not big problem. May the file did not
      // even exist.
    }
  }

  /**
   * File upload is a special case because the Nav7 is not initialized, because the file upload
   * requests don't go through the Vaadin servlet.
   */
  protected void showNotification(String message, String detail) {
    Notification n = new Notification(message, detail);
    n.setDelayMsec(-1);
    n.show(UI.getCurrent().getPage());
  }

  public String getDirectory() {
    return directory;
  }

  public File getFile() {
    return file;
  }

  @Override
  public void uploadSucceeded(SucceededEvent event) {
    success = true;
  }

  public void addFinishedListener(FinishedListener listener) {
    upload.addFinishedListener(listener);
  }

  public boolean wasSuccess() {
    return success;
  }

  @Override
  public void uploadStarted(StartedEvent event) {
    success = false;
  }

}
