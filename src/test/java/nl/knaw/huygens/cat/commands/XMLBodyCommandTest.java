package nl.knaw.huygens.cat.commands;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class XMLBodyCommandTest {
  @Test
  public void testPretty() {
    String xml = "<xml><text><body>something</body></text></xml>";
    String expected = "<xml>\n  <text>\n    <body>something</body>\n  </text>\n</xml>\n";
    XMLBodyCommand c = new XMLBodyCommand();
    String pretty = c.pretty(xml);
    assertEquals(expected, pretty);
  }
}
