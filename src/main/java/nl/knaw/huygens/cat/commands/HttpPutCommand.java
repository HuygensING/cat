package nl.knaw.huygens.cat.commands;

import nl.knaw.huygens.cat.HuygensCommand;

@HuygensCommand(name = "put")
public class HttpPutCommand extends HttpMethodCommand {
  public HttpPutCommand() {
    super("PUT");
  }
}
