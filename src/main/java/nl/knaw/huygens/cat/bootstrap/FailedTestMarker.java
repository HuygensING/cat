package nl.knaw.huygens.cat.bootstrap;

import java.util.List;

import com.google.common.collect.Lists;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.cat.SpecificationProcessingAdapter;
import org.concordion.api.Element;
import org.concordion.api.listener.SpecificationProcessingEvent;

class FailedTestMarker extends SpecificationProcessingAdapter {
  private final List<String> failedTests = Lists.newArrayList();

  @Override
  public void afterProcessingSpecification(SpecificationProcessingEvent event) {
    final Element root = event.getRootElement();
    if (hasFailure(root)) {
      Log.trace("Test has failure(s): {}", failedTests);
      markFailedTests(root);
    }
  }

  private void markFailedTests(Element element) {
    if (isAnchor(element)) {
      // <a href="#test-id">some text</a> -> id='test-id'
      final String id = element.getAttributeValue("href").substring(1);
      if (failedTests.contains(id)) {
        // <a href="#test-id">some text</a> -> <a ...>some text <span class="badge">Failed</span></a>
        element.appendNonBreakingSpace().appendChild(createFailureBadge());
      }
    }

    // Recursive visit to find other test references
    for (Element child : element.getChildElements()) {
      markFailedTests(child);
    }
  }

  private Element createFailureBadge() {
    Element failureBadge = new Element("span");
    failureBadge.addAttribute("class", "badge");
    failureBadge.appendText("Failed");
    return failureBadge;
  }

  private boolean hasFailure(Element element) {
    // End the recursion if we can pinpoint a failure, such as <code class='failure'>
    if (hasClass(element) && classOf(element).contains("failure")) {
      return true;
    }

    // Recursive visit to see if there is a failure deeper down the tree
    boolean foundFailure = false;
    for (final Element child : element.getChildElements()) {
      if (hasFailure(child)) {
        foundFailure = true;
        // mark which test failed for later use to decorate the 'index pills'
        if (isTestContentDiv(element)) {
          failedTests.add(idOf(element));
          // and while we are here, might as well add some markup to the panel
          final Element panel = element.getFirstChildElement("div");
          panel.addAttribute("class", classOf(panel).replace("panel-default", "panel-danger"));
        }
      }
    }

    return foundFailure;
  }

  private String classOf(Element div) {
    return div.getAttributeValue("class");
  }

  private boolean hasClass(Element element) {
    return classOf(element) != null;
  }

  private boolean hasId(Element element) {
    return idOf(element) != null;
  }

  private String idOf(Element element) {
    return element.getAttributeValue("id");
  }

  private boolean isAnchor(Element element) {
    return localName(element).equals("a");
  }

  private boolean isDiv(Element element) {
    return localName(element).equals("div");
  }

  private boolean isTestContentDiv(Element element) {
    return isDiv(element) && hasId(element);
  }

  private String localName(Element element) {
    return element.getLocalName();
  }
}
