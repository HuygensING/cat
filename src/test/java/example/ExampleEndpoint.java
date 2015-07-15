package example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/example")
public class ExampleEndpoint {
  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response helloWorldText() {
    return Response.ok("Hello world").build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response helloWorldJson() {
    return Response.ok("{\"hello\": \"world\"}").build();
  }
}
