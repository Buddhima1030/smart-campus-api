package com.smartcampus.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;
import java.util.HashMap;
import java.util.Map;

@Provider
public class ExceptionMappers implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable ex) {

        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse();
        }

        Map<String, Object> body = new HashMap<>();

        if (ex instanceof RoomNotEmptyException) {
            body.put("error", "Room not empty");
            body.put("message", ex.getMessage());
            return Response.status(409).entity(body).build();
        }

        if (ex instanceof LinkedResourceNotFoundException) {
            body.put("error", "Invalid reference");
            body.put("message", ex.getMessage());
            return Response.status(422).entity(body).build();
        }

        if (ex instanceof SensorUnavailableException) {
            body.put("error", "Sensor unavailable");
            body.put("message", ex.getMessage());
            return Response.status(403).entity(body).build();
        }

        body.put("error", "Internal server error");
        return Response.status(500).entity(body).build();
    }
}