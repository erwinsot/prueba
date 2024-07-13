package org.acme.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.java.Log;
import org.acme.DTOs.FormData;
import org.acme.DTOs.LocalEndpointDTO;
import org.acme.DTOs.ModifyDataHots;
import org.acme.DTOs.Net;
import org.acme.DTOs.WserviceDTO;
import org.acme.entity.VariableNameEntity;
import org.bson.Document;


/**
 * This class is responsible for managing web services.
 * It is annotated with @ApplicationScoped, meaning a single instance will be created for the entire application.
 * It is also annotated with @Log, which is a Lombok annotation that provides a logger.
 */
@ApplicationScoped
@Log
public class WsService {


  @Inject
  UrlParser urlParser;

  @Inject
  WsdlHostInfo wsdlHostInfo;


  @Inject
  MongoClient mongoClient;

  /**
   * This method is used to create a new web service.
   * It first retrieves the host from the WSDL file using the WsdlHostInfo service.
   * Then it creates a new WserviceEntity and sets its properties based on the retrieved host and the provided FormData.
   * Finally, it persists the new WserviceEntity using the WsRespository.
   *
   * @param formData the form data containing the details of the new web service
   * @throws IOException if an error occurs during host retrieval
   */
  public void createWs(FormData formData) throws IOException {
    Document query = new Document("nameService", formData.getWsdlDetail());
    Document result = (Document) getCollection().find(query).first();



    if ( result != null) {
      log.info("El servicio ya existe");
      return;
    }

    Net net = urlParser.parseUrl(wsdlHostInfo.getHost(formData.getWsdlDetail()));
    ModifyDataHots modifyDataHots = new ModifyDataHots();
    modifyDataHots.setHostService(net.getHost());
    modifyDataHots.setPortService(net.getPort());
    modifyDataHots.setProtocolService(net.getProtocol());
    modifyDataHots.setPathService(net.getPath());
    modifyDataHots.setNameService(formData.getWsdlDetail());
    addRouteMongo(modifyDataHots);
  }

  /**
   * This method is used to retrieve all web services with a specific name.
   * It first retrieves the WserviceEntity with the specified name using the WsRespository.
   * Then it maps the WserviceEntity to a WserviceDTO and returns it.
   *
   * @param nameService the name of the service to retrieve
   * @return an Optional containing the WserviceDTO if found, or an empty Optional otherwise
   */
  public Optional<WserviceDTO> getAllWs(String nameService) {
    Document query = new Document("nameService", nameService);
    Document result = (Document) getCollection().find(query).first();

    if (result != null) {
      WserviceDTO wserviceDTO = new WserviceDTO();
      wserviceDTO.setHostService(result.getString("hostService"));
      wserviceDTO.setPortService(result.getString("portService"));
      wserviceDTO.setProtocolService(result.getString("protocolService"));
      wserviceDTO.setUriService(result.getString("pathService"));
      return Optional.of(wserviceDTO);
    }
    return Optional.empty();
  }

  /**
   * This method is used to modify a web service by its name.
   * It first retrieves the WserviceEntity with the specified name using the WsRespository.
   * If the WserviceEntity is found, it updates its properties based on the provided ModifyDataHots and persists the changes using the WsRespository.
   *
   * @param modifyDataHots the data containing the new details of the web service
   */
  public void modifyWsByName(ModifyDataHots modifyDataHots) {

    Document query = new Document("nameService", modifyDataHots.getNameService());
    Document result = (Document) getCollection().find(query).first();

    if (result != null) {
        Document update = new Document("$set", new Document()
                .append("nameService", modifyDataHots.getNameService())
                .append("hostService", modifyDataHots.getHostService())
                .append("portService", modifyDataHots.getPortService())
                .append("protocolService", modifyDataHots.getProtocolService())
                .append("pathService", modifyDataHots.getPathService()));
        getCollection().updateOne(query, update);
    }
    else {
      Document document = new Document()
              .append("nameService", modifyDataHots.getNameService())
              .append("hostService", modifyDataHots.getHostService())
              .append("portService", modifyDataHots.getPortService())
              .append("protocolService", modifyDataHots.getProtocolService())
              .append("pathService", modifyDataHots.getPathService());
      getCollection().insertOne(document);
    }

  }

  public void addRouteMongo(ModifyDataHots modifyDataHots){
    Document document = new Document()
            .append("nameService", modifyDataHots.getNameService())
            .append("hostService", modifyDataHots.getHostService())
            .append("portService", modifyDataHots.getPortService())
            .append("protocolService", modifyDataHots.getProtocolService())
            .append("pathService", modifyDataHots.getPathService());



    getCollection().insertOne(document);

  }

  private MongoCollection getCollection(){
    return mongoClient.getDatabase("Service").getCollection("service");
  }

  /**
   * This method is used to save a variable.
   * It first retrieves the first VariableNameEntity from the VariableNameRepository.
   * If a VariableNameEntity is found, it updates its nameService property with the new value and persists the changes using the VariableNameRepository.
   * If no VariableNameEntity is found, it persists the new VariableNameEntity using the VariableNameRepository.
   *
   * @param variableNameEntity the VariableNameEntity to save
   */
  public void saveVariable(VariableNameEntity variableNameEntity) {
    Document query = new Document("nameService", variableNameEntity.getNameService());
    log.info("Query: " + query);
    Document result = (Document) getCollectionVariable().find(query).first();

    if (result != null) {
      Document update = new Document("$set", new Document()
              .append("nameService", variableNameEntity.getNameService()));
      getCollectionVariable().updateOne(query, update);
      return;
    }

    Document document = new Document()
            .append("nameService", variableNameEntity.getNameService());

    getCollectionVariable().insertOne(document);
  }

  private MongoCollection getCollectionVariable(){
    return mongoClient.getDatabase("variable").getCollection("variable");
  }
  /**
   * This method is used to retrieve the first column name from the VariableNameRepository.
   *
   * @return the first column name
   */
  public String getVariable() {
    Document firstDocument = (Document) getCollectionVariable().find().first();
    if (firstDocument != null && firstDocument.containsKey("nameService")) {
      return firstDocument.getString("nameService");
    }
    return null;
  }

}