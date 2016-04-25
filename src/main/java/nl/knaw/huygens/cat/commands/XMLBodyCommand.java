package nl.knaw.huygens.cat.commands;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.listener.AssertResultRenderer;
import nl.knaw.huygens.cat.HuygensCommand;

@HuygensCommand(name = "xmlBody", htmlTag = "pre")
public class XMLBodyCommand extends AbstractHuygensCommand {
  public XMLBodyCommand() {
    addListener(new AssertResultRenderer());
  }

  @Override
  public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = commandCall.getElement();
    element.addStyleClass("xml");

    String body = pretty(element.getText());
    element.moveChildrenTo(new Element("tmp"));
    element.appendText(body);

    getFixture(evaluator).body(body);
  }

  String pretty(String xml) {
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      StreamResult result = new StreamResult(new StringWriter());
      Source source = new StreamSource(new StringReader(xml));
      transformer.transform(source, result);
      return result.getWriter().toString().replace("\r\n", "\n").replace("\n\r", "\n").replace("\r", "\n");
    } catch (TransformerFactoryConfigurationError | TransformerException e) {
      throw new RuntimeException(e);
    }
  }
}
