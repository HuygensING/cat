package nl.knaw.huygens.cat;

import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;

public abstract class AbstractExtension implements ConcordionExtension {

  protected void linkCSS(ConcordionExtender extender, String location) {
    extender.withLinkedCSS(location, resource(location));
  }

  protected void linkJavaScript(ConcordionExtender extender, String location) {
    extender.withLinkedJavaScript(location, resource(location));
  }

  private Resource resource(String location) {
    return new Resource(location);
  }
}
