package org.acme.routes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.Produces;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.camel.model.rest.RestBindingMode;

import java.util.Optional;


@Data
@ApplicationScoped
@AllArgsConstructor
@NoArgsConstructor
public class RouteTestsCamel extends RouteBuilder {

    private String pathLocal;
    private String serviceClass;
    private String protocol;
    private String host;
    private String port;
    private String path;


    @Override
    public void configure() {

        if (pathLocal != null && serviceClass != null) {
            String uri = String.format("cxf://%s://%s:%s%s?serviceClass=org.acme.wsdl.%s", protocol, host, port, path, serviceClass);
            from("cxf:///" + pathLocal + "?serviceClass=org.acme.wsdl." + serviceClass)
                    .to(uri);
        } else {

        }
    }


}
