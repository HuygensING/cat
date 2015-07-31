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
import nu.xom.Text;
import org.concordion.api.listener.DocumentParsingListener;

class NavigationBuilder implements DocumentParsingListener {
  @Override
  public void beforeParsing(Document document) {
    visitDocument(document);
  }

  private void visitDocument(Document document) {
    Element root = document.getRootElement();
    Element body = root.getFirstChildElement("body");
    Element container = new Element("div");
    container.addAttribute(new Attribute("class", "container"));

    Attribute suiteDesc = body.getAttribute("data-desc");
    if (suiteDesc != null) {
      body.removeAttribute(suiteDesc);
      Element h1 = new Element("h1");
      h1.appendChild(new Text(suiteDesc.getValue()));

      Element jumboTron = new Element("div");
      jumboTron.addAttribute(new Attribute("class", "jumbotron"));
      jumboTron.appendChild(h1);
      container.appendChild(jumboTron);
    }

    Element row = new Element("div");
    row.addAttribute(new Attribute("class", "row"));
    container.appendChild(row);

    Element colMenu = new Element("div");
    colMenu.addAttribute(new Attribute("class", "col-md-3"));
    container.appendChild(colMenu);

    Element colContent = new Element("div");
    colContent.addAttribute(new Attribute("class", "col-md-9"));
    container.appendChild(colContent);

    Element tabContentDiv = new Element("div");
    colContent.appendChild(tabContentDiv);
    tabContentDiv.addAttribute(new Attribute("class", "tab-content"));

    Map<String, Element> identifiedDivs = Maps.newLinkedHashMap();
    Elements testDivs = body.getChildElements("div");
    for (int i = 0; i < testDivs.size(); i++) {
      Element testDiv = testDivs.get(i);
      final Attribute id = testDiv.getAttribute("id");
      final Attribute description = testDiv.getAttribute("data-desc");
      identifiedDivs.put(id.getValue(), testDiv);

      if (i == 0) {
        testDiv.addAttribute(new Attribute("class", "tab-pane fade in active"));
      } else {
        testDiv.addAttribute(new Attribute("class", "tab-pane"));
      }

      testDiv.detach();
      tabContentDiv.appendChild(testDiv);

      Element panel = new Element("div");
      panel.addAttribute(new Attribute("class", "panel panel-default"));

      Element panelHeading = new Element("div");
      panel.appendChild(panelHeading);

      Element bold = new Element("b");  // TODO: move to css
      panelHeading.addAttribute(new Attribute("class", "panel-heading"));
      panelHeading.appendChild(bold);
      bold.appendChild(new Text(description.getValue()));

      Element panelBody = new Element("div");
      panel.appendChild(panelBody);

      panelBody.addAttribute(new Attribute("class", "panel-body"));
      final Nodes nodes = testDiv.removeChildren();
      for (int j = 0; j < nodes.size(); j++) {
        panelBody.appendChild(nodes.get(j));
      }
      testDiv.appendChild(panel);
    }
    Log.trace("Identified divs: {}", identifiedDivs);

    Element ul = new Element("ul");
    colMenu.appendChild(ul);
    ul.addAttribute(new Attribute("class", "nav nav-pills nav-stacked"));
    ul.appendChild(new Text("\n"));
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
        a.addAttribute(new Attribute("href", String.format("#%s", id)));
        a.addAttribute(new Attribute("data-toggle", "tab"));
        String desc = testDiv.getAttributeValue("data-desc");
//      testDiv.removeAttribute(description);
        a.appendChild(new Text(ofNullable(desc).orElseGet(() -> String.format("#%s", id))));
        Log.trace("testDiv: {}", testDiv.toXML());
        Log.trace("query yields: size={}", testDiv.query(".//*[@class]").size());
        if (testDiv.query("//*[@class='failure']").size() > 0) {
          Element spanFailed = new Element("span");
          spanFailed.addAttribute(new Attribute("class", "badge"));
          spanFailed.appendChild(new Text("Failed!"));
          a.appendChild(spanFailed);
        }
        li.appendChild(a);
        ul.appendChild("  ");
        ul.appendChild(li);
        ul.appendChild(new Text("\n"));
      }
    });
    body.insertChild(new Text("\n"), 0);
    body.insertChild(container, 1);
  }
}
