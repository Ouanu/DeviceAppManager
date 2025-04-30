package org.ouanu.manager.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties("storage")
public class StorageProperties {
    private String location = "upload_dir";
}