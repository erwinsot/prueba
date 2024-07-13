package org.acme.servicesCamel;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.extern.java.Log;
import org.acme.DTOs.*;
import org.acme.configurations.ConsulRegistration;
import org.acme.configurations.RouteStopException;
import org.acme.configurations.XstlChange;

import org.acme.controllers.DeleteRoutesResource;
import org.acme.routes.BaseCamelRoute;
import org.acme.routes.XstlRouteTransform;
import org.acme.service.SoapServiceConfiguration;
import org.acme.service.TypeSoapService;
import org.acme.service.UrlParser;
import org.acme.service.WsdlHostInfo;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.bson.Document;
import org.slf4j.Logger;

/**
 * Esta clase es responsable de administrar las rutas dinámicas.
 * Está anotado con @ApplicationScoped, lo que significa que se creará una sola instancia para toda la aplicación.
 * También está anotado con @Log, que es una anotación de Lombok que proporciona un registro.
 */


@ApplicationScoped
@Log
public class DynamicRoutesManager {

  Logger logger= org.slf4j.LoggerFactory.getLogger(DynamicRoutesManager.class);

  /**
   * Inyección de dependencias
   */


  @Inject
  CamelContext camelContext;
  @Inject
  SoapServiceConfiguration soapServiceConfiguration;
  @Inject
  WsdlHostInfo wsdlHostInfo;
  @Inject
  UrlParser urlParser;
  @Inject
  XstlRouteTransform xstlRouteTransform;
  @Inject
  ProducerTemplate producerTemplate;
  @Inject
  XstlChange xstlChange;
  @Inject
  ConditionBean conditionBean;

  @Inject
  TypeSoapService typeSoapService;

  @Inject
  MongoClient mongoClient;

  @Inject
  DeleteRoutesResource deleteRoutesResource;

  @Inject
  ConsulRegistration consulRegistration;




  /**
   * Método para agregar una ruta dinámica
   *
   * @param uri          La URI de la ruta
   * @param portEndpoint El puerto del punto final
   * @param path         La ruta de la ruta
   * @param uriRest      La URI del servicio REST
   * @param xstlPath     La ruta del archivo XSTL
   * @param portRest     El puerto del servicio REST
   * @param host         El host de la ruta
   * @throws Exception Si ocurre un error al agregar la ruta
   */


  public void addDynamicRoute(String uri, int portEndpoint, String path, String uriRest,
                              String xstlPath, int portRest, String host) throws Exception {
    BaseCamelRoute dynamicRoute =
        new BaseCamelRoute(uri, portEndpoint, path, uriRest, xstlPath, portRest, host);
    camelContext.addRoutes(dynamicRoute);
  }

  /**
   * Método para agregar una ruta SOAP
   *
   * @param routeDto Los datos de la ruta
   * @throws Exception Si ocurre un error al agregar la ruta
   */

  public void addSoapRoute(ModifyDataHots routeDto) throws Exception {

    Document query = new Document("nameService", routeDto.getNameService());
    log.info("Query: " + query);
    Document result = (Document) getCollection().find(query).first();

    if (result != null) {
      log.info("La ruta está presente y será borrada////////////////////////////////////////////////////////////////////////////////////////////////////////////////");

      try {
        stopRouteAction(routeDto.getNameService());
        camelContext.removeRoute(routeDto.getNameService());
        log.info("Ruta detenida y eliminada exitosamente: " + routeDto.getNameService());
      } catch (Exception e) {
        log.info("Error al detener y eliminar la ruta: " + routeDto.getNameService()+e);
        throw new RouteStopException("Error al detener y eliminar la ruta: " + e.getMessage()+ e);
      }
    }

    // Parse URL and prepare route details
    Net net = urlParser.parseUrl(wsdlHostInfo.getHost(routeDto.getNameService()));
    String protocol = getValueOrDefault(routeDto.getProtocolService(), net.getProtocol());
    String host = getValueOrDefault(routeDto.getHostService(), net.getHost());
    String port = getValueOrDefault(routeDto.getPortService(), net.getPort());
    String path = getValueOrDefault(routeDto.getPathService(), net.getPath());
    String uriLocal = routeDto.getNameService();
    String nameService = routeDto.getNameService();
    String soapversion = typeSoapService.getHosts().toString();

    log.info("Preparando para agregar una nueva ruta: " + uriLocal);

    // Obtener la clase de servicio SOAP
    String serviceClass = soapServiceConfiguration.getHost();
    log.info("Clase de servicio: " + serviceClass);

    // Crear la nueva ruta
    try {
      XstlRouteTransform dynamicRoute = new XstlRouteTransform(uriLocal, nameService, protocol, host, port, path);
      camelContext.addRoutes(dynamicRoute);
      log.info("Ruta agregada exitosamente: " + uriLocal);
    } catch (Exception e) {
      log.info("Error al agregar la nueva ruta: " + uriLocal + e);
      throw new Exception("Error al agregar la nueva ruta: " + e.getMessage(), e);
    }

    // Registrar el servicio en Consul
    try {
      ConsulRegistration consulRegistration = new ConsulRegistration();
      consulRegistration.setId(nameService + "-" + port);
      consulRegistration.setName(nameService);
      consulRegistration.setTags1(soapversion);
      consulRegistration.setTags2("soap");

      Vertx vertx = Vertx.vertx();
      StartupEvent startupEvent = new StartupEvent();
      consulRegistration.init(startupEvent, vertx);
      log.info("Servicio registrado en Consul: " + nameService);
    } catch (Exception e) {
      log.info("Error al registrar el servicio en Consul: " + nameService + e);
      throw new Exception("Error al registrar el servicio en Consul: " + e.getMessage(), e);
    }

    log.info("Detalles de la ruta: uriLocal=" + uriLocal + ", nameService=" + nameService + ", protocol=" + protocol + ", host=" + host + ", port=" + port + ", path=" + path);
  }

