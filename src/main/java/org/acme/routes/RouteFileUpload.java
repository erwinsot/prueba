package org.acme.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

/**
 * This class is responsible for defining the routes for file upload.
 * It extends RouteBuilder from Apache Camel, which allows it to define routes for message processing.
 * It defines two routes: one for processing WSDL files and another for processing XSLT files.
 * The files are saved in the respective directories under src/main/resources.
 */
public class RouteFileUpload extends RouteBuilder {
    /**
     * This method is called to configure the routes.
     * It defines two routes: one for processing WSDL files and another for processing XSLT files.
     * The files are saved in the respective directories under src/main/resources.
     *
     * @throws Exception if an error occurs during configuration
     */
    @Override
    public void configure() throws Exception {
        // Define a route for processing WSDL files
        from("direct:processWsdl")
            // Set the file name header to the name of the file without the extension, followed by .wsdl
            .setHeader(Exchange.FILE_NAME, simple("${file:name.noext}.wsdl"))
            // Save the file in the wsdl directory under src/main/resources
            .to("file:src/main/resources/wsdl/")
            // Log a message indicating that the WSDL file has been saved
            .log("Archivo WSDL guardado en resources");

        // Define a route for processing XSLT files
        from("direct:processXslt")
            // Set the file name header to the name of the file without the extension, followed by .xslt
            .setHeader(Exchange.FILE_NAME, simple("${file:name.noext}.xslt"))
            // Save the file in the xslt directory under src/main/resources
            .to("file:src/main/resources/xslt/")
            // Log a message indicating that the XSLT file has been saved
            .log("Archivo XSTL guardado en resources");
    }
}