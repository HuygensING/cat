package nl.knaw.huygens.cat.commands;

import nl.knaw.huygens.cat.HuygensCommand;

@HuygensCommand(name = "get")
public class HttpGetCommand extends HttpMethodCommand {
  public HttpGetCommand() {
    super("GET");
  }
}
