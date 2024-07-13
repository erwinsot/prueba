package org.acme.DTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteData {
    private String uri;
    private int portEndpoint;
    private String path;
    private String uriRest;
    private String xstlPath;
    private int portRest;
    private String host;

}
