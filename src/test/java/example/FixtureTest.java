package example;

import static org.junit.Assert.assertTrue;

import nl.knaw.huygens.Log;
import org.junit.Test;

public class FixtureTest {
  @Test
  public void testNothing() {
    System.err.println("Blaat!");
    Log.debug("testing ... nothing!");
    assertTrue(true);
  }
}
