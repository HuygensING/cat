package nl.knaw.huygens.cat.bootstrap;

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
    final Element body = bodyOf(document);
    final Elements testDivs = childDivsOf(body); // get these now, before we alter the structure of body

    insertJumbotron(body);

    final Element container = appendDiv(body, "container");
    final Element row = appendDiv(container, "row");
    final Element tabOverview = appendUl(appendDiv(row, "col-md-3"), "nav nav-pills nav-stacked");
    final Element tabContent = appendDiv(appendDiv(row, "col-md-9"), "tab-content");

    for (int i = 0; i < testDivs.size(); i++) {
      final Element testDiv = testDivs.get(i);
      addClass(testDiv, i == 0 ? "tab-pane fade active in" : "tab-pane fade");

      restructureTest(testDiv, tabOverview, tabContent, i == 0);
    }
  }

  private void restructureTest(Element testDiv, Element tabOverview, Element tabContent, boolean isActive) {
    final Element li = appendElement(tabOverview, "li");
    if (isActive) {
      addClass(li, "active");
    }

    final Element a = appendElement(li, "a");
    addAttribute(a, "href", "#" + testDiv.getAttributeValue("id"));
    addAttribute(a, "data-toggle", "tab");
    a.appendChild(testDiv.getAttributeValue("data-desc"));

    if (indicatesFailure(testDiv)) {
      final Element failureBadge = appendElement(a, "span");
      addAttribute(failureBadge, "class", "badge");
      failureBadge.appendChild("Failed");
    }

    // Relocate testDiv from body to tab's content pane
    testDiv.detach();
    tabContent.appendChild(testDiv);

    restructureIntoPanel(testDiv);
  }

  private boolean indicatesFailure(Element element) {
    return element.query("//*[@class='failure']").size() > 0;
  }

  private void insertJumbotron(Element body) {
    Attribute suiteDesc = body.getAttribute("data-desc");
    if (suiteDesc != null) {
      body.removeAttribute(suiteDesc);
      createJumbotron(body, suiteDesc.getValue());
    }
  }

  private Elements childDivsOf(Element body) {
    return body.getChildElements("div");
  }

  private void restructureIntoPanel(Element element) {
    // First unlink all the children
    final Nodes nodes = element.removeChildren();

    // Create a panel with heading and body inside the element
    final Element panel = appendDiv(element, "panel panel-default");
    final Element panelHeading = appendDiv(panel, "panel-heading");
    final Element panelBody = appendDiv(panel, "panel-body");

    // Transfer the description from the element to its heading
    final Attribute description = element.getAttribute("data-desc");
    element.removeAttribute(description);
    appendElement(panelHeading, "b").appendChild(description.getValue());

    // Finally, relocate the children inside the panel-body
    for (int j = 0; j < nodes.size(); j++) {
      panelBody.appendChild(nodes.get(j));
    }
  }

  private Element bodyOf(Document document) {
    return document.getRootElement().getFirstChildElement("body");
  }

  // Utility element creation cruft down here.

  private void addClass(Element element, String className) {
    element.addAttribute(createClassAttribute(className));
  }

  private Element createElement(String type) {
    return new Element(type);
  }

  private Element createElement(String type, String className) {
    Element element = createElement(type);
    addClass(element, className);
    return element;
  }

  private Element appendElement(Element parent, String type) {
    Element element = createElement(type);
    parent.appendChild(element);
    return element;
  }

  private Element createJumbotron(Element parent, String description) {
    Element jumbotron = createDiv("jumbotron", createH1(description));
    parent.insertChild(jumbotron, 0);
    return jumbotron;
  }

  private Element appendDiv(Element parent, String className, Element... children) {
    Element div = createDiv(className, children);
    parent.appendChild(div);
    return div;
  }

  private Element createDiv(String className, Element... children) {
    final Element div = createElement("div", className);

    addClass(div, className);

    for (Element child : children) {
      div.appendChild(child);
    }

    return div;
  }

  private Element createH1(String description) {
    Element h1 = createElement("h1");
    h1.appendChild(description);
    return h1;
  }

  private Attribute createClassAttribute(String className) {
    return createAttribute("class", className);
  }

  private Attribute createAttribute(String localName, String value) {
    return new Attribute(localName, value);
  }

  private void addAttribute(Element element, String localName, String value) {
    element.addAttribute(createAttribute(localName, value));
  }

  private Element appendUl(Element parent, String className) {
    Element ul = createElement("ul", className);
    parent.appendChild(ul);
    return ul;
  }
}
