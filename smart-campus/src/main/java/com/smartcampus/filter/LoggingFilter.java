package com.smartcampus.filter;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = Logger.getLogger("API");

    @Override
    public void filter(ContainerRequestContext requestContext) {
        logger.info("Request: " + requestContext.getMethod() + " " +
                requestContext.getUriInfo().getPath());
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        logger.info("Response Status: " + responseContext.getStatus());
    }
}