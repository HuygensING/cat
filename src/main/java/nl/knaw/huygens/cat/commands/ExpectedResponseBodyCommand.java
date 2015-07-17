package nl.knaw.huygens.cat.commands;

import java.util.Optional;

import nl.knaw.huygens.cat.HuygensCommand;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.listener.AssertResultRenderer;

@HuygensCommand(name = "responseBody")
public class ExpectedResponseBodyCommand extends AbstractHuygensCommand {

  public ExpectedResponseBodyCommand() {
    addListener(new AssertResultRenderer());
  }

  @Override
  public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = commandCall.getElement();

    final String expectedBody = element.getText();
    final Optional<String> actualBody = getFixture(evaluator).response();

    if (expectedBody.isEmpty()) {
      element.addStyleClass("empty");
      if (actualBody.isPresent()) {
        fail(resultRecorder, element, actualBody.get(), "(not set)");
      } else {
        succeed(resultRecorder, element);
      }
    } else {
      if (actualBody.isPresent()) {
        if (actualBody.get().equals(expectedBody)) {
          succeed(resultRecorder, element);
        }
        else {
          fail(resultRecorder, element, actualBody.get(), expectedBody);
        }
      } else {
        fail(resultRecorder, element, "(not set)", expectedBody);
      }
    }
  }
}
