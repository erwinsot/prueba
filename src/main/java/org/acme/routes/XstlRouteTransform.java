package org.acme.routes;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.URISyntaxException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.acme.DTOs.ConditionBean;
import org.apache.camel.builder.RouteBuilder;

import java.io.File;

/**
 * This class is responsible for defining the XSLT transformation route.
 * It extends RouteBuilder from Apache Camel, which allows it to define routes for message processing.
 * This class is annotated with @ApplicationScoped, meaning a single instance will be created for the entire application.
 * It uses Lombok annotations for automatic generation of getters, setters, constructors, and toString methods.
 */
@ApplicationScoped
@Log
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XstlRouteTransform extends RouteBuilder {

  private String uriLocal;
  private String nameService;
  private String protocol;
  private String host;
  private String port;
  private String path;

  private static final String WSDL_DIRECTORY = "src/main/resources/wsdl/";
  private static final String XSTL_DIRECTORY = "src/main/resources/xslt/";

  /**
   * This method is called to configure the routes.
   * It first checks for the presence of WSDL files in the WSDL_DIRECTORY.
   * If a WSDL file is found, it sets up a route from a CXF endpoint to either apply an XSLT transformation or not, based on a condition.
   * The transformed or original message is then sent to another CXF endpoint.
   *
   * @throws Exception if an error occurs during configuration
   */
  @Override
  public void configure() throws Exception {
    var wsdlFiles = new File(WSDL_DIRECTORY).listFiles((dir, name) -> name.endsWith(".wsdl"));

    if (wsdlFiles == null || wsdlFiles.length == 0) {
      log.info("Ningún archivo WSDL encontrado");
      return;
    }

    File desiredWsdlFile = null;
    for (File file : wsdlFiles) {
      if (file.getName().equals(nameService+".wsdl")) {
        desiredWsdlFile = file;
        log.info("Se encontró el archivo WSDL deseado: " + nameService);
        break;
      }
    }

    if (desiredWsdlFile == null) {
      log.info("No se encontró el archivo WSDL deseado: " + nameService);
      return;
    }

    String wsdlPath = desiredWsdlFile.getAbsolutePath();
    var cxfEndpoint=String.format("cxf:%s?wsdlURL=file:%s&dataFormat=MESSAGE", uriLocal, wsdlPath);

    if (uriLocal == null || nameService == null) {
      log.info("La ruta no está configurada");
      return;
    }

    var xsltPath = String.format("%s%s.xslt", XSTL_DIRECTORY, nameService);
    var xsltEndpoint = String.format("xslt:file:%s", xsltPath);

    from(cxfEndpoint)
        .routeId(nameService)
        .choice()
        .when().method(ConditionBean.class, "isApplyTransformation")
        .to("direct:"+nameService+"-applyXslt")
        .log("Mensaje transformado: ${body}")
        .otherwise()
        .log("No se aplica transformación XSLT")
        .end()
        .to(String.format("cxf:%s://%s:%s%s?dataFormat=MESSAGE", protocol, host, port, path));

    from("direct:"+nameService+"-applyXslt")
        .routeId(nameService + "-applyXslt")
        .toD(xsltEndpoint+"?failOnNullBody=false")
        .log("Mensaje transformado: ${body}");
  }
}