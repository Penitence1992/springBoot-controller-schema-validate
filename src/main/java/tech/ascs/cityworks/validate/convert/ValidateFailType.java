package tech.ascs.cityworks.validate.convert;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ValidateFailType {
    REQUIRED("required"),
    ENUM("enum"),
    MAX_LENGTH("maxLength"),
    MIN_LENGTH("minLength"),
    PATTERN("pattern"),
    TYPE("type"),
    UN_KNOWN("unknown");
    private String name;

    ValidateFailType(String name){
        this.name = name;
    }

    private static Map<String, ValidateFailType> typeMap;

    static {
        typeMap = Stream.of(values()).collect(Collectors.toMap(ValidateFailType::getName, t -> t));
    }

    public static ValidateFailType valueOfName(String value){
        return Optional.ofNullable(typeMap.get(value)).orElse(UN_KNOWN);
    }

    public String getName(){
        return this.name;
    }
}
