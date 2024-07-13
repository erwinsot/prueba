package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.DTOs.Net;

import java.net.URL;

/**
 * This class is responsible for parsing URLs.
 * It is annotated with @ApplicationScoped, meaning a single instance will be created for the entire application.
 */
@ApplicationScoped
public class UrlParser {

    /**
     * This method is used to parse a URL string and extract its components.
     * It creates a URL object from the string and extracts the protocol, host, port, and path.
     * These components are then set in a Net object, which is returned.
     *
     * @param urlString the URL string to parse
     * @return a Net object containing the components of the URL
     */
    public Net parseUrl(String urlString) {

        Net net = new Net();

        try {
            URL url = new URL(urlString);

            // Protocol (http)
            String protocol = url.getProtocol();
            net.setProtocol(protocol);
            System.out.println("Protocolo: " + protocol);

            // Host (localhost)
            String host = url.getHost();
            net.setHost(host);
            System.out.println("Host: " + host);

            // Port (9080)
            int port = url.getPort();
            net.setPort(String.valueOf(port));
            System.out.println("Puerto: " + port);

            // Path (/BBVA_PROCWeb/sca/EntradaProcesoExport1)
            String path = url.getPath();
            net.setPath(path);
            System.out.println("Ruta: " + path);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return net;
    }
}