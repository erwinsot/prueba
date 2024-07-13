package org.acme.DTOs;

import jakarta.ws.rs.FormParam;
import java.io.InputStream;
import lombok.Data;

@Data
public class XsltData {
  @FormParam("xsltFile")
  private InputStream xsltFile;
  @FormParam("xsltDetail")
  private String xsltDetail;
}
