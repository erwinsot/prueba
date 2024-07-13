package org.acme.configurations;

import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.consul.ConsulClientOptions;
import io.vertx.ext.consul.ServiceOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.consul.ConsulClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is responsible for registering a service with Consul.
 * Consul is a service that provides service discovery, configuration, and orchestration capabilities.
 * This class is annotated with @ApplicationScoped, meaning a single instance will be created for the entire application.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApplicationScoped
public class ConsulRegistration {

  String setHostConsul= System.getenv().getOrDefault ("SET_HOST_CONSUL","localhost");
  String setHostAdressRegister = System.getenv().getOrDefault("SET_HOST_ADRESS_REGISTER", "localhost");
  int setPortConsul = Integer.parseInt(System.getenv().getOrDefault("SET_PORT_CONSUL", "8500"));
  int setPortAdressRegister = Integer.parseInt(System.getenv().getOrDefault("SET_PORT_ADRESS_REGISTER", "12505"));

  private String name;
  private String id;
  private String tags1;
  private String tags2;

  /**
   * This method is called at application startup.
   * It creates a Consul client and registers a service with the provided name, id, and tags.
   * If any of the required values are null, the method will return without doing anything.
   *
   * @param ev    the startup event, provided by the Quarkus runtime
   * @param vertx the Vert.x instance, used to create the Consul client
   */
  public void init(@Observes StartupEvent ev, Vertx vertx) {
    // Verificar si los valores necesarios son nulos
    if (name == null || id == null || tags1 == null || tags2 == null) {
      // Si alguno de los valores es nulo, no proceder con la inicialización
      return;
    }

    // Create a Consul client with the default host (localhost) and port (8500)
    ConsulClient client = ConsulClient.create(vertx, new ConsulClientOptions()
        .setHost(setHostConsul) //localhost
        .setPort(setPortConsul)); //8500

    // Register a service with the provided name, id, and tags
    // The service is running on localhost and port 12501
    client.registerServiceAndAwait(new ServiceOptions()
        .setName(name)
        .setId(id)
        .setAddress(setHostAdressRegister) //localhost // La dirección en la que se está ejecutando tu servicio
        .setPort(setPortAdressRegister) //12505 // El puerto en el que se está ejecutando tu servicio
        .setTags(new ArrayList<>(Arrays.asList(tags1, tags2))));
  }

  public void deregisterService(Vertx vertx, String id) {
    // Create a Consul client with the configured host and port
    ConsulClient client = ConsulClient.create(vertx, new ConsulClientOptions()
            .setHost(setHostConsul)
            .setPort(setPortConsul));

    // Deregister the service with Consul using its ID
    client.deregisterServiceAndAwait(id);
  }
}