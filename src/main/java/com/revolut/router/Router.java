package com.revolut.router;

import com.revolut.exception.RevolutException;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;

import java.util.UUID;

import static com.revolut.utils.JsonUtil.json;
import static spark.Spark.*;

/**
 * Basic router with exception handling
 */
public class Router {
    /**
     * Router logger
     */
    private Logger logger;

    public Router(Logger logger) {
        this.logger = logger;
    }

    /**
     * Add a new handler for POST request
     *
     * @param path URL
     * @param route spark route with handler
     */
    public void handlePOST(String path, spark.Route route) {
        // Force all responses to be JSON
        after((request, response) -> response.type("application/json"));

        post(path, (spark.Request request, spark.Response response) -> {
            UUID traceId = UUID.randomUUID();

            try {
                // Add trace id
                ThreadContext.put(com.revolut.logger.Logger.FIELD_TRACE_ID, traceId.toString());
                logger.info("Matched " + path + " route");
                return route.handle(request, response);
            } catch (RevolutException exception) {
                // Revolut exception might have additional displayable data
                logger.error("Uncaught revolut exception", exception);
                response.status(200);
                return new Response(Response.ResponseCode.INTERNAL_ERROR, traceId.toString()).setData(exception.toDisplayable());
            } catch (Exception exception) {
                // All other exceptions are plain 500 internal server error
                logger.error("Uncaught exception", exception);
                response.status(500);
                return new Response(Response.ResponseCode.INTERNAL_ERROR, traceId.toString());
            }
        }, json());

        logger.info("Registered " + path + " route");
    }
}
