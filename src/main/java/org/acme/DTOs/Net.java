package org.acme.DTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Net {

    private String protocol;
    private String host;
    private String port;
    private String path;


}
