package util;

import org.springframework.util.ObjectUtils;

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

                String sourceFieldName = sourceField.getName();
                if(sourceField.getType().getName().endsWith("Vo")){
                    // field가 Vo로 끝나는 경우
                    sourceFieldName = sourceFieldName.replace("Vo", "");
                }

                Field destinationField = destinationClass.getDeclaredField(sourceFieldName);
                sourceField.setAccessible(true);
                destinationField.setAccessible(true);

                Object value = sourceField.get(source);
                if(ObjectUtils.isEmpty(value)){
                    continue;
                }

                Class<?> destinationFieldType = destinationField.getType();
                if (destinationFieldType.isAssignableFrom(value.getClass())) {
                    // 값의 타입이 호환되면 그대로 설정
                    destinationField.set(entity, value);
                } else {
                    // 값의 타입이 호환되지 않는 경우 형변환 시도
                    try {
                        Object convertedValue = convertValue(value, destinationFieldType);
                        destinationField.set(entity, convertedValue);
                    } catch (Exception e) {
                        // 형변환이 실패하면 예외 처리
                        throw new IllegalArgumentException("Failed to convert value for field: " + sourceFieldName, e);
                    }
                }

//                destinationField.set(entity, value);
            } catch (NoSuchFieldException e) {
                // TODO logger.debug 작성하여 DB 입력 안될 시 확인 가능하도록 해야함(필드명이 같아야 입력됨)
            }
        }
        return entity;
    }

    // 형변환을 시도하는 메서드
    private static Object convertValue(Object value, Class<?> targetType) {
        // 여기에서 원하는 형변환 로직을 구현
        // 예: String을 int로 변환하는 경우
        if (targetType == Integer.class) {
            return Integer.parseInt((String) value);
        }
        // 기타 타입에 대한 형변환 로직 추가

        // 타입이 호환되지 않으면 null 반환
        return null;
    }
}
