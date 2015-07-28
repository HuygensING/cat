package nl.knaw.huygens.cat;

import org.concordion.api.Element;
import org.concordion.api.listener.*;

public class RestResultRenderer implements AssertEqualsListener, AssertTrueListener, AssertFalseListener {
  private static final String STYLE_REST_SUCCESS = "rest-success";
  private static final String STYLE_REST_FAILURE = "rest-failure";

  private static final String STYLE_ACTUAL = "actual";
  private static final String STYLE_EXPECTED = "expected";

  private static final String ELEM_DEL = "del";
  private static final String ELEM_INS = "ins";

  @Override
  public void successReported(AssertSuccessEvent event) {
    event.getElement().addStyleClass(STYLE_REST_SUCCESS).appendNonBreakingSpaceIfBlank();
  }

  @Override
  public void failureReported(AssertFailureEvent event) {
    Element element = event.getElement();
    element.addStyleClass(STYLE_REST_FAILURE);

    Element spanExpected = new Element(ELEM_DEL);
    spanExpected.addStyleClass(STYLE_EXPECTED);
    element.moveChildrenTo(spanExpected);
    element.appendChild(spanExpected);
    spanExpected.appendNonBreakingSpaceIfBlank();

    Element spanActual = new Element(ELEM_INS);
    spanActual.addStyleClass(STYLE_ACTUAL);
    spanActual.appendText(String.valueOf(event.getActual()));
    spanActual.appendNonBreakingSpaceIfBlank();

    element.appendText("\n");
    element.appendChild(spanActual);
  }

}
