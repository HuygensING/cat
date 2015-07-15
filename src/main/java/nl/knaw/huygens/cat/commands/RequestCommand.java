package nl.knaw.huygens.cat.commands;

import nl.knaw.huygens.cat.HuygensCommand;
import nl.knaw.huygens.cat.RestFixture;
import org.concordion.api.CommandCall;
import org.concordion.api.CommandCallList;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

@HuygensCommand(name = "request", htmlTag = "div")
public class RequestCommand extends AbstractHuygensCommand {
  @Override
  public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final RestFixture fixture = getFixture(evaluator);
    final CommandCallList childCommands = commandCall.getChildren();

    fixture.clear();
    childCommands.setUp(evaluator, resultRecorder);
    childCommands.execute(evaluator, resultRecorder);
    fixture.execute();
    childCommands.verify(evaluator, resultRecorder);
  }
}
