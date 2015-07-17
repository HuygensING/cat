package example;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.cat.RestFixture;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class ExampleFixture extends RestFixture {

  // Boiler-plate:
  @BeforeClass
  public static void setupExample() {
    setupRestFixture(guiceModule());  // Guice registration via com.google.inject.Module
    register(ExampleEndpoint.class);  // Jersey registration via inheritance through RestFixture
  }

  private static Module guiceModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        Log.trace("setting up Guice bindings");
//        bind(HelloService.class).in(Scopes.SINGLETON); // Let Guice instantiate it if there is a class, or DIY:
        bind(HelloService.class).toInstance(() -> "Hello world");
      }
    };
  }
}
