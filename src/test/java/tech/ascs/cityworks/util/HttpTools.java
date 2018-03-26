package tech.ascs.cityworks.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.Map;

public class HttpTools {

    public static void uploadToFarServiceByHttpClient(String remoteUrl, Map<String, File> files, Map<String, String> texts) {
        HttpClient httpclient = HttpClientBuilder.create().build();
        try {
            HttpPost httppost = new HttpPost(remoteUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            files.forEach((name, file)-> builder.addBinaryBody(name,file, ContentType.create("image/jpeg"),file.getName()));
            texts.forEach(builder::addTextBody);
            httppost.setEntity(builder.build());
            HttpResponse response = httpclient.execute(httppost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                System.out.println("服务器正常响应.....");
                HttpEntity resEntity = response.getEntity();
                System.out.println(EntityUtils.toString(resEntity,"UTF-8"));// httpclient自带的工具类读取返回数据
                EntityUtils.consume(resEntity);
            }else {
                System.out.println(copyToString(response.getEntity().getContent()));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String copyToString(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ( (line = reader.readLine()) != null){
            builder.append(line).append("\n");
        }
        return builder.toString();
    }
}
