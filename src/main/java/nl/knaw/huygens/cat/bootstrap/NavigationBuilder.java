package nl.knaw.huygens.cat.bootstrap;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.concordion.api.listener.DocumentParsingListener;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

class NavigationBuilder implements DocumentParsingListener {
  private static final String ATTRIBUTE_ID = "id";
  private static final String ATTRIBUTE_DATA_DESC = "data-desc";
  private Element tabOverview;
  private Element tabContent;
  private Element body;
  private Element container;
  private boolean isFirst = true;

  private static Predicate<Element> hasId() {
    return div -> div.getAttribute(ATTRIBUTE_ID) != null;
  }

  private static Predicate<Element> hasDescription() {
    return div -> div.getAttribute(ATTRIBUTE_DATA_DESC) != null;
  }

  private static Predicate<Element> isTestCase() {
    return hasId().and(hasDescription());
  }

  private static Stream<Element> stream(final Elements elements) {
    final Iterable<Element> iterable = () -> new Iterator<Element>() {
      private int curIndex = 0;

      @Override
      public boolean hasNext() {
        return curIndex < elements.size();
      }

      @Override
      public Element next() {
        return elements.get(curIndex++);
      }
    };

    return StreamSupport.stream(iterable.spliterator(), false);
  }

  @Override
  public void beforeParsing(Document document) {
    installNavigationElements(document);
    promoteSuiteDescriptionToJumbotron();
    AtomicInteger counter = new AtomicInteger();
    streamTestCases().forEach(d -> installNavigationForTest(d, counter.incrementAndGet()));
  }

  private void installNavigationElements(Document document) {
    body = bodyOf(document);
    container = appendDiv(body, "container");

    final Element row = appendDiv(container, "row");
    tabOverview = appendUl(appendDiv(row, "col-md-3"), "nav nav-pills nav-stacked");
    tabContent = appendDiv(appendDiv(row, "col-md-9"), "tab-content");
  }

  private void promoteSuiteDescriptionToJumbotron() {
    Optional.ofNullable(body.getAttribute(ATTRIBUTE_DATA_DESC)) //
        .map(body::removeAttribute) //
        .map(Attribute::getValue) //
        .ifPresent(this::insertJumbotron);
  }

  private Stream<Element> streamTestCases() {
    return streamDivs(body).filter(isTestCase());
  }

  private Stream<Element> streamDivs(Element element) {
    return stream(element.getChildElements("div"));
  }

  private void insertJumbotron(String description) {
    container.insertChild(createDiv("jumbotron", createH1(description)), 0);
  }

  private String numbered(String description, int i) {
    return i + ") " + description;
  }

  private void installNavigationForTest(Element testDiv, int i) {
    addClass(testDiv, isFirst ? "tab-pane fade active in" : "tab-pane fade");
    restructureTest(testDiv, isFirst, i);
    isFirst = false;
  }

  private void restructureTest(Element testDiv, boolean asActiveTab, int i) {
    final Element li = appendElement(tabOverview, "li");

    if (asActiveTab) {
      addClass(li, "active");
    }

    final Element a = appendElement(li, "a");
    addAttribute(a, "href", "#" + testDiv.getAttributeValue(ATTRIBUTE_ID));
    addAttribute(a, "data-toggle", "tab");
    a.appendChild(numbered(testDiv.getAttributeValue(ATTRIBUTE_DATA_DESC),i));

    if (indicatesFailure(testDiv)) {
      final Element failureBadge = appendElement(a, "span");
      addAttribute(failureBadge, "class", "badge");
      failureBadge.appendChild("Failed");
    }

    relocateToContentPane(testDiv);
    restructureIntoPanel(testDiv, i);
  }

  private boolean indicatesFailure(Element element) {
    return element.query("//*[@class='failure']").size() > 0;
  }

  private void relocateToContentPane(Element element) {
    element.detach();
    tabContent.appendChild(element);
  }

  private void restructureIntoPanel(Element element, int i) {
    // First unlink all the children
    final Nodes nodes = element.removeChildren();

    // Create a panel with heading and body inside the element
    final Element panel = appendDiv(element, "panel panel-default");
    final Element panelHeading = appendDiv(panel, "panel-heading");
    final Element panelBody = appendDiv(panel, "panel-body");

    // Transfer the description from the element to the panel heading
    final Attribute description = element.getAttribute(ATTRIBUTE_DATA_DESC);
    appendElement(panelHeading, "strong").appendChild(numbered(description.getValue(),i));
    element.removeAttribute(description);

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
