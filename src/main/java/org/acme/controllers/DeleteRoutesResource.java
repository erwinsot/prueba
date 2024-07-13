package org.acme.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;
import org.acme.DTOs.DeleteRouteDTO;

@ApplicationScoped
@Log
public class DeleteRoutesResource {
    private  String setLocalHost = System.getenv().getOrDefault("LOCAL_HOST", "localhost");
    private  String setLocalPort = System.getenv().getOrDefault("LOCAL_PORT", "12504");
    String EXTERNAL_SERVICE_URL = "http://"+setLocalHost+":"+setLocalPort+"/createLocal/deleteRoute";
    public String deleteExternalService(DeleteRouteDTO data) {
        Client client = ClientBuilder.newClient();
        log.info("EXTERNAL_SERVICE_URL: " + EXTERNAL_SERVICE_URL);
        log.info("Data: " + data);
        try {
            Response response = client.target(EXTERNAL_SERVICE_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(data, MediaType.APPLICATION_JSON));

            if (response.getStatus() == 200) {
                return response.readEntity(String.class);
            } else {
                return "Error: " + response.getStatus() + " - " + response.readEntity(String.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception: " + e.getMessage();
        } finally {
            client.close();
        }
    }
}
