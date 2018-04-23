package tech.ascs.cityworks.validate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties("schema")
public class SchemaValidateProperties {

    private String path = "classpath:/schema";

    private String basePackage = "tech.ascs.cityworks";

    private boolean flatMode = false;

    private List<String> annotations = Arrays.asList(
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.PatchMapping"
    );
}
