package org.acme;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.DTOs.ExampleData;

import java.util.regex.Pattern;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus REST";
    }


    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public String create(ExampleData data) {
        return "Datos recibidos correctamente";
    }



}
