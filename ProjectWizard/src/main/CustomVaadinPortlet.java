package main;

import java.io.IOException;

import javax.portlet.PortletException;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinPortlet;
import com.vaadin.server.VaadinPortletService;
import com.vaadin.server.VaadinRequest;

/**
 * 
 * copied from:
 * https://github.com/jamesfalkner/vaadin-liferay-beacon-demo/blob/master/src/main/java/
 * com/liferay/mavenizedbeacons/CustomVaadinPortlet.java This custom Vaadin portlet allows for
 * serving Vaadin resources like theme or widgetset from its web context (instead of from ROOT).
 * Usually it doesn't need any changes.
 * 
 */
public class CustomVaadinPortlet extends VaadinPortlet {

  private class CustomVaadinPortletService extends VaadinPortletService {
    /**
     *
     */

    public CustomVaadinPortletService(final VaadinPortlet portlet,
        final DeploymentConfiguration config) throws ServiceException {
      super(portlet, config);
    }

    /**
     * This method is used to determine the uri for Vaadin resources like theme or widgetset. It's
     * overriden to point to this web application context, instead of ROOT context
     */
    @Override
    public String getStaticFileLocation(final VaadinRequest request) {
      //return super.getStaticFileLocation(request);
      // self contained approach:
       return request.getContextPath();
    }
  }

  @Override
  protected void doDispatch(javax.portlet.RenderRequest request,
      javax.portlet.RenderResponse response) throws javax.portlet.PortletException,
      java.io.IOException {
    super.doDispatch(request, response);
  }

  @Override
  public void serveResource(javax.portlet.ResourceRequest request,
      javax.portlet.ResourceResponse response) throws PortletException, IOException {
      super.serveResource(request, response);
  }


  @Override
  protected VaadinPortletService createPortletService(
      final DeploymentConfiguration deploymentConfiguration) throws ServiceException {
    final CustomVaadinPortletService customVaadinPortletService =
        new CustomVaadinPortletService(this, deploymentConfiguration);
    customVaadinPortletService.init();
    return customVaadinPortletService;
  }
}
