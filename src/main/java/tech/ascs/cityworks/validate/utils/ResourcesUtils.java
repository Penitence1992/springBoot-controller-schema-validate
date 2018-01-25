package tech.ascs.cityworks.validate.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by RenJie on 2017/7/14 0014.
 */
public class ResourcesUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesUtils.class);

    private static final boolean IS_TRACE_ENABLED = LOGGER.isTraceEnabled();

    public static String readToString(String path) throws IOException {
        if (IS_TRACE_ENABLED) {
            LOGGER.trace("Load File by path : {}", path);
        }
        if (path.startsWith("classpath:")) {
            path = path.substring(10);
            Resource resource = new ClassPathResource(path);
            return FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
        } else {
            return FileCopyUtils.copyToString(new FileReader(path));
        }
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }

        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();

        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());

        return bigInt.toString(16);
    }
}
