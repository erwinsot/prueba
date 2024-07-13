package org.acme.DTOs;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApplicationScoped
@NoArgsConstructor
public class ModifyDataHots {
  private String nameService;
  private String protocolService;
  private String hostService;
  private String portService;
  private String pathService;
}
