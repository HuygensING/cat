package nl.knaw.huygens.cat.commands;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class XmlUtilTest {
  @Test
  public void testPretty() {
    String xml = "<xml><text><body>something</body></text></xml>";
    String expected = "<xml>\n  <text>\n    <body>something</body>\n  </text>\n</xml>\n";
    String pretty = XmlUtil.pretty(xml);
    assertEquals(expected, pretty);
  }
}
