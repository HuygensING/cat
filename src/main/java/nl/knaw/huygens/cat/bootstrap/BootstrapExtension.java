package nl.knaw.huygens.cat.bootstrap;

import nl.knaw.huygens.cat.AbstractExtension;
import org.concordion.api.extension.ConcordionExtender;

public class BootstrapExtension extends AbstractExtension {
  @Override
  public void addTo(ConcordionExtender concordionExtender) {
    linkCSS(concordionExtender, "/bootstrap/bootstrap.css");
    linkCSS(concordionExtender, "/bootstrap/enable-bootstrap.css");

    linkJavaScript(concordionExtender, "/jquery/jquery.min.js");
    linkJavaScript(concordionExtender, "/bootstrap/bootstrap.min.js");

    concordionExtender.withSpecificationProcessingListener(new FailedTestMarker());
  }
}
