package com.smartcampus.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("api", "Smart Campus API");
        map.put("version", "v1");
        map.put("contact", "admin@smartcampus.ac.uk");

        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");

        map.put("resources", links);
        return Response.ok(map).build();
    }
}