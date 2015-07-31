package nl.knaw.huygens.cat.bootstrap;

import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.Maps;
import nl.knaw.huygens.Log;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import org.concordion.api.listener.DocumentParsingListener;

class NavigationBuilder implements DocumentParsingListener {
  @Override
  public void beforeParsing(Document document) {
    installNavigationElements(document);
  }

  private void installNavigationElements(Document document) {
    final Element body = document.getRootElement().getFirstChildElement("body");

    final Element container = createDiv("container");

    Attribute suiteDesc = body.getAttribute("data-desc");
    if (suiteDesc != null) {
      body.removeAttribute(suiteDesc);
      container.appendChild(createJumbotron(suiteDesc.getValue()));
    }

    Element row = createDiv("row");
    container.appendChild(row);

    Element colMenu = createDiv("col-md-3");
    row.appendChild(colMenu);

    Element colContent = createDiv("col-md-9");
    row.appendChild(colContent);

    Element tabContentDiv = createDiv("tab-content");
    colContent.appendChild(tabContentDiv);

    Map<String, Element> identifiedDivs = Maps.newLinkedHashMap();
    Elements testDivs = body.getChildElements("div");
    for (int i = 0; i < testDivs.size(); i++) {
      Element testDiv = testDivs.get(i);
      final Attribute id = testDiv.getAttribute("id");
      final Attribute description = testDiv.getAttribute("data-desc");
      identifiedDivs.put(id.getValue(), testDiv);

      if (i == 0) {
        testDiv.addAttribute(createClassAttribute("tab-pane fade in active"));
      } else {
        testDiv.addAttribute(createClassAttribute("tab-pane"));
      }

      testDiv.detach();
      tabContentDiv.appendChild(testDiv);

      Element panel = createDiv("panel panel-default");

      Element panelHeading = createDiv("panel-heading");
      panel.appendChild(panelHeading);

      Element bold = new Element("b");  // TODO: move to css
      panelHeading.appendChild(bold);
      bold.appendChild(description.getValue());

      Element panelBody = createDiv("panel-body");
      panel.appendChild(panelBody);

      final Nodes nodes = testDiv.removeChildren();
      for (int j = 0; j < nodes.size(); j++) {
        panelBody.appendChild(nodes.get(j));
      }
      testDiv.appendChild(panel);
    }
    Log.trace("Identified divs: {}", identifiedDivs);

    Element ul = new Element("ul");
    ul.addAttribute(createClassAttribute("nav nav-pills nav-stacked"));
    colMenu.appendChild(ul);
    ul.appendChild("\n");
    identifiedDivs.forEach(new BiConsumer<String, Element>() {
      private boolean first = true;

      @Override
      public void accept(String id, Element testDiv) {
        final Element li = new Element("li");
        if (first) {
          first = false;
          li.addAttribute(new Attribute("class", "active"));
        }
        final Element a = new Element("a");
        a.addAttribute(createAttribute("href", String.format("#%s", id)));
        a.addAttribute(createAttribute("data-toggle", "tab"));
        String desc = testDiv.getAttributeValue("data-desc");
//      testDiv.removeAttribute(description);
        a.appendChild(ofNullable(desc).orElseGet(() -> String.format("#%s", id)));
        Log.trace("testDiv: {}", testDiv.toXML());
        Log.trace("query yields: size={}", testDiv.query(".//*[@class]").size());
        if (testDiv.query("//*[@class='failure']").size() > 0) {
          Element spanFailed = new Element("span");
          spanFailed.addAttribute(createClassAttribute("badge"));
          spanFailed.appendChild("Failed!");
          a.appendChild(spanFailed);
        }
        li.appendChild(a);
        ul.appendChild("  ");
        ul.appendChild(li);
        ul.appendChild("\n");
      }
    });
    body.insertChild("\n", 0);
    body.insertChild(container, 1);
  }

  private Element createJumbotron(String description) {
    return createDiv("jumbotron", createH1(description));
  }

  private Element createDiv(String divClass, Element... children) {
    final Element div = createDiv();

    div.addAttribute(createClassAttribute(divClass));

    for (Element child : children) {
      div.appendChild(child);
    }

    return div;
  }

  private Element createDiv() {
    return new Element("div");
  }

  private Element createH1(String description) {
    Element h1 = new Element("h1");
    h1.appendChild(description);
    return h1;
  }

  private Attribute createClassAttribute(String className) {
    return createAttribute("class", className);
  }

  private Attribute createAttribute(String localName, String value) {
    return new Attribute(localName, value);
  }
}
