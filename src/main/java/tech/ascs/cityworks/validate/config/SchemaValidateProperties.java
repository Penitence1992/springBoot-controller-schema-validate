package tech.ascs.cityworks.validate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "schema")
public class SchemaValidateProperties {

    private String path = "classpath:/schema";
    private String basePackage = "tech.ascs.cityworks";
    private boolean flatMode = false;
    private String[] annotations = new String[]{
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.PatchMapping"
    };
}
