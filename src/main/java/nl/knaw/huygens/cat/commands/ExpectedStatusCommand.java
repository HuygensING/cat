package nl.knaw.huygens.cat.commands;

import nl.knaw.huygens.cat.HuygensCommand;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.listener.AssertResultRenderer;

@HuygensCommand(name = "status")
public class ExpectedStatusCommand extends AbstractHuygensCommand {

  public ExpectedStatusCommand() {
    addListener(new AssertResultRenderer());
  }

  @Override
  public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = commandCall.getElement();

    final String expectedStatus = element.getText();
    final String actualStatus = getFixture(evaluator).status();

    if (expectedStatus.equals(actualStatus)) {
      succeed(resultRecorder, element);
    }
    else {
      fail(resultRecorder, element, actualStatus, expectedStatus);
    }
  }
}
