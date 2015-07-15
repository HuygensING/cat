package nl.knaw.huygens.cat;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.Family.REDIRECTION;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.squarespace.jersey2.guice.BootstrapUtils;
import nl.knaw.huygens.Log;
import org.concordion.api.extension.Extensions;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;

@Extensions(RestExtension.class)
public class RestFixture extends JerseyTest {

  // TODO: make this configurable
  private static final URI BASE_URI = UriBuilder.fromUri("https://localhost/").port(4242).build();

  private static boolean jersey2GuiceBridgeInitialised;

  private static ResourceConfig application;

  private final Map<String, String> headers = new HashMap<>();

  private WebTarget target;

  private Optional<MediaType> optionalContentType;

  private Optional<String> optionalBody;

  private Response response;

  private Optional<String> entity;

  private String method;

  private String url;

  @BeforeClass
  public static void setup() {
    Log.debug("Setting up Jersey");

    application = new AcceptanceTestApplication();
    Log.trace("+- application=[{}]", application);

    if (!jersey2GuiceBridgeInitialised) {
      initialiseJersey2GuiceBridge();
      jersey2GuiceBridgeInitialised = true;
    }
  }

  protected static void register(Class<?> componentClass) {
    application.register(componentClass);
  }

  private static void initialiseJersey2GuiceBridge() {
    Log.debug("Bootstrapping Jersey2-Guice bridge");

    final ServiceLocator locator = BootstrapUtils.newServiceLocator();
    Log.trace("+- locator=[{}]", locator);

    final Injector injector = BootstrapUtils.newInjector(locator, singletonList(baseModule()));
    Log.trace("+- injector=[{}]", injector);

    BootstrapUtils.install(locator);
    Log.trace("+- done: locator installed");
  }

  private static Module baseModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        Log.trace("setting up Guice bindings");
//        bind(AlexandriaService.class).toInstance(service);
//        bind(AlexandriaConfiguration.class).toInstance(CONFIG);
//        bind(AnnotationEntityBuilder.class).in(Scopes.SINGLETON);
//        bind(EndpointPathResolver.class).in(Scopes.SINGLETON);
//        bind(ResourceEntityBuilder.class).in(Scopes.SINGLETON);
      }
    };
  }

  public void clear() {
    Log.debug("Clearing {}", getClass().getSimpleName());

    target = client().target(getBaseUri());
    Log.trace("+- refreshed WebTarget: [{}]", target);

    optionalContentType = Optional.empty();
    optionalBody = Optional.empty();
    response = null;
    entity = Optional.empty();
    headers.clear();
    Log.trace("+- done (request details cleared)");
  }

  public RestFixture method(String method) {
    Log.trace("method set to: [{}]", method);
    this.method = method;
    return this;
  }

  public RestFixture url(String url) {
    Log.trace("url set to: [{}]", url);
    this.url = url;
    return this;
  }

  public RestFixture execute() {
    Log.trace("executing");
    request(method, url);
    return this;
  }

  public void request(String method, String path) {
    Log.trace("request: method=[{}], path=[{}]", method, path);

    // TODO: don't always ask for JSON, esp. when accept-header is set
    Builder invoker = target.path(path).request(APPLICATION_JSON_TYPE);

    for (Map.Entry<String, String> entry : headers.entrySet()) {
      Log.trace("header: {}: {}", entry.getKey(), entry.getValue());
      invoker = invoker.header(entry.getKey(), entry.getValue());
    }

    if (optionalBody.isPresent()) {
      final MediaType mediaType = optionalContentType.orElse(APPLICATION_JSON_TYPE);
      response = invoker.method(method, Entity.entity(optionalBody.get(), mediaType), Response.class);
    } else {
      response = invoker.method(method, Response.class);
    }
    Log.trace("response: [{}]", response);

    if (response == null) {
      throw new IllegalStateException("Invoker yielded null Response");
    }

    if (response.hasEntity()) {
      final String rawEntity = response.readEntity(String.class);
      entity = Optional.of(normalizeHostInfo(rawEntity));
      Log.trace("read response entity: [{}]", entity);
    }
  }

  public void body(String body) {
    optionalBody = Optional.of(body);
  }

  public void emptyBody() {
    body("");
  }

  public void setHeader(String name, String value) {
    headers.put(name, value);
  }

  public void contentType(String type) {
    optionalContentType = Optional.of(MediaType.valueOf(type));
  }

  public Optional<String> response() {
    return entity;
  }

  public Optional<String> location() {
    return Optional.ofNullable(response.getLocation()).map(URI::toString).map(this::normalizeHostInfo);
  }

  public Optional<String> header(String header) {
    return Optional.ofNullable(response.getHeaderString(header));
  }

  public String status() {
    final StatusType statusInfo = statusInfo();
    return format("%s %s", statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
  }

  public boolean wasSuccessful() {
    final StatusType statusInfo = statusInfo();
    final Family family = statusInfo.getFamily();
    return family == SUCCESSFUL || family == REDIRECTION;
  }

  private StatusType statusInfo() {
    return response.getStatusInfo();
  }

  @Override
  protected Application configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);
    return application;
  }

  @Override
  protected URI getBaseUri() {
    return BASE_URI;
  }

  private String normalizeHostInfo(String s) {
    return s.replaceAll(hostInfo(), "{host}");
  }

  private String hostInfo() {
    final URI baseURI = getBaseUri();
    return format("%s:%d", baseURI.getHost(), baseURI.getPort());
  }

}
