package tech.ascs.cityworks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tech.ascs.cityworks.validate.base.EnableSchemaValidation;

/**
 * Created by RenJie on 2017/6/29 0029.
 */
@SpringBootApplication
@EnableSchemaValidation
public class JsonSchemaLaunch {

    public static void main(String[] args){

        SpringApplication.run(JsonSchemaLaunch.class,args);
    }

}
