package processes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;

import concurrency.UpdateProgressBar;
import control.AttachmentConfig;

import logging.Log4j2Logger;
import main.ProjectwizardUI;
import model.AttachmentInformation;
import model.IBarcodeBean;

/**
 * Provides methods to move uploaded attachments to the datamover folder on the portal and create
 * marker files to start movement to the DSS.
 * 
 * @author Andreas Friedrich
 * 
 */
public class AttachmentMover {

  private String pathVariable;
  private AttachmentConfig config;
  private logging.Logger logger = new Log4j2Logger(AttachmentMover.class);

  /**
   * Create a new AttachmentMover
   * 
   * @param pathVariable Variable containing the path to binaries on the server, needed for global
   *        calls in the python scripts
   */
  public AttachmentMover(String pathVariable, AttachmentConfig attachmentConfig) {
    this.pathVariable = pathVariable;
    if (!pathVariable.endsWith("/"))
      this.pathVariable += "/";
    this.config = attachmentConfig;
  }

  /**
   * Moves attachments to the datamover folder.
   * 
   * @param attachments List of names and other infos for each attachment
   * @param moveUploadsReadyRunnable
   * @param object2
   * @param object
   * @return
   */
  public void moveAttachments(final List<AttachmentInformation> attachments, final ProgressBar bar,
      final Label info, final MoveUploadsReadyRunnable ready) {
    final Sardine sardine = SardineFactory.begin(config.getUser(), config.getPass());
    final int todo = attachments.size();
    if (todo > 0) {
      Thread t = new Thread(new Runnable() {
        volatile int current = 0;

        @Override
        public void run() {
          for (AttachmentInformation a : attachments) {
            current++;
            double frac = current * 1.0 / todo;
            UI.getCurrent().access(new UpdateProgressBar(bar, info, frac));
            byte[] data;
            try {
              data =
                  FileUtils.readFileToByteArray(new File(ProjectwizardUI.tmpFolder + a.getName()));
              sardine.put(config.getUri() + a.getTargetFileName(), data);
            } catch (IOException e1) {
              e1.printStackTrace();
            }
          }
          UI.getCurrent().access(ready);
          UI.getCurrent().setPollInterval(-1);
          try {
            createMarkers(attachments);
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
      t.start();
      UI.getCurrent().setPollInterval(100);
    } else {
      UI.getCurrent().access(ready);
    }
  }

  private void createMarkers(List<AttachmentInformation> attachments) throws InterruptedException,
      IOException {
    Sardine sardine = SardineFactory.begin(config.getUser(), config.getPass());
    File marker = new File(ProjectwizardUI.tmpFolder + "marker");
    if (!marker.exists())
      marker.createNewFile();
    String prefix = ".MARKER_is_finished_";
    for (AttachmentInformation a : attachments) {
      byte[] data;
      while (!sardine.exists(config.getUri() + a.getTargetFileName()));
      Thread.sleep(100);
      data = FileUtils.readFileToByteArray(marker);
      sardine.put(config.getUri() + prefix + a.getTargetFileName(), data);
    }
  }
}
