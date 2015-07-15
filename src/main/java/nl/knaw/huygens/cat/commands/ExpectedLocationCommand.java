package nl.knaw.huygens.cat.commands;

import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import nl.knaw.huygens.cat.HuygensCommand;
import nl.knaw.huygens.cat.RestFixture;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.listener.AssertResultRenderer;

@HuygensCommand(name = "location")
public class ExpectedLocationCommand extends AbstractHuygensCommand {

  public ExpectedLocationCommand() {
    addListener(new AssertResultRenderer());
  }

  @Override
  public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = commandCall.getElement();
    element.addStyleClass("location");

    final String expectedLocation = element.getText();
    final String type = typeFrom(element);

    final RestFixture fixture = getFixture(evaluator);
    final String actualLocation = fixture.location().map(l -> extract(l, type)).orElse("(not set)");

    if (actualLocation.equals(expectedLocation)) {
      succeed(resultRecorder, element);
    } else {
      fail(resultRecorder, element, actualLocation, expectedLocation);
    }
  }

  private String extract(String location, String type) {
    switch (type) {
      case "base":
        return baseOf(location);
      case "full":
        return location;
      default:
        throw new IllegalArgumentException("Illegal type: " + type);
    }
  }

  private String typeFrom(Element element) {
    String type = element.getAttributeValue("type");
    return Optional.ofNullable(type).orElse("full");
  }

  private String baseOf(String s) {
    return s.substring(0, s.lastIndexOf('/') + 1);
  }

  private String tailOf(String s) {
    return Iterables.getLast(Splitter.on('/').split(s));
  }

  private Supplier<String> malformedDescription(String idStr) {
    return () -> "malformed UUID: " + idStr;
  }

}
