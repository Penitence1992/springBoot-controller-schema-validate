package tech.ascs.cityworks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.ascs.cityworks.validate.base.EnableSchemaValidation;

import java.util.Objects;

/**
 * Created by RenJie on 2017/6/29 0029.
 */
@SpringBootApplication
@RestController
@EnableSchemaValidation
public class Launch {

    public static void main(String[] args){
        SpringApplication.run(Launch.class,args);
    }


    @RequestMapping({"/validator/api1","/validator/api3"})
    public String validateController1(String username, String password){
        return "SUCCESS";
    }

    @GetMapping("/validator/api2")
    public String validateController2(String username, String password){
        return "SUCCESS";
    }

    @PostMapping("/validator/api4")
    public String validateControllerRequestBody(@RequestBody String body){
        return "SUCCESS";
    }

    @PostMapping("/validator/api5")
    public String validateControllerRequestBodyBean(@RequestBody TestBean body){
        return "SUCCESS";
    }

    @PostMapping("/validator/api6")
    public String validateControllerWithFile(@RequestParam MultipartFile username){
        if(Objects.isNull(username)){
            return "FAIL";
        }
        return "SUCCESS";
    }

}
