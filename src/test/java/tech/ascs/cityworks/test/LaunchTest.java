package tech.ascs.cityworks.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tech.ascs.cityworks.Launch;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by RenJie on 2017/6/29 0029.
 * 测试类
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Launch.class)
public class LaunchTest extends MockMvcResultMatchers {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @PostConstruct
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testDoValidateControllerFail() {
        RequestBuilder builder = MockMvcRequestBuilders.get("/validator/api1?username=aa");
        Object object = null;
        try {
            int status = mockMvc.perform(builder).andReturn().getResponse().getStatus();
        } catch (Exception exception) {
            object = new Object();
            exception.printStackTrace();
        }
        Assert.assertNotNull("throw runtime exception and then object not null", object);

    }

    @Test
    public void testDoValidateControllerSuccess() throws Exception {
        RequestBuilder builder = MockMvcRequestBuilders.get("/validator/api1?username=aaaaaaa&password=asd");
        String result = mockMvc.perform(builder).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Assert.assertEquals("return message must SUCCESS", "SUCCESS", result);
    }

    @Test
    public void testNoValidateController() throws Exception {
        RequestBuilder builder = MockMvcRequestBuilders.get("/validator/api2?username=aa");
        String result = mockMvc.perform(builder).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Assert.assertEquals("return message must SUCCESS", "SUCCESS", result);
    }

    @Test
    public void testNoValidateController3() throws Exception {
        RequestBuilder builder = MockMvcRequestBuilders.get("/validator/api3?username=aa");
        String result = mockMvc.perform(builder).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Assert.assertEquals("return message must SUCCESS", "SUCCESS", result);
    }

    @Test
    public void testDoValidateController4TextFail() throws Exception {
        RequestBuilder builder = MockMvcRequestBuilders.post("/validator/api4")
                .contentType(MediaType.TEXT_PLAIN)
                .content("aaaasdsadsad");
        Object object = null;
        try {
            int status = mockMvc.perform(builder).andReturn().getResponse().getStatus();
        } catch (Exception exception) {
            object = new Object();
            exception.printStackTrace();
        }
        Assert.assertNotNull("throw runtime exception and then object not null", object);
    }

    @Test
    public void testDoValidateController4JsonSuccess() throws Exception {
        RequestBuilder builder = MockMvcRequestBuilders.post("/validator/api4")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"a\":\"b\"}");
        String result = mockMvc.perform(builder).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Assert.assertEquals("return message must SUCCESS", "SUCCESS", result);
    }

    @Test
    public void testDoValidateController5JsonSuccess() throws Exception {
        RequestBuilder builder = MockMvcRequestBuilders.post("/validator/api5")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"username\":\"ppppppp\"}");
        String result = mockMvc.perform(builder).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Assert.assertEquals("return message must SUCCESS", "SUCCESS", result);
    }

    @Test
    public void testDoValidateController6File() throws Exception {
//        RequestBuilder builder = MockMvcRequestBuilders.post("/validator/api6")
//                .contentType(MediaType.MULTIPART_FORM_DATA)
//                .

        RequestBuilder builder = MockMvcRequestBuilders.fileUpload("/validator/api6")
                .file(new MockMultipartFile("username", new FileInputStream("/Users/penitence/setSkyNode/13_BK.jpg")));
        String result = mockMvc.perform(builder).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        System.out.println(result);
    }
}
