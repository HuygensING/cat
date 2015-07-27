package example;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.cat.RestExtension;
import nl.knaw.huygens.cat.RestFixture;
import org.concordion.api.extension.Extension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class ExampleFixture extends RestFixture {
  // Configuration of the RestExtension for this particular project:
  @Extension
  @SuppressWarnings("unused")
  public RestExtension extensionFoundViaReflection //
      = new RestExtension() //
//      .addPackages("nl.knaw.huygens") // "nl.knaw.huygens" is included; add more to scan for project specific commands
      .useCodeMirror()               // Use CodeMirror to show side-by-side diffs when JSON results mismatch
      .includeBootstrap();           // Bootstrap{.css,js} can be included to spice up the output


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
