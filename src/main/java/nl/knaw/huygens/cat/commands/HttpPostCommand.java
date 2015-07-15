package nl.knaw.huygens.cat.commands;

import nl.knaw.huygens.cat.HuygensCommand;

@HuygensCommand(name = "post")
public class HttpPostCommand extends HttpMethodCommand {
  public HttpPostCommand() {
    super("POST");
  }
}
