package org.acme.DTOs;

import jakarta.ws.rs.FormParam;
import lombok.Data;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;


import java.io.InputStream;
@Data
public class FormData {

    @FormParam("wsdlFile")
    private InputStream wsdlFile;
    @FormParam("wsdlDetail")
    private String wsdlDetail;
}
