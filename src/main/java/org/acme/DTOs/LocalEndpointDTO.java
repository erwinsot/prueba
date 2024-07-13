package org.acme.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalEndpointDTO {
  private String nameLocalEndpoint;
  private String uri;
}
