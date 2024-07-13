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
public class SoapServiceConfiguration {

  /**
   * This method is used to get the host from the first WSDL file in the 'wsdl' directory.
   * It reads the WSDL file line by line and uses a regular expression to find the host.
   *
   * @return the host from the WSDL file, or null if the host could not be found
   */
  public String getHost() {
    try {
      String folderPath =
          "src/main/resources/wsdl/";
      File folder = new File(folderPath);

      if (folder.exists() && folder.isDirectory()) {
        File[] files = folder.listFiles();
        if (files != null && files.length > 0) {
          // Take the first resource (you can adjust this according to your needs)
          File firstFile = files[0];

          // Get the service address from the first WSDL file
          String location = getLocationFromWSDLFile(firstFile);
          return location;
        } else {
          System.out.println("The 'wsdl' folder is empty.");
        }
      } else {
        System.out.println("The 'wsdl' folder does not exist or is not a folder.");
      }

    } catch (IOException e) {
      System.out.println("Error extracting the service address: " + e.getMessage());
    }

    return null;
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
    Pattern pattern = Pattern.compile("<portType\\s+name=\"(.*?)\"\\s*>");

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