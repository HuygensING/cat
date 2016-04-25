package nl.knaw.huygens.cat.commands;

import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.listener.AssertResultRenderer;
import nl.knaw.huygens.cat.HuygensCommand;

@HuygensCommand(name = "xmlBody", htmlTag = "pre")
public class XmlBodyCommand extends AbstractHuygensCommand {
  public XmlBodyCommand() {
    addListener(new AssertResultRenderer());
  }

  @Override
  public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = commandCall.getElement();
    element.addStyleClass("xml");

    String xml = element.getText();
    String prettyXml = XmlUtil.pretty(xml);
    element.moveChildrenTo(new Element("tmp"));
    element.appendText(prettyXml);

    getFixture(evaluator).body(xml);
  }

}
