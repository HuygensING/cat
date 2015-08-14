package nl.knaw.huygens.cat.commands;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import nl.knaw.huygens.Log;
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
    final Element replacement = substituteVariables(evaluator, element);
    replacement.addStyleClass("location");

    final String expectedLocation = replacement.getText();
    final String type = typeFrom(element);

    final RestFixture fixture = getFixture(evaluator);
    final String actualLocation = fixture.location().map(l -> extract(l, type)).orElse("(not set)");

    Log.trace("expectedLocation: [{}]", expectedLocation);
    Log.trace("actualLocation  : [{}]", actualLocation);
    if (actualLocation.equals(expectedLocation)) {
      succeed(resultRecorder, replacement);
    } else {
      fail(resultRecorder, replacement, actualLocation, expectedLocation);
    }
  }

  private String extract(String location, String type) {
    switch (type) {
      case "base":
        return baseOf(location);
      case "full":
        return location;
      case "uuid":
        return uuidQuality(location);
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

  private String uuidQuality(String location) {
    final String idStr = tailOf(location);
    return parse(idStr).map(uuid -> "well-formed UUID").orElseGet(malformedDescription(idStr));
  }

  private Optional<UUID> parse(String idStr) {
    try {
      return Optional.of(UUID.fromString(idStr));
    } catch (IllegalArgumentException dulyNoted) {
      return Optional.empty();
    }
  }

  private Supplier<String> malformedDescription(String idStr) {
    return () -> "malformed UUID: " + idStr;
  }

}
