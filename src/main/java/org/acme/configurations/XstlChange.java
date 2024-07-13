package org.acme.configurations;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.java.Log;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * This class is responsible for managing XSLT transformations.
 * It implements the Processor interface from Apache Camel, which allows it to process Exchange objects.
 * This class is annotated with @ApplicationScoped, meaning a single instance will be created for the entire application.
 * It is also annotated with @Log, which is a Lombok annotation that provides a logger.
 */
@ApplicationScoped
@Log
public class XstlChange implements Processor {

    // A flag to determine whether to apply XSLT transformation or not
    private boolean activarTransformacion = false;

    /**
     * This method is called to process an Exchange.
     * It sets a property on the Exchange named "aplicarXSLT" with the value of the activarTransformacion flag.
     * It also logs the value of the activarTransformacion flag.
     *
     * @param exchange the Exchange to process
     * @throws Exception if an error occurs during processing
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        exchange.setProperty("aplicarXSLT", activarTransformacion);
        log.info("Activar transformación: " + activarTransformacion);
    }

    /**
     * This method is used to set the value of the activarTransformacion flag.
     * It also logs the new value of the flag.
     *
     * @param nuevoValor the new value for the activarTransformacion flag
     */
    public void setActivarTransformacion(boolean nuevoValor) {
        this.activarTransformacion = nuevoValor;
        log.info("la tranformacion"+activarTransformacion);
    }
}