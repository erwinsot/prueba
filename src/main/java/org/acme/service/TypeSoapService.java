package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for retrieving the hosts from a WSDL file.
 * It is annotated with @ApplicationScoped, meaning a single instance will be created for the entire application.
 */
@ApplicationScoped
public class TypeSoapService {

  /**
   * This method is used to get the hosts from the first WSDL file in the 'wsdl' directory.
   * It reads the WSDL file line by line and uses regular expressions to find the hosts.
   *
   * @return a list of hosts from the WSDL file, or an empty list if no hosts could be found
   */
  public List<String> getHosts() {
    try {
      String folderPath = "src/main/resources/wsdl/";
      File folder = new File(folderPath);

      if (folder.exists() && folder.isDirectory()) {
        File[] files = folder.listFiles();
        if (files != null && files.length > 0) {
          // Take the first resource (you can adjust this according to your needs)
          File firstFile = files[0];

          // Get the service addresses from the first WSDL file
          List<String> locations = getLocationFromWSDLFile(firstFile);
          return locations;
        } else {
          System.out.println("La carpeta 'wsdl' está vacía.");
        }
      } else {
        System.out.println("La carpeta 'wsdl' no existe o no es una carpeta.");
      }

    } catch (IOException e) {
      System.out.println("Error al extraer la dirección del servicio: " + e.getMessage());
    }

    return new ArrayList<>();
  }

  /**
   * This method is used to get the locations from a WSDL file.
   * It reads the WSDL file line by line and uses regular expressions to find the locations.
   *
   * @param wsdlFile the WSDL file to extract the locations from
   * @return a list of locations from the WSDL file, or an empty list if no locations could be found
   * @throws IOException if an error occurs while reading the file
   */
  private List<String> getLocationFromWSDLFile(File wsdlFile) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(wsdlFile));
    String line;

    // List to store the found addresses
    List<String> locations = new ArrayList<>();

    // Pattern for detecting SOAP 1.1 address
    Pattern soap11Pattern = Pattern.compile("<soap:address\\s+location=\"(.*?)\"\\s*/>");
    // Pattern for detecting SOAP 1.2 address
    Pattern soap12Pattern = Pattern.compile("<soap12:address\\s+location=\"(.*?)\"\\s*/>");
    // Pattern for detecting xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" (SOAP 1.1 indication)
    Pattern xmlnsSoap11Pattern = Pattern.compile("xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"");

    // Search for the service address in the file content
    while ((line = reader.readLine()) != null) {
      Matcher matcher11 = soap11Pattern.matcher(line);
      Matcher matcher12 = soap12Pattern.matcher(line);
      Matcher xmlnsSoap11Matcher = xmlnsSoap11Pattern.matcher(line);

      if (matcher11.find()) {
        locations.add("SOAP 1.1: " + matcher11.group(1));
      }

      if (matcher12.find()) {
        locations.add("SOAP 1.2: " + matcher12.group(1));
      }

      // Also check the case without namespace prefix for SOAP 1.1
      if (line.contains("address") && line.contains("location") && !line.contains("soap:address")) {
        String location = line.replaceAll(".*location=\"([^\"]*)\".*", "$1");
        locations.add("SOAP 1.1 (sin prefijo): " + location);
      }

      // Check xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" (SOAP 1.1 indication)
      if (xmlnsSoap11Matcher.find()) {
        locations.add("SOAP 1.1 (xmlns:soap): " + wsdlFile.getName());
      }
    }
    // Close the BufferedReader reader
    reader.close();

    // Print the found addresses
    if (!locations.isEmpty()) {
      System.out.println("Found the following SOAP addresses:");
      for (String location : locations) {
        System.out.println(location);
      }
    } else {
      System.out.println("The WSDL does not use SOAP 1.1 or SOAP 1.2");
    }

    return locations;
  }

}