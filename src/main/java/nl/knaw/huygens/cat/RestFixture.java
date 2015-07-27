package nl.knaw.huygens.cat;

import static java.lang.String.format;
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.squarespace.jersey2.guice.BootstrapUtils;
import nl.knaw.huygens.Log;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

public class RestFixture extends JerseyTest {
  private static final EnumSet<Family> SUCCESSFUL_STATUS_FAMILIES = EnumSet.of(REDIRECTION, SUCCESSFUL);

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

  public static void setupRestFixture(Module module) {
    Log.debug("Setting up Jersey");

    application = new AcceptanceTestApplication();
    Log.trace("+- application=[{}]", application);

    if (!jersey2GuiceBridgeInitialised) {
      initialiseJersey2GuiceBridge(module);
      jersey2GuiceBridgeInitialised = true;
    }
  }

  protected static void register(Class<?> componentClass) {
    Log.trace("Registering: [{}]", componentClass.getName());
    application.register(componentClass);
  }

  private static void initialiseJersey2GuiceBridge(Module module) {
    Log.debug("Bootstrapping Jersey2-Guice bridge");

    final ServiceLocator locator = BootstrapUtils.newServiceLocator();
    Log.trace("+- locator=[{}]", locator);

    final Injector injector = BootstrapUtils.newInjector(locator, Collections.singletonList(module));
    Log.trace("+- injector=[{}]", injector);

    BootstrapUtils.install(locator);
    Log.trace("+- done: locator installed");
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

    target = target.path(path);

    Builder invoker;
    if (headers.containsKey("accept")) {
      invoker = target.request();
    } else {
      invoker = target.request(APPLICATION_JSON_TYPE);
    }

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
    Log.trace("body set to: [{}]", body);
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
    return SUCCESSFUL_STATUS_FAMILIES.contains(statusFamily());
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

  private StatusType statusInfo() {
    return response.getStatusInfo();
  }

  private Family statusFamily() {
    return statusInfo().getFamily();
  }

  private String normalizeHostInfo(String s) {
    return s.replaceAll(hostInfo(), "{host}");
  }

  private String hostInfo() {
    final URI baseURI = getBaseUri();
    return format("%s:%d", baseURI.getHost(), baseURI.getPort());
  }

}
