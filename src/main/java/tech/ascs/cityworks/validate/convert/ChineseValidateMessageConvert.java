package tech.ascs.cityworks.validate.convert;

import com.networknt.schema.ValidationMessage;
import tech.ascs.cityworks.validate.base.ValidateMessageConvert;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class ChineseValidateMessageConvert implements ValidateMessageConvert {
    @Override
    public Set<String> convert(Set<ValidationMessage> validationMessages) {
//        StringBuilder builder = new StringBuilder();
        Set<String> message = new HashSet<>();
        validationMessages.forEach(validationMessage -> {
            if ("required".equals(validationMessage.getType())) {
                message.add(argsConcat(getArgs(validationMessage)) + "参数必须");
            }else {
                message.add(validationMessage.toString());
            }
        });

        return message;
    }

    private String parsePath(String path){
        String tmp = path;
        if(path.startsWith("$.")){
            tmp = path.substring(2);
        }
        return tmp;
    }

    private String argsConcat(String[] args){
        StringJoiner joiner = new StringJoiner(",");
        Stream.of(args).forEach(joiner::add);
        return joiner.toString();
    }

    private String[] getArgs(ValidationMessage validationMessage){
        try {
            Field field = validationMessage.getClass().getDeclaredField("arguments");
            field.setAccessible(true);
            return (String[]) field.get(validationMessage);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return new String[]{};
    }
}
