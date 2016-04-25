package nl.knaw.huygens.cat.commands;

import java.util.Optional;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import nl.knaw.huygens.cat.HuygensCommand;
import nl.knaw.huygens.cat.RestResultRenderer;

@HuygensCommand(name = "xmlResponse", htmlTag = "pre")
public class ExpectedXmlResponseCommand extends AbstractHuygensCommand {

  public ExpectedXmlResponseCommand() {
    addListener(new RestResultRenderer());
  }

  @Override
  public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = substituteVariables(evaluator, commandCall.getElement());
    element.addStyleClass("xml");

    final String expectedXml = element.getText();
    final String expectedPrettyXml = XmlUtil.pretty(expectedXml);
    element.moveChildrenTo(new Element("tmp"));
    element.appendText(expectedPrettyXml);

    final Optional<String> actual = getFixture(evaluator).response();
    if (actual.isPresent()) {
      final String actualXml = actual.get();
      final String actualPrettyXml = XmlUtil.pretty(actualXml);
      if (actualPrettyXml.equals(expectedPrettyXml)) {
        succeed(resultRecorder, element);
      } else {
        fail(resultRecorder, element, actualPrettyXml, expectedPrettyXml);
      }
    } else {
      fail(resultRecorder, element, "(not set)", expectedPrettyXml);
    }
  }

}
