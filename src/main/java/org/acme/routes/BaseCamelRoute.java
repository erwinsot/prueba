package org.acme.routes;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.configurations.CardXmlProcess;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for defining the base Camel route for the application.
 * It extends RouteBuilder from Apache Camel, which allows it to define routes for message processing.
 * This class is annotated with @ApplicationScoped, meaning a single instance will be created for the entire application.
 * It uses Lombok annotations for automatic generation of getters, setters, constructors, and toString methods.
 */
@ApplicationScoped
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseCamelRoute extends RouteBuilder {
    private static final Logger logger = LoggerFactory.getLogger(BaseCamelRoute.class);

    private String uri;
    private int portEndpoint;
    private String path;
    private String uriRest;
    private String xstlPath;
    private int portRest;
    private String host;

    /**
     * This method is called to configure the routes.
     * It defines a REST configuration and two routes: one for processing JSON requests and another for processing XML requests.
     *
     * @throws Exception if an error occurs during configuration
     */
    @Override
    public void configure() throws Exception {
        logger.info("Entering the route configuration");

        // Configure the REST component with the host and port
        restConfiguration()
            .component("netty-http")
            .host(host)
            .port(portRest);

        // Define a route for processing JSON requests
        rest(path)
            .post(uriRest)
            .consumes("application/json")
            .to("direct:CardXml");

        from("direct:CardXml")
            .log("Received POST request thread: ${body}")
            .log("Processed successfully")
            .process(new CardXmlProcess(xstlPath)) // Process the request with the CardXmlProcess
            .setHeader("operationName", constant("processCardXmlRequest"))
            .setHeader("operationNamespace", constant("http://www.bbvacard.com/servicesoapcard/gen"))
            .setHeader("CamelCxfRsRequestDataFormat", constant("MESSAGE"))
            .setHeader("Content-Type", constant("text/xml"))
            .log("Processing message: ${body}");

        // Define a route for processing XML requests
        rest("xml")
            .post("/card")
            .consumes("application/xml")
            .to("direct:toXml");

        from("direct:toXml")
            .to("xj:identity?transformDirection=JSON2XML") // Convert the request from JSON to XML
            .log("Processed successfully")
            .log("Processed message: ${body}");
    }
}