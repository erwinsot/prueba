package org.acme.DTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteDto {
    private String uriLocal;
    private String nameService;
    private String protocol;
    private String host;
    private String port;
    private String path;

}
