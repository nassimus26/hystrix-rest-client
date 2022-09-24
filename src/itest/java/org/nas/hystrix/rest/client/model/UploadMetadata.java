package org.nas.hystrix.rest.client.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Nassim MOUALEK on 24/09/2018.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadMetadata {
    private String username;

    private Integer position;
}
