package tech.ascs.cityworks.validate.convert;

import com.networknt.schema.ValidationMessage;
import org.springframework.stereotype.Component;
import tech.ascs.cityworks.validate.base.ValidateMessageConvert;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

public class ChineseValidateMessageConvert implements ValidateMessageConvert {

    @Override
    public Set<String> convert(Set<ValidationMessage> validationMessages) {
        Map<String,Set<String>> messageCache = new HashMap<>();
        validationMessages.forEach(validationMessage -> {
            switch (ValidateFailType.valueOfName(validationMessage.getType())){
                case ENUM:
                    putIntoMessageCache(getArgName(validationMessage.getPath()), "值必须为"+argsConcat(getArgs(validationMessage)) + "中的一个", messageCache);
                    break;
                case PATTERN:
                    putIntoMessageCache(getArgName(validationMessage.getPath()), "不符合规则:" + argsConcat(getArgs(validationMessage)), messageCache);
                    break;
                case REQUIRED:
                    putIntoMessageCache(argsConcat(getArgs(validationMessage)), "参数必须", messageCache);
                    break;
                case MAX_LENGTH:
                    putIntoMessageCache(getArgName(validationMessage.getPath()), "最大长度为" + argsConcat(getArgs(validationMessage)) + "个字符", messageCache);
                    break;
                case MIN_LENGTH:
                    putIntoMessageCache(getArgName(validationMessage.getPath()), "最少长度为" + argsConcat(getArgs(validationMessage)) + "个字符", messageCache);
                    break;
                case TYPE:
                    String[] args = getArgs(validationMessage);
                    putIntoMessageCache(getArgName(validationMessage.getPath()), "类型不匹配,类型应该为" + args[1], messageCache);
                    break;
                case UN_KNOWN:
                    putIntoMessageCache("", validationMessage.getMessage(), messageCache);
                    break;
            }
        });
        return convertMessageCacheToSet(messageCache);
    }

    private void putIntoMessageCache(String name, String message, Map<String,Set<String>> messageCache){
        if(!messageCache.containsKey(name)){
            messageCache.put(name, new HashSet<>());
        }
        messageCache.get(name).add(message);
    }

    private Set<String> convertMessageCacheToSet(Map<String,Set<String>> messageCache){
        Set<String> set = new HashSet<>();
        messageCache.forEach((name, messages) -> {
            if("".equals(name)){
                set.addAll(messages);
                return;
            }
            StringJoiner joiner = new StringJoiner(",");
            messages.forEach(joiner::add);
            set.add(name + joiner.toString());
        });
        return set;
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

    private String getArgName(String path){
        String name = path;
        if(name.contains(".")){
            name = name.substring(name.lastIndexOf(".") + 1);
        }
        return name;
    }
}
