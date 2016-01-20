package nl.knaw.huygens.cat.commands;

import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.listener.AssertResultRenderer;
import nl.knaw.huygens.cat.HuygensCommand;

@HuygensCommand(name = "requestBody", htmlTag = "pre")
public class RequestBodyCommand extends AbstractHuygensCommand {
  public RequestBodyCommand() {
    addListener(new AssertResultRenderer());
  }

  @Override
  public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = commandCall.getElement();
    String body = element.getText();
    element.moveChildrenTo(new Element("tmp"));
    element.appendText(body);

    getFixture(evaluator).body(body);
  }
}
