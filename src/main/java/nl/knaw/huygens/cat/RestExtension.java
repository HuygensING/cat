package nl.knaw.huygens.cat;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.Log;
import org.concordion.api.Command;
import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.internal.ConcordionBuilder;
import org.concordion.internal.SimpleEvaluator;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static nl.knaw.huygens.cat.TagTranslator.forTags;

public class RestExtension implements ConcordionExtension {
  private final Set<Class<? extends Command>> commandClasses = Sets.newHashSet();
  private final Map<String, String> htmlCommandTags = Maps.newHashMap();

  public RestExtension() {
    // TODO: get rid of nl.knaw.huygens, make this an external param somehow
    addAnnotatedCommands(new Reflections("nl.knaw.huygens"));
  }

  @Override
  public void addTo(ConcordionExtender concordionExtender) {
    registerCommands(concordionExtender);
    installCommandToHtmlTagTranslator(concordionExtender);
    registerCodeMirror(concordionExtender);
    registerBootstrap(concordionExtender);

    /* HACK to make the fixture (the instance of the JerseyTest) available in our Concordion Commands.
     *
     * Using the force / reading the source, we know that the incoming ConcordionExtender at this point is
     * actually a ConcordionBuilder. We can trick this builder into accepting a new EvaluatorFactory, and as
     * the fixture instance is passed through createEvaluator, we can intercept it and store it in an Evaluator
     * of our own where we can make the fixture available at a later time.
     */
    final ConcordionBuilder concordionBuilder = (ConcordionBuilder) concordionExtender;
    concordionBuilder.withEvaluatorFactory(fixture -> {
      if (fixture instanceof RestFixture) {
        return new FixtureEvaluator((RestFixture) fixture);
      }
      return new SimpleEvaluator(fixture);
    });
  }

  private void registerCodeMirror(ConcordionExtender extender) {
    linkCSS(extender, "/codemirror/codemirror.css");
    linkCSS(extender, "/codemirror/enable-codemirror.css");
    linkCSS(extender, "/codemirror/merge.css");

    linkJavaScript(extender, "/codemirror/codemirror.js");
    linkJavaScript(extender, "/codemirror/javascript.js");
    linkJavaScript(extender, "/codemirror/diff_match_patch.js");
    linkJavaScript(extender, "/codemirror/merge.js");
    linkJavaScript(extender, "/codemirror/enable-codemirror.js");
  }

  private void registerBootstrap(ConcordionExtender extender) {
    linkCSS(extender, "/bootstrap/bootstrap.css");
    linkCSS(extender, "/bootstrap/enable-bootstrap.css");

    linkJavaScript(extender, "/jquery/jquery.min.js");
    linkJavaScript(extender, "/bootstrap/bootstrap.min.js");
  }

  private void linkCSS(ConcordionExtender extender, String location) {
    extender.withLinkedCSS(location, resource(location));
  }

  private void linkJavaScript(ConcordionExtender extender, String location) {
    extender.withLinkedJavaScript(location, resource(location));
  }

  private Resource resource(String location) {
    return new Resource(location);
  }

  private void addAnnotatedCommands(Reflections scanner) {
    scanForAnnotatedClasses(scanner, HuygensCommand.class).forEach(this::addCommand);
  }

  private Set<Class<?>> scanForAnnotatedClasses(Reflections scanner, Class<? extends Annotation> annotationClass) {
    final Set<Class<?>> annotatedClasses = scanner.getTypesAnnotatedWith(annotationClass);

    if (Log.isDebugEnabled()) {
      final int annotatedClassesCount = annotatedClasses.size();
      final String annotationName = annotationClass.getSimpleName();
      final String classOrClasses = annotatedClassesCount == 1 ? "class" : "classes";
      Log.debug("Found {} @{} annotated {}:", annotatedClassesCount, annotationName, classOrClasses);
    }

    return annotatedClasses;
  }

  @SuppressWarnings("unchecked")
  private void addCommand(Class<?> candidate) {
    if (Command.class.isAssignableFrom(candidate)) {
      Log.trace("+- {}", candidate.getCanonicalName());
      commandClasses.add((Class<? extends Command>) candidate);
    } else {
      Log.warn("Ignoring @{} class {} as it does not implement {}", //
          HuygensCommand.class.getSimpleName(), candidate.getName(), Command.class.getName());
    }
  }

  private void registerCommands(ConcordionExtender extender) {
    commandClasses.stream().forEach(cmdClass -> registerCommand(extender, cmdClass));
  }

  private void registerCommand(ConcordionExtender extender, Class<? extends Command> commandClass) {
    final HuygensCommand annotation = commandClass.getAnnotation(HuygensCommand.class);
    registerCommand(extender, commandClass, annotation.name());
    registerCommandTranslation(annotation);
  }

  private void registerCommand(ConcordionExtender extender, Class<? extends Command> command, String name) {
    Log.trace("Command <{}> will be handled by {}", name, command.getSimpleName());
    extender.withCommand(HuygensNamespace.asString(), name, instantiate(command));
  }

  private void registerCommandTranslation(HuygensCommand annotation) {
    final String name = annotation.name();
    final String tag = annotation.htmlTag();
    Log.trace("Command <{}> will be translated to HTML tag <{}>", name, tag);
    htmlCommandTags.put(name, tag);
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
    concordionExtender.withDocumentParsingListener(forTags(htmlCommandTagsView()));
  }

  private Map<String, String> htmlCommandTagsView() {
    return Collections.unmodifiableMap(htmlCommandTags);
  }

}
