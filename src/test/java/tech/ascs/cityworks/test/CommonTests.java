package tech.ascs.cityworks.test;

import org.junit.Test;

import java.util.regex.Pattern;

public class CommonTests {

    @Test
    public void testCommon() {
        String url1 = "/////////users//{userId}/one";

        System.out.println(url1.replaceAll("/+", "/"));
    }

}
