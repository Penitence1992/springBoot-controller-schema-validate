package tech.ascs.cityworks.validate.utils;

import java.util.StringJoiner;

public class StringUtils {

    public static String joinString(Iterable<String> collection, CharSequence delimiter){
        StringJoiner joiner = new StringJoiner(delimiter);
        collection.forEach(joiner::add);
        return joiner.toString();
    }
}
