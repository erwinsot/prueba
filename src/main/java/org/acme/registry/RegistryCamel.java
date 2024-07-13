package org.acme.registry;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.java.Log;
import org.apache.camel.builder.RouteBuilder;

/**
 * This class is responsible for managing the registry of XSLT files.
 * It extends RouteBuilder from Apache Camel, which allows it to define routes for message processing.
 * This class is annotated with @ApplicationScoped, meaning a single instance will be created for the entire application.
 * It is also annotated with @Log, which is a Lombok annotation that provides a logger.
 */
@ApplicationScoped
@Log
public class RegistryCamel extends RouteBuilder {
  // The directory where the XSLT files are located
  private static final String XSLT_DIRECTORY = "src/main/resources/xslt/";

  /**
   * This method is called to configure the routes.
   * It defines a route that starts with a timer that triggers every 50000 milliseconds.
   * The route then processes an Exchange by scanning the XSLT directory for .xslt files,
   * logging the names of the found files, and adding them to the registry.
   *
   * @throws Exception if an error occurs during configuration
   */
  public void configure() throws Exception {

    from("timer:foo?period=50000")
        .process(exchange -> {
          // Create a File object for the XSLT directory
          File directory = new File(XSLT_DIRECTORY);

          // List the .xslt files in the directory
          File[] xsltFiles = directory.listFiles((dir, name) -> name.endsWith(".xslt"));

          // If there are any .xslt files
          if (xsltFiles != null && xsltFiles.length > 0) {
            // Create a set to store the file names
            Set<String> xsltFileNames = new HashSet<>();

            // Loop through the files and add their names to the set
            for (File file : xsltFiles) {
              xsltFileNames.add(file.getName());
              log.info("Found XSLT file: " + file.getName());
            }

            // Add the set of file names to the registry
            exchange.getContext().getRegistry().bind("xsltFileNames", xsltFileNames);
            log.info("XSLT file names added to the registry.");

          } else {
            // If there are no .xslt files, log a message
            log.info("No XSLT files found in the directory.");
          }
        });
  }
}