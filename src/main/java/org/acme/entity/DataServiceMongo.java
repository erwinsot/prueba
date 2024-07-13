package org.acme.entity;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApplicationScoped
@NoArgsConstructor
public class DataServiceMongo {
    private String nameService;
    private String protocol;
    private String host;
    private String port;
    private String path;
    private String nameLocalEndpoint;
    private String portLocalEndpoint;
    private String uriLocalEndpoint;
}