  private MongoCollection getCollection(){
    return mongoClient.getDatabase("Service").getCollection("service");
  }



  /**
   * Método para obtener un valor o un valor predeterminado
   *
   * @param value        El valor
   * @param defaultValue El valor predeterminado
   * @return El valor o el valor predeterminado
   */
  private String getValueOrDefault(String value, String defaultValue) {
    return (value != null && !value.isEmpty()) ? value : defaultValue;
  }

  /**
   * Método para detener una ruta
   *
   * @param xstlValue El valor XSTL
   */

  public void stopRoute(XstlValue xstlValue) throws RouteStopException {
    String rutaId = xstlValue.getRouteId();
    String xstl = xstlValue.getXstlValue();



    // Detener la ruta
    try {
      stopRouteAction(rutaId);
    } catch (Exception e) {
      throw new RouteStopException("Error al detener la ruta: " + e.getMessage());
    }

    // Aplicar transformación si se proporciona un valor XSTL válido
    if (xstl != null && !xstl.isEmpty()) {
      conditionBean.setApplyTransformation(Boolean.parseBoolean(xstl));
    }

    // Iniciar la ruta
    startRoute(rutaId);
  }

  /**
   * Método para detener una ruta
   *
   * @param rutaId El ID de la ruta
   * @throws Exception Si ocurre un error al detener la ruta
   */

  private void stopRouteAction(String rutaId) throws Exception {
    producerTemplate.sendBody("controlbus:route?routeId=" + rutaId + "&action=stop", null);
  }

// Otros métodos auxiliares, como startRoute, pueden ir aquí si es necesario


// Otros métodos auxiliares, como startRoute, pueden ir aquí si es necesario

  /**
   * Método para iniciar una ruta
   *
   * @param rutaId El ID de la ruta
   */


  public void startRoute(String rutaId) {
    try {
      // Iniciar la ruta
      producerTemplate.sendBody("controlbus:route?routeId=" + rutaId + "&action=start", null);
      // Lógica adicional después de iniciar la ruta
    } catch (Exception e) {
      // Manejo de excepciones
      e.printStackTrace();
    }
  }


  public void deleteRoute(DeleteRouteDTO deleteRouteDTO) throws Exception {
    String rutaId = deleteRouteDTO.getNameService();
    Document query = new Document("nameService", deleteRouteDTO.getNameService());
    log.info("Query: " + query);
    Document result = (Document) getCollection().find(query).first();
    listRoutes();
    if (result != null) {
      String portLocalEndPoint = result.getString("portLocalEndPoint");
      String portHostService = result.getString("portService");
      logger.info(("PortLocalEndPoint: " + portLocalEndPoint));
      // Borra el documento
      // Detener y eliminar la ruta principal
      if (camelContext.getRoute(rutaId) != null) {
        try {
          // Detener la ruta
          stopRouteAction(rutaId);

          // Esperar un momento para asegurarse de que la ruta esté detenida
          Thread.sleep(1000);

          // Eliminar la ruta
          camelContext.removeRoute(rutaId);
          log.info("Route with id " + rutaId + " removed.");
          logger.info("send deleteExternalService");
          deleteRoutesResource.deleteExternalService(deleteRouteDTO);

        } catch (Exception e) {
          logger.error("Error al detener y eliminar la ruta: " + e.getMessage(), e);
          throw new RouteStopException("Error al detener la ruta: " + e.getMessage());
        }
      } else {
        log.info("No route found with id " + rutaId);
      }

      // Detener y eliminar la ruta applyXslt
      String applyXsltRouteId = rutaId + "-applyXslt";
      if (camelContext.getRoute(applyXsltRouteId) != null) {
        try {
          // Detener la ruta
          stopRouteAction(applyXsltRouteId);

          // Esperar un momento para asegurarse de que la ruta esté detenida
          Thread.sleep(1000);

          // Eliminar la ruta
          camelContext.removeRoute(applyXsltRouteId);
          log.info("Route with id " + applyXsltRouteId + " removed.");
        } catch (Exception e) {
          logger.error("Error al detener y eliminar la ruta: " + e.getMessage(), e);
          throw new RouteStopException("Error al detener la ruta: " + e.getMessage());
        }
      } else {
        log.info("No route found with id " + applyXsltRouteId);
      }
      Vertx vertx = Vertx.vertx();
      consulRegistration.deregisterService(vertx, deleteRouteDTO.getNameService()+"-"+portHostService);;
      getCollection().deleteOne(query);
      log.info("Document with nameService " + rutaId + " deleted.");
    } else {
      log.info("No document found with nameService " + rutaId);
    }
  }



  public void listRoutes() {
    camelContext.getRoutes().forEach(route -> {
      log.info("Route ID: " + route.getId() + ", Endpoint: " + route.getEndpoint().getEndpointUri());
    });
  }



}
