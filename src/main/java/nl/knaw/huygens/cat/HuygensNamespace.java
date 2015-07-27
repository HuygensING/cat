package nl.knaw.huygens.cat;

import nu.xom.Attribute;
import nu.xom.Element;

public class HuygensNamespace {
  private static final String EXTENSION_NS = "http://huygens.knaw.nl/concordion-acceptance-test";
  private static final String DEFAULT_PREFIX = "h";

  private HuygensNamespace() {
    throw new AssertionError("This class shall not be instantiated");
  }

  public static boolean contains(Element element) {
    return EXTENSION_NS.equals(element.getNamespaceURI());
  }

  public static String asString() {
    return EXTENSION_NS;
  }

  public static Attribute createAttribute(String name) {
    return createAttribute(name, DEFAULT_PREFIX);
  }

  public static Attribute createAttribute(String name, String prefix) {
    final Attribute attr = new Attribute(name, "");
    attr.setNamespace(prefix, EXTENSION_NS);
    return attr;
  }
}
