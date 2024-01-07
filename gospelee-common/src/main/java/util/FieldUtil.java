package util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FieldUtil {

    private static final String ENTITY_PACKAGE_PATH = "com.gospelee.api.entity";

    private static final String FACTORY_GET_ENTITY_METHOD_NAME = "getPrivateEntity";

    public static<T> Object toEntity(T source) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> sourceClass = source.getClass();

        String className = ENTITY_PACKAGE_PATH + "." + sourceClass.getSimpleName().replace("Vo", "Factory");
        String entityName = ENTITY_PACKAGE_PATH + "." + sourceClass.getSimpleName().replace("Vo", "");

        /* PROTECTED 된 entity에 대해 factoryClass를 사용하여 우회하여 entity를 가져온다 */
        Class<?> factoryClass = Class.forName(className);
        Class<?> destinationClass = Class.forName(entityName);

        Method method = factoryClass.getDeclaredMethod(FACTORY_GET_ENTITY_METHOD_NAME);
        method.setAccessible(true);

        // newInstance를 하기 위해 factory class로 우회하였다
        Object instance = factoryClass.newInstance();
        Object entity = method.invoke(instance);

        for (Field sourceField : sourceClass.getDeclaredFields()) {
            try {
                Field destinationField = destinationClass.getDeclaredField(sourceField.getName());
                sourceField.setAccessible(true);
                destinationField.setAccessible(true);

                Object value = sourceField.get(source);
                destinationField.set(entity, value);
            } catch (NoSuchFieldException e) {
                // TODO logger.debug 작성하여 DB 입력 안될 시 확인 가능하도록 해야함(필드명이 같아야 입력됨)
            }
        }
        return entity;
    }
}
