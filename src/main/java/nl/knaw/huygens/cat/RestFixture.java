package nl.knaw.huygens.cat;

import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.Family.REDIRECTION;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.squarespace.jersey2.guice.BootstrapUtils;
import jersey.repackaged.com.google.common.collect.Lists;
import nl.knaw.huygens.Log;

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

  private Multimap<String, Object> queryParams = ArrayListMultimap.create();

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
    queryParams.clear();
    Log.trace("+- done (request details cleared)");
  }

  public RestFixture method(String method) {
    Log.trace("method set to: [{}]", method);
    this.method = method;
    return this;
  }

  public RestFixture url(String url) {
    Log.trace("url set to: [{}]", url);
    URI uri = URI.create(url);
    this.url = uri.getPath();
    String queryString = uri.getQuery();
    if (queryString != null) {
      Splitter.on('&').trimResults().split(queryString).forEach(qkv -> {
        String[] kv = qkv.split("=");
        queryParams.put(kv[0], kv[1]);
      });
    }
    return this;
  }

  public RestFixture execute() {
    Log.trace("executing");
    request(method, url, queryParams.asMap());
    return this;
  }

  public void request(String method, String path, Map<String, Collection<Object>> queryParams) {
    Log.trace("request: method=[{}], path=[{}], queryParams=[{}]", method, path, queryParams);

    target = target.path(path);
    queryParams.forEach((k, vc) -> {
      Object[] array = Lists.newArrayList(vc).toArray();
      target = target.queryParam(k, array);
    });

    Builder invoker = target.request();

    for (Map.Entry<String, String> entry : headers.entrySet()) {
      Log.trace("header: {}: {}", entry.getKey(), entry.getValue());
      invoker = invoker.header(entry.getKey(), entry.getValue());
    }

    invoker = invoker.accept(tryGetHeader(ACCEPT).map(MediaType::valueOf).orElse(APPLICATION_JSON_TYPE));

    if (optionalBody.isPresent()) {
      final MediaType mediaType = tryGetHeader(CONTENT_TYPE).map(MediaType::valueOf).orElse(APPLICATION_JSON_TYPE);
      // final MediaType mediaType =
      // optionalContentType.orElse(APPLICATION_JSON_TYPE);
      Log.trace("optionalBody present, mediaType=[{}]", mediaType);
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
    final MediaType value = MediaType.valueOf(type);
    Log.trace("contentType: {} -> {}", type, value);
    optionalContentType = Optional.of(value);
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

  private Optional<String> tryGetHeader(final String header) {
    Log.trace("tryGetHeader, header=[{}]", header);
    return headers.keySet().stream().filter(header::equalsIgnoreCase).findFirst().map(headers::get);
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
