package example;

import nl.knaw.huygens.cat.HuygensCommand;
import nl.knaw.huygens.cat.commands.AbstractHuygensCommand;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

@HuygensCommand(name = "example", htmlTag = "h2")
public class ExampleCommand extends AbstractHuygensCommand {
  @Override
  public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    ((ExampleFixture)getFixture(evaluator)).setupViaExampleCommand(commandCall.getElement().getText());
  }
}
