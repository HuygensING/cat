package nl.knaw.huygens.cat.commands;

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

  private AssertEqualsListener announce() {
    return listeners.announce();
  }
}
