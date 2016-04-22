package nl.knaw.huygens.cat.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;

public class ExpectedJsonResponseCommandTest {
  ExpectedJsonResponseCommand ejrc = new ExpectedJsonResponseCommand();

  @Test
  public void testDurationValidationSucceedsForValidDurationString() {
    JsonNode node = mockedJsonNode("PT5.208S");
    assertTrue(ejrc.isValidDuration(node));
  }

  @Test
  public void testDurationValidationFailsForInvalidDurationString() {
    JsonNode node = mockedJsonNode("Ni!");
    assertFalse(ejrc.isValidDuration(node));
  }

  private JsonNode mockedJsonNode(String value) {
    JsonNode node = mock(JsonNode.class);
    when(node.asText()).thenReturn(value);
    return node;
  }

}
