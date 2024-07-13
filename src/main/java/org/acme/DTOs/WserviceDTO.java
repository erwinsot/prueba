package org.acme.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WserviceDTO {
  private String protocolService;
    private String hostService;
    private String portService;
    private String uriService;
}
