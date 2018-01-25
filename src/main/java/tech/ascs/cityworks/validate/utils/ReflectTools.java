package tech.ascs.cityworks.validate.utils;

import com.esotericsoftware.reflectasm.MethodAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by RenJie on 2017/6/1 0001.
 * 反射工具类
 */
public class ReflectTools {

    private static final Logger logger = LoggerFactory.getLogger(ReflectTools.class);

    private static final boolean IS_DEBUG_ENABLE = logger.isDebugEnabled();

    private static Map<String, MethodAccess> modelAccessCache = new HashMap<>();

    private static MethodAccess takeMethodAccessFromCache(Object obj){
        if (!modelAccessCache.containsKey(obj.getClass().getName())) {
            modelAccessCache.put(obj.getClass().getName(), MethodAccess.get(obj.getClass()));
        }
        return modelAccessCache.get(obj.getClass().getName());
    }

    public static Object invokeMethod(Object obj, String methodName){
        MethodAccess methodAccess = takeMethodAccessFromCache(obj);
        return methodAccess.invoke(obj, methodName);
    }

    /**
     * 通过Get或者Is方法获取对象值
     */
    public static Object getFieldValue(Object obj, String fieldName) throws NoSuchMethodException {
        MethodAccess methodAccess = takeMethodAccessFromCache(obj);
        int index = tryGetGetMethod(methodAccess, fieldName);  //尝试获取get方法
        if (index == -1) {
            index = tryGetIsMethod(methodAccess, fieldName); //尝试获取is方法
        }
        if (index == -1) {
            throw new NoSuchMethodException("can't not found the field : " + fieldName + " get or is method");
        }
        return methodAccess.invoke(obj, index);
    }

    /**
     * 通过Get或者Is方法获取对象值
     */
    public static Object getFieldValue(Object obj, Field field) throws NoSuchMethodException {
        return getFieldValue(obj, field.getName());
    }

    /**
     * 通过Set方法设置对象的值
     */
    public static boolean setFieldValue(Object obj, String fieldName, String fieldType, Object setValue) throws NoSuchMethodException, IllegalAccessException {
        MethodAccess methodAccess = takeMethodAccessFromCache(obj);
        int index = tryGetSetMethod(methodAccess, fieldName);
        if (index == -1) {
            throw new NoSuchMethodException("can't not found the field : " + fieldName + " Set method");
        }
        //setValue 不为null 且 field的Type 和 setValue 的Type不同的时候抛出异常
        if (setValue != null && !setValue.getClass().getName().toLowerCase().endsWith(fieldType.toLowerCase())) {
            throw new IllegalAccessException("The field type is : " + fieldType + " not match the value type : " + setValue.getClass().getName());
        }
        try {
            methodAccess.invoke(obj, index, setValue);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 通过Set方法设置对象的值
     */
    public static boolean setFieldValue(Object obj, Field field, Object setValue) throws NoSuchMethodException, IllegalAccessException {
        return setFieldValue(obj, field.getName(), field.getType().getName(), setValue);
    }

    /**
     * 把Map合并到Object中
     */
    public static void mergeMapToObject(Object object, Map<String, Object> map, String... ignoreField) {
        List<String> ignores = Arrays.asList(ignoreField);
        Stream.of(object.getClass().getDeclaredFields()).filter(field -> !ignores.contains(field.getName()))
                .forEach(field -> Optional.ofNullable(map.get(field.getName())).ifPresent(value -> {
                            try {
                                setFieldValue(object, field, value);
                            } catch (NoSuchMethodException | IllegalAccessException | NullPointerException e) {
                                logger.warn(e.getMessage());
                            }
                        }
                ));
    }

    public static void mergeObject(Object tag, Object source, boolean ignoreNull, String... ignoreField) {
        List<String> ignores = Arrays.asList(ignoreField);
        Stream.of(tag.getClass().getDeclaredFields()).filter(field -> !ignores.contains(field.getName()))//.parallel()
                .forEach(field -> {
                    try {
                        Optional.ofNullable(source.getClass().getDeclaredField(field.getName())).ifPresent(value -> {
                                    try {
                                        value.setAccessible(true);
                                        Object copyValue = value.get(source);
                                        if(!ignoreNull || null != copyValue){
                                            setFieldValue(tag, field, value.get(source));
                                        }
                                    } catch (NoSuchMethodException | IllegalAccessException | NullPointerException e) {
                                        if(IS_DEBUG_ENABLE) {
                                            logger.debug(e.getMessage());
                                        }
                                    }
                                }
                        );
                    } catch (NoSuchFieldException e) {
                        if(IS_DEBUG_ENABLE) {
                            logger.debug(e.getMessage());
                        }
                    }
                });
    }

    private static int tryGetIsMethod(MethodAccess methodAccess, String fieldName) {
        try {
            String methodName = "is" + toUpperCaseFirstOne(fieldName);
            return methodAccess.getIndex(methodName);
        } catch (Exception e) {
            return -1;
        }
    }

    private static int tryGetGetMethod(MethodAccess methodAccess, String fieldName) {
        try {
            String methodName = "get" + toUpperCaseFirstOne(fieldName);
            return methodAccess.getIndex(methodName);
        } catch (Exception e) {
            return -1;
        }
    }

    private static int tryGetSetMethod(MethodAccess methodAccess, String fieldName) {
        try {
            String methodName = "set" + toUpperCaseFirstOne(fieldName);
            return methodAccess.getIndex(methodName, 1);
        } catch (Exception e) {
            return -1;
        }
    }

    private static String toUpperCaseFirstOne(String str) {
        return str.toUpperCase().charAt(0) + str.substring(1);
    }
    private static String toLowerCaseFirstOne(String str) {
        return str.toLowerCase().charAt(0) + str.substring(1);
    }

    public static String parseMethodNameToFiledName(String methodName) throws NoSuchMethodException {
        if(methodName.startsWith("get") || methodName.startsWith("set")){
            return toLowerCaseFirstOne(methodName.substring(3));
        }else if (methodName.startsWith("is")){
            return toLowerCaseFirstOne(methodName.substring(2));
        }else{
            throw new NoSuchMethodException("This isn't a base bean , must start with [get, set, is]");
        }
    }
}
