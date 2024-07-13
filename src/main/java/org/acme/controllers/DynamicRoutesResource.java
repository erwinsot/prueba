package org.acme.controllers;

import static java.lang.Thread.sleep;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import lombok.extern.java.Log;
import org.acme.DTOs.*;
import org.acme.configurations.RouteStopException;
import org.acme.entity.VariableNameEntity;
import org.acme.service.UrlParser;
import org.acme.service.WsService;
import org.acme.service.WsdlHostInfo;
import org.acme.servicesCamel.DynamicRoutesManager;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import java.io.InputStream;

/**
 * This class is a JAX-RS resource that provides endpoints for managing dynamic routes.
 */
@Path("/dynamic")
@Log
@Transactional
public class DynamicRoutesResource {

  DynamicRoutesManager dynamicRoutesManager;
  ProducerTemplate producerTemplate;
  WsdlHostInfo wsdlHostInfo;

  @Inject
  WsService wsService;

  @Inject
  UrlParser urlParser;


  /**
   * Constructor for the DynamicRoutesResource class.
   *
   * @param dynamicRoutesManager The service for managing dynamic routes.
   * @param producerTemplate     The Camel template for producing messages.
   * @param wsdlHostInfo         The service for getting host information.
   */
  @Inject
  public DynamicRoutesResource(DynamicRoutesManager dynamicRoutesManager,
                               ProducerTemplate producerTemplate, WsdlHostInfo wsdlHostInfo) {
    this.dynamicRoutesManager = dynamicRoutesManager;
    this.producerTemplate = producerTemplate;
    this.wsdlHostInfo = wsdlHostInfo;
  }

  /**
   * Endpoint for adding a dynamic route.
   *
   * @param routeData The data for the route to add.
   * @return A response indicating the result of the operation.
   * @throws Exception If an error occurs while adding the route.
   */
  @POST
  @Path("/add")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response addDynamicRoute(RouteData routeData) throws Exception {
    dynamicRoutesManager.addDynamicRoute(routeData.getUri(), routeData.getPortEndpoint(),
        routeData.getPath(), routeData.getUriRest(), routeData.getXstlPath(),
        routeData.getPortRest(), routeData.getHost());
    return Response.status(Response.Status.CREATED).entity("Route created successfully").build();
  }

  /**
   * Endpoint for uploading a WSDL file.
   *
   * @param formData The form data containing the WSDL file.
   * @return A response indicating the result of the operation.
   */
  @POST
  @Path("/uploadwsdl")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_PLAIN)
  public Response uploaWsdl(FormData formData) throws IOException {
    InputStream wsdlContent = formData.getWsdlFile();
    String fileName = formData.getWsdlDetail();
    log.info("File name: " + wsdlContent);
    producerTemplate.sendBodyAndHeader("direct:processWsdl", wsdlContent, Exchange.FILE_NAME,
        fileName);
    wsService.createWs(formData);
    return Response.ok("Archivo WSDL recibido y procesado").build();
  }

  /**
   * Endpoint for uploading an XSLT file.
   *
   * @param xsltData The form data containing the XSLT file.
   * @return A response indicating the result of the operation.
   */
  @POST
  @Path("/uploadxslt")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_PLAIN)
  public Response uploaXstl(XsltData xsltData) {
    InputStream wsdlContent = xsltData.getXsltFile();
    String fileName = xsltData.getXsltDetail();
    log.info("File name: " + wsdlContent);
    producerTemplate.sendBodyAndHeader("direct:processXslt", wsdlContent, Exchange.FILE_NAME,
        fileName);
    return Response.ok("Archivo WSDL recibido y procesado").build();
  }

  /**
   * Endpoint for creating a SOAP route.
   *
   * @param soapRoute The data for the SOAP route to create.
   * @return A response indicating the result of the operation.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/soap")
  public Response createSoapRoute(ModifyDataHots soapRoute) {
    try {
      dynamicRoutesManager.addSoapRoute(soapRoute);
      return Response.ok("ruta creada correctamente").build();
    } catch (Exception e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  /**
   * Endpoint for getting host information.
   *
   * @return A response containing the host information.
   */
 /* @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/hostinfo")
  public Response getHostInfo() {
    log.info("Host info: " + wsdlHostInfo.getHost());
    return Response.ok(wsdlHostInfo.getHost()).build();
  }*/

  /**
   * Endpoint for setting the XSTL value.
   *
   * @param xstlValue The XSTL value to set.
   * @return A response indicating the result of the operation.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/stopRoute")
  public Response setXstl(XstlValue xstlValue) throws RouteStopException {
    dynamicRoutesManager.stopRoute(xstlValue);
    return Response.ok("Xstl agregado correctamente").build();
  }

  /**
   * Endpoint for starting a route.
   *
   * @param routeId The ID of the route to start.
   * @return A response indicating the result of the operation.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/startRoute")
  public Response startRoute(XstlValue routeId) {
    try {
      dynamicRoutesManager.startRoute(routeId.getRouteId());
      return Response.ok("Ruta iniciada correctamente").build();
    } catch (Exception e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  /**
   * Endpoint for getting host information.
   *
   * @return A response containing the host information.
   */

  @POST
  @Path("/dataHost")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getHostData(DataResponseHost dataResponseHost) {
    log.info("Name service: " + dataResponseHost.getNameService());
    return Response.ok(wsService.getAllWs(dataResponseHost.getNameService())).build();

  }

  /**
   * Endpoint for modifying a route.
   *
   * @param modifyDataHots The data for the route to modify.
   * @return A response indicating the result of the operation.
   * @throws Exception If an error occurs while modifying the route.
   */
  @POST
  @Path("/modify")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response modifyRoute(ModifyDataHots modifyDataHots) throws Exception {
    log.info("Name service: " + modifyDataHots.getNameService());
    wsService.modifyWsByName(modifyDataHots);
    dynamicRoutesManager.addSoapRoute(modifyDataHots);
    return Response.status(Response.Status.CREATED).entity("Route modified successfully").build();
  }


  @POST
  @Path("variable")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response addVariable(VariableNameEntity variable) {
    wsService.saveVariable(variable);
    return Response.status(Response.Status.CREATED).entity("Variable created successfully").build();
  }

  /**
   * Endpoint for getting a variable.
   *
   * @return A response containing the variable.
   */


  @GET
  @Path("/getVariable")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getVariable() {
    return Response.ok(wsService.getVariable()).build();
  }

  @POST
  @Path("/deleteRoute")
  @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoute(DeleteRouteDTO deleteRoute) {
        try {
        dynamicRoutesManager.deleteRoute(deleteRoute);
        return Response.ok("Ruta eliminada correctamente").build();
        } catch (Exception e) {
        return Response.serverError().entity(e.getMessage()).build();
        }
    }






}