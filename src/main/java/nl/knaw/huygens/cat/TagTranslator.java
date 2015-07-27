package nl.knaw.huygens.cat;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.concordion.api.listener.DocumentParsingListener;

import java.util.Map;

import static nl.knaw.huygens.cat.HuygensNamespace.createAttribute;

class TagTranslator implements DocumentParsingListener {
  private final Map<String, String> tags;

  private TagTranslator(Map<String, String> tags) {
    this.tags = tags;
  }

  public static TagTranslator forTags(Map<String, String> tags) {
    return new TagTranslator(tags);
  }

  @Override
  public void beforeParsing(Document document) {
    traverse(document.getRootElement());
  }

  private void traverse(Element element) {
    final Elements children = element.getChildElements();

    for (int i = 0; i < children.size(); i++) {
      traverse(children.get(i));
    }

    translate(element);
  }

  private void translate(Element element) {
    if (HuygensNamespace.contains(element)) {
      copyLocalNameToAttribute(element);
      replaceLocalNameByHtmlTag(element);
    }
  }

  private void copyLocalNameToAttribute(Element element) {
    element.addAttribute(createAttribute(element.getLocalName()));
  }

  private void replaceLocalNameByHtmlTag(Element element) {
    element.setNamespacePrefix("");
    element.setNamespaceURI(null);
    element.setLocalName(translate(element.getLocalName()));
  }

  private String translate(String localName) {
    return tags.getOrDefault(localName, localName);
  }
}
