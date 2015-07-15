package nl.knaw.huygens.cat.commands;

import nl.knaw.huygens.cat.HuygensCommand;

@HuygensCommand(name = "delete")
public class HttpDeleteCommand extends HttpMethodCommand {
  public HttpDeleteCommand() {
    super("DELETE");
  }
}
