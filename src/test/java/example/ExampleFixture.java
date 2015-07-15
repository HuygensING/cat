package example;

import nl.knaw.huygens.cat.RestFixture;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class ExampleFixture extends RestFixture {
  @BeforeClass
  public static void setupExample() {
    register(ExampleEndpoint.class);
  }
}
