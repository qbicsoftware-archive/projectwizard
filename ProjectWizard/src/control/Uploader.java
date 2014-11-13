package control;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import main.ProjectwizardUI;

import com.vaadin.ui.Panel;
import com.vaadin.ui.Upload;

/**
 * Class handling the TSV upload
 * @author Andreas Friedrich
 *
 */
@SuppressWarnings("serial")
public class Uploader implements Upload.SucceededListener, Upload.FailedListener, Upload.Receiver {

  Panel root; // Root element for contained components.
  File file; // File to write to.
  String error;

  public Uploader() {}

  // Callback method to begin receiving the upload.
  public OutputStream receiveUpload(String filename, String MIMEType) {
    FileOutputStream fos = null; // Output stream to write to
    file = new File(ProjectwizardUI.tmpFolder + filename);
    if (!MIMEType.equals("text/plain") && !MIMEType.equals("text/tab-separated-values")) {
      error = "Wrong File type. Please only upload tsv or txt files.";
    }
    try {
      // Open the file for writing.
      fos = new FileOutputStream(file);
    } catch (FileNotFoundException e) {
      // Error while opening the file. Not reported here.
      e.printStackTrace();
      return null;
    }
    return fos; // Return the output stream to write to
  }

  public String getError() {
    return error;
  }

  public File getFile() {
    return file;
  }

  // This is called if the upload is finished.
  public void uploadSucceeded(Upload.SucceededEvent event) {
    // Display the uploaded file in the image panel.
    System.out.println("Uploading " + event.getFilename() + " of type '" + event.getMIMEType()
        + "' successful.");
  }

  // This is called if the upload fails.
  public void uploadFailed(Upload.FailedEvent event) {
    // Log the failure on screen.
    System.out.println("Uploading " + event.getFilename() + " of type '" + event.getMIMEType()
        + "' failed.");
  }
}
