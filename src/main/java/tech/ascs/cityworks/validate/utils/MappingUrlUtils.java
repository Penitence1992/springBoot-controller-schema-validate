package tech.ascs.cityworks.validate.utils;

import java.util.regex.Pattern;

public class MappingUrlUtils {

    private static final String REG_TO_SAME = "\\{(\\w+)[^:]}";
//    private static final Pattern REG_TO_SAME_PATTERN = Pattern.compile(REG_TO_SAME);

    private static final String REG_TO_REG_VARIABLE = "\\{(\\w+):?(.*?)?\\}";
    private static final Pattern REG_TO_REG_VARIABLE_PATTERN = Pattern.compile(REG_TO_REG_VARIABLE);

    public static String pathVariableConvert(String url){
        if (REG_TO_REG_VARIABLE_PATTERN.matcher(url).find()){
            return url.replaceAll(REG_TO_SAME, "{$1:.*}").replaceAll(REG_TO_REG_VARIABLE, "{$2}");

        }else {
            return url;
        }
    }
}
