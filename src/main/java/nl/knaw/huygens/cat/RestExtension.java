package nl.knaw.huygens.cat;

import java.util.Collections;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.cat.bootstrap.BootstrapExtension;
import nl.knaw.huygens.cat.codemirror.CodeMirrorExtension;
import nl.knaw.huygens.cat.commands.AbstractHuygensCommand;
import org.concordion.api.Command;
import org.concordion.api.EvaluatorFactory;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.internal.ConcordionBuilder;
import org.concordion.internal.SimpleEvaluator;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

public class RestExtension extends AbstractExtension {
  private final Map<String, String> commandNameToHtmlTagMapping = Maps.newHashMap();
  private final Config config = new Config();

  public RestExtension includeBootstrap() {
    config.includeBootstrap = true;
    return this;
  }

  public RestExtension enableCodeMirror() {
    config.enableCodeMirror = true;
    return this;
  }

  public RestExtension addPackages(String... packages) {
    config.addPackages(packages);
    return this;
  }

  @Override
  public void addTo(ConcordionExtender concordionExtender) {
    if (config.enableCodeMirror) {
      new CodeMirrorExtension().addTo(concordionExtender);
    }

    if (config.includeBootstrap) {
      new BootstrapExtension().addTo(concordionExtender);
    }

    addAnnotatedCommands(concordionExtender, new Reflections(config.builder));
    installCommandToHtmlTagTranslator(concordionExtender);

    /* HACK to make the fixture (the instance of the JerseyTest) available in our Concordion Commands.
     *
     * Using the force / reading the source, we know that the incoming ConcordionExtender at this point is
     * actually a ConcordionBuilder. We can trick this builder into accepting a new EvaluatorFactory, and as
     * the fixture instance is passed through createEvaluator, we can intercept it and store it in an Evaluator
     * of our own where we can make the fixture available at a later time.
     */
    final ConcordionBuilder concordionBuilder = (ConcordionBuilder) concordionExtender;
    concordionBuilder.withEvaluatorFactory(createEvaluatorFactory());
  }

  private EvaluatorFactory createEvaluatorFactory() {
    return fixture -> {
      // Neither SimpleEvaluator nor its super class has a 'getter' for the fixture passed in
      // through its constructor, so as a poor man's solution we redundantly store it as a variable
      final SimpleEvaluator evaluator = new SimpleEvaluator(fixture);
      evaluator.setVariable(HuygensNamespace.FIXTURE_VARIABLE_NAME, fixture);
      return evaluator;
    };
  }

  private void addAnnotatedCommands(ConcordionExtender concordionExtender, Reflections scanner) {
    scanner.getTypesAnnotatedWith(HuygensCommand.class).forEach(type -> {
      if (Command.class.isAssignableFrom(type)) {
        Log.debug("Found: {}", type.getCanonicalName());

        @SuppressWarnings("unchecked")
        final Class<? extends Command> commandClass = (Class<? extends Command>) type;

        final HuygensCommand annotation = type.getAnnotation(HuygensCommand.class);
        concordionExtender.withCommand(HuygensNamespace.asString(), annotation.name(), instantiate(commandClass));
        Log.trace("+- will handle command <{}>", annotation.name());

        commandNameToHtmlTagMapping.put(annotation.name(), annotation.htmlTag());
        Log.trace("+- configures <{}> to be translated to HTML tag <{}>", annotation.name(), annotation.htmlTag());
      } else {
        Log.warn("Ignoring @{} class {} as it does not implement {}", //
            HuygensCommand.class.getSimpleName(), type.getName(), Command.class.getCanonicalName());
      }
    });
  }

  private Command instantiate(Class<? extends Command> cmd) {
    try {
      return cmd.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      Log.warn("Cannot instantiate command [{}]: {}", cmd.getName(), e);
      throw new RuntimeException(e);
    }
  }

  private void installCommandToHtmlTagTranslator(ConcordionExtender concordionExtender) {
    final Map<String, String> readOnlyView = Collections.unmodifiableMap(commandNameToHtmlTagMapping);
    final TagTranslator translator = TagTranslator.forTags(readOnlyView);
    concordionExtender.withDocumentParsingListener(translator);
  }

  static class Config {
    private static String COMMANDS_PACKAGE = AbstractHuygensCommand.class.getPackage().getName();
    private static Predicate<String> IS_JAVA_CLASS_FILE = s -> s.endsWith(".class");
    private static Predicate<String> IS_COMMAND = s -> s.startsWith(HuygensCommand.class.getCanonicalName());

    private final ConfigurationBuilder builder = new ConfigurationBuilder() //
        .forPackages(COMMANDS_PACKAGE) //
        .filterInputsBy(IS_JAVA_CLASS_FILE) //
        .setScanners( //
            new SubTypesScanner(), //
            new TypeAnnotationsScanner().filterResultsBy(IS_COMMAND));

    private boolean includeBootstrap;
    private boolean enableCodeMirror;

    void addPackages(String... packages) {
      builder.forPackages(packages);
    }
  }
}
