package nl.knaw.huygens.cat;

import org.concordion.internal.SimpleEvaluator;

public class FixtureEvaluator extends SimpleEvaluator {
  private final RestFixture fixture;

  public FixtureEvaluator(RestFixture fixture) {
    super(fixture);
    this.fixture = fixture;
  }

  public RestFixture getFixture() {
    return fixture;
  }
}
