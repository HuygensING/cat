package example;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nl.knaw.huygens.Log;

@Path("/example")
public class ExampleEndpoint {
  private final HelloService helloService;

  @Inject
  public ExampleEndpoint(HelloService helloService) {
    Log.trace("ExampleEndpoint created: helloService=[{}]", helloService);
    this.helloService = helloService;
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response helloWorldText() {
    return Response.ok(helloService.getGreeting()).build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response helloWorldJson() {
    return Response.ok("{\"hello\": \"world\"}").build();
  }

  @GET
  @Path("query")
  @Produces(MediaType.APPLICATION_JSON)
  public Response returnQueryParams(@QueryParam("par1") String par1, @QueryParam("par2") String par2) {
    return Response.ok("{\"par1\": \"" + par1 + "\",\"par2\": \"" + par2 + "\"}").build();
  }

}
