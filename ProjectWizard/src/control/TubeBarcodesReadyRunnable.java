package control;

import java.util.List;

import main.BarcodeCreator;
import model.IBarcodeBean;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;

import views.WizardBarcodeView;

/**
 * Class implementing the Runnable interface so it can trigger a response in the view after the
 * barcode creation thread finishes
 * 
 * @author Andreas Friedrich
 * 
 */
public class TubeBarcodesReadyRunnable implements Runnable {

  private WizardBarcodeView view;
  private List<IBarcodeBean> barcodeBeans;
  BarcodeCreator creator;

  public TubeBarcodesReadyRunnable(WizardBarcodeView view, BarcodeCreator creator,
      List<IBarcodeBean> barcodeBeans) {
    this.view = view;
    this.barcodeBeans = barcodeBeans;
    this.creator = creator;
  }

  private void attachDownloadToButton() {
    FileResource pdfSource = creator.zipAndDownloadBarcodes(barcodeBeans);
    FileDownloader pdfDL = new FileDownloader(pdfSource);
    pdfDL.extend(view.getButtonTube());
  }

  @Override
  public void run() {
    attachDownloadToButton();
    view.creationDone();
    view.tubesReady();
  }
}
