package nl.knaw.huygens.cat;

import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;
import static nl.knaw.huygens.cat.HuygensNamespace.FIXTURE_VARIABLE_NAME;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.cat.bootstrap.BootstrapExtension;
import nl.knaw.huygens.cat.codemirror.CodeMirrorExtension;
import nl.knaw.huygens.cat.commands.AbstractHuygensCommand;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.Text;
import org.concordion.api.Command;
import org.concordion.api.EvaluatorFactory;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.listener.SpecificationProcessingEvent;
import org.concordion.api.listener.SpecificationProcessingListener;
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

    scanAndRegisterAnnotatedCommands(concordionExtender, new Reflections(config.builder));
    installCommandToHtmlTagTranslator(concordionExtender);

    concordionExtender.withDocumentParsingListener(this::visitDocument);

    concordionExtender.withSpecificationProcessingListener(new SpecificationProcessingListener() {
      private final List<String> failedTests = Lists.newArrayList();

      @Override
      public void beforeProcessingSpecification(SpecificationProcessingEvent event) {
      }

      @Override
      public void afterProcessingSpecification(SpecificationProcessingEvent event) {
        final org.concordion.api.Element root = event.getRootElement();
        if (hasFailure(root)) {
          Log.trace("Test has failed test(s): {}", failedTests);
          markFailedTests(root);
        }
      }

      private void markFailedTests(org.concordion.api.Element element) {
        if (element.getLocalName().equals("a") && failedTests.contains(element.getAttributeValue("href"))) {
          Log.trace("Index: {}", element.toXML());
          org.concordion.api.Element failureBadge = new org.concordion.api.Element("span");
          failureBadge.addAttribute("class", "badge");
          failureBadge.appendText("Failed");
          element.appendNonBreakingSpace();
          element.appendChild(failureBadge);
        }
        final org.concordion.api.Element[] childElements = element.getChildElements();
        for (org.concordion.api.Element childElement : childElements) {
          markFailedTests(childElement);
        }
      }

      private boolean hasFailure(org.concordion.api.Element element) {
        final String classValue = element.getAttributeValue("class");
        if (classValue != null && classValue.contains("failure")) {
          Log.trace("FAIL-1: {}", element.toXML());
          return true;
        }

        boolean foundFailure = false;
        final org.concordion.api.Element[] childElements = element.getChildElements();
        for (final org.concordion.api.Element child : childElements) {
          if (hasFailure(child)) {
            foundFailure = true;
            if (element.getLocalName().equals("div") && element.getAttributeValue("id") != null) {
              failedTests.add("#" + element.getAttributeValue("id"));
              final org.concordion.api.Element div = element.getFirstChildElement("div");
              final String divClass = div.getAttributeValue("class");
              div.addAttribute("class", divClass.replace("panel-default", "panel-danger"));
              Log.trace("class: {}", div.getAttributeValue("class"));
            }
          }
        }
        return foundFailure;
      }
    });

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

  private void visitDocument(Document document) {
    Element root = document.getRootElement();
    Element body = root.getFirstChildElement("body");
    Element container = new Element("div");
    container.addAttribute(new Attribute("class", "container"));

    Attribute suiteDesc = body.getAttribute("data-desc");
    if (suiteDesc != null) {
      body.removeAttribute(suiteDesc);
      Element h1 = new Element("h1");
      h1.appendChild(new Text(suiteDesc.getValue()));

      Element jumboTron = new Element("div");
      jumboTron.addAttribute(new Attribute("class", "jumbotron"));
      jumboTron.appendChild(h1);
      container.appendChild(jumboTron);
    }

    Element row = new Element("div");
    row.addAttribute(new Attribute("class", "row"));
    container.appendChild(row);

    Element colMenu = new Element("div");
    colMenu.addAttribute(new Attribute("class", "col-md-3"));
    container.appendChild(colMenu);

    Element colContent = new Element("div");
    colContent.addAttribute(new Attribute("class", "col-md-9"));
    container.appendChild(colContent);

    Element tabContentDiv = new Element("div");
    colContent.appendChild(tabContentDiv);
    tabContentDiv.addAttribute(new Attribute("class", "tab-content"));

    Map<String, Element> identifiedDivs = Maps.newLinkedHashMap();
    Elements testDivs = body.getChildElements("div");
    for (int i = 0; i < testDivs.size(); i++) {
      Element testDiv = testDivs.get(i);
      final Attribute id = testDiv.getAttribute("id");
      final Attribute description = testDiv.getAttribute("data-desc");
      identifiedDivs.put(id.getValue(), testDiv);

      if (i == 0) {
        testDiv.addAttribute(new Attribute("class", "tab-pane fade in active"));
      } else {
        testDiv.addAttribute(new Attribute("class", "tab-pane"));
      }

      testDiv.detach();
      tabContentDiv.appendChild(testDiv);

      Element panel = new Element("div");
      panel.addAttribute(new Attribute("class", "panel panel-default"));

      Element panelHeading = new Element("div");
      panel.appendChild(panelHeading);

      Element bold = new Element("b");  // TODO: move to css
      panelHeading.addAttribute(new Attribute("class", "panel-heading"));
      panelHeading.appendChild(bold);
      bold.appendChild(new Text(description.getValue()));

      Element panelBody = new Element("div");
      panel.appendChild(panelBody);

      panelBody.addAttribute(new Attribute("class", "panel-body"));
      final Nodes nodes = testDiv.removeChildren();
      for (int j = 0; j < nodes.size(); j++) {
        panelBody.appendChild(nodes.get(j));
      }
      testDiv.appendChild(panel);
    }
    Log.trace("Identified divs: {}", identifiedDivs);

    Element ul = new Element("ul");
    colMenu.appendChild(ul);
    ul.addAttribute(new Attribute("class", "nav nav-pills nav-stacked"));
    ul.appendChild(new Text("\n"));
    identifiedDivs.forEach(new BiConsumer<String, Element>() {
      private boolean first = true;

      @Override
      public void accept(String id, Element testDiv) {
        final Element li = new Element("li");
        if (first) {
          first = false;
          li.addAttribute(new Attribute("class", "active"));
        }
        final Element a = new Element("a");
        a.addAttribute(new Attribute("href", String.format("#%s", id)));
        a.addAttribute(new Attribute("data-toggle", "tab"));
        String desc = testDiv.getAttributeValue("data-desc");
//      testDiv.removeAttribute(description);
        a.appendChild(new Text(ofNullable(desc).orElseGet(() -> String.format("#%s", id))));
        Log.trace("testDiv: {}", testDiv.toXML());
        Log.trace("query yields: size={}", testDiv.query(".//*[@class]").size());
        if (testDiv.query("//*[@class='failure']").size() > 0) {
          Element spanFailed = new Element("span");
          spanFailed.addAttribute(new Attribute("class", "badge"));
          spanFailed.appendChild(new Text("Failed!"));
          a.appendChild(spanFailed);
        }
        li.appendChild(a);
        ul.appendChild("  ");
        ul.appendChild(li);
        ul.appendChild(new Text("\n"));
      }
    });
    body.insertChild(new Text("\n"), 0);
    body.insertChild(container, 1);
  }

  private EvaluatorFactory createEvaluatorFactory() {
    return fixture -> {
      // Neither SimpleEvaluator nor its super class has a 'getter' for the fixture passed in
      // through its constructor, so as a poor man's solution we redundantly store it as a variable
      final SimpleEvaluator evaluator = new SimpleEvaluator(fixture);
      evaluator.setVariable(FIXTURE_VARIABLE_NAME, fixture);
      return evaluator;
    };
  }

  private void scanAndRegisterAnnotatedCommands(ConcordionExtender concordionExtender, Reflections scanner) {
    scanner.getTypesAnnotatedWith(HuygensCommand.class).forEach(type -> {
      if (Command.class.isAssignableFrom(type)) {
        Log.debug("Found: {}", type.getCanonicalName());

        @SuppressWarnings("unchecked") final Class<? extends Command> commandClass = (Class<? extends Command>) type;

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
    final Map<String, String> readOnlyView = unmodifiableMap(commandNameToHtmlTagMapping);
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
