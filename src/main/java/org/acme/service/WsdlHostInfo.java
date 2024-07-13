package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.java.Log;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for retrieving the host from a WSDL file.
 * It is annotated with @ApplicationScoped, meaning a single instance will be created for the entire application.
 * It is also annotated with @Log, which is a Lombok annotation that provides a logger.
 */
@ApplicationScoped
@Log
public class WsdlHostInfo {
  // Directory where the WSDL files are stored
  private static final String WSDL_DIRECTORY = "src/main/resources/wsdl/";

  /**
   * This method is used to get the host from a WSDL file with a specific name.
   * It first checks for the presence of WSDL files in the WSDL_DIRECTORY.
   * If a WSDL file with the specified name is found, it retrieves the host from the file.
   *
   * @param nameService the name of the service for which to retrieve the host
   * @return the host from the WSDL file, or null if the host could not be found
   * @throws IOException if an error occurs during file reading
   */
  public String getHost(String nameService) throws IOException {
    var wsdlFiles = new File(WSDL_DIRECTORY).listFiles((dir, name) -> name.endsWith(".wsdl"));
    if (wsdlFiles == null || wsdlFiles.length == 0) {
      log.info("Ningún archivo WSDL encontrado");
      return null;
    }
    File desiredWsdlFile = null;
    for (File file : wsdlFiles) {
      if (file.getName().equals(nameService + ".wsdl")) {
        desiredWsdlFile = file;
        log.info("Se encontró el archivo WSDL deseado: " + nameService);
        break;
      }
    }
    if (desiredWsdlFile == null) {
      log.info("No se encontró el archivo WSDL deseado: " + nameService);
      return null;
    }

    // Get the service address from the found WSDL file
    return getLocationFromWSDLFile(desiredWsdlFile);
  }

  /**
   * This method is used to get the location from a WSDL file.
   * It reads the WSDL file line by line and uses a regular expression to find the location.
   *
   * @param wsdlFile the WSDL file to extract the location from
   * @return the location from the WSDL file, or null if the location could not be found
   * @throws IOException if an error occurs while reading the file
   */
  private String getLocationFromWSDLFile(File wsdlFile) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(wsdlFile));
    String line;
    Pattern pattern = Pattern.compile("<soap:address\\s+location=\"(.*?)\"\\s*/>");

    // Search for the service address in the file content
    while ((line = reader.readLine()) != null) {
      Matcher matcher = pattern.matcher(line);
      if (matcher.find()) {
        // The service address is found in the first group captured by the pattern
        String location = matcher.group(1);
        // Close the BufferedReader reader
        reader.close();
        return location;
      }
    }
    // Close the BufferedReader reader
    reader.close();
    return null;
  }
}