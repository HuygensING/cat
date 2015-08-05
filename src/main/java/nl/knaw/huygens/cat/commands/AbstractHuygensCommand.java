package nl.knaw.huygens.cat.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.cat.HuygensNamespace;
import nl.knaw.huygens.cat.RestFixture;
import org.concordion.api.AbstractCommand;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.internal.util.Announcer;

public abstract class AbstractHuygensCommand extends AbstractCommand {
  private static Pattern VARIABLE_NAME = Pattern.compile(".*?(\\$[a-z][a-zA-Z0-9_]*)");
  private final Announcer<AssertEqualsListener> listeners = Announcer.to(AssertEqualsListener.class);

  protected void addListener(AssertEqualsListener listener) {
    listeners.addListener(listener);
  }

  protected RestFixture getFixture(Evaluator evaluator) {
    return (RestFixture) evaluator.getVariable(HuygensNamespace.FIXTURE_VARIABLE_NAME);
  }

  protected void succeed(ResultRecorder resultRecorder, Element element) {
    resultRecorder.record(Result.SUCCESS);
    announce().successReported(new AssertSuccessEvent(element));
  }

  protected void fail(ResultRecorder resultRecorder, Element element, String actual, String expected) {
    resultRecorder.record(Result.FAILURE);
    announce().failureReported(new AssertFailureEvent(element, expected, actual));
  }

  protected Element substituteVariables(Evaluator evaluator, Element element) {
    final String before = element.getText();
    final Matcher matcher = VARIABLE_NAME.matcher(before);

    String after = before;
    while (matcher.find()) {
      final String name = matcher.toMatchResult().group(1);
      final Object value = evaluator.getVariable("#" + name.substring(1));
      if (value == null) {
        Log.warn("No such variable or null value stored for [{}]", name);
      }
      else {
        after = after.replace(name, value.toString());
      }
    }

    final Element replacement = new Element(element.getLocalName());
    replacement.appendText(after);

    element.appendSister(replacement);
    element.getParentElement().removeChild(element);

    return replacement;
  }

  private AssertEqualsListener announce() {
    return listeners.announce();
  }
}
