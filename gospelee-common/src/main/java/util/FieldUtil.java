package util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
import org.springframework.util.ObjectUtils;

public class FieldUtil {

  private static final String ENTITY_PACKAGE_PATH = "com.gospelee.api.entity";

  private static final String FACTORY_GET_ENTITY_METHOD_NAME = "getPrivateEntity";

  @SneakyThrows
  public static <T> Object toEntity(T source) {
    Class<?> sourceClass = source.getClass();

    String className =
        ENTITY_PACKAGE_PATH + "." + sourceClass.getSimpleName().replace("DTO", "Factory");
    String entityName = ENTITY_PACKAGE_PATH + "." + sourceClass.getSimpleName().replace("DTO", "");

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
        if (sourceField.getType().getName().endsWith("DTO")) {
          // field가 DTO로 끝나는 경우
          sourceFieldName = sourceFieldName.replace("DTO", "");
        }

        Field destinationField = destinationClass.getDeclaredField(sourceFieldName);
        sourceField.setAccessible(true);
        destinationField.setAccessible(true);

        Object value = sourceField.get(source);
        if (ObjectUtils.isEmpty(value)) {
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
            throw new IllegalArgumentException(
                "Failed to convert value for field: " + sourceFieldName, e);
          }
        }
      } catch (NoSuchFieldException e) {
        // TODO logger.debug 작성하여 DB 입력 안될 시 확인 가능하도록 해야함(필드명이 같아야 입력됨)
      }
    }
    return entity;
  }

  // 형변환을 시도하는 메서드
  @SneakyThrows
  private static Object convertValue(Object source, Class<?> targetType) {
    Class<?> sourceClass = source.getClass();

    String className =
        ENTITY_PACKAGE_PATH + "." + sourceClass.getSimpleName().replace("DTO", "Factory");
    String entityName = ENTITY_PACKAGE_PATH + "." + sourceClass.getSimpleName().replace("DTO", "");

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
        if (sourceField.getType().getName().endsWith("DTO")) {
          // field가 DTO로 끝나는 경우
          sourceFieldName = sourceFieldName.replace("DTO", "");
        }

        Field destinationField = destinationClass.getDeclaredField(sourceFieldName);
        sourceField.setAccessible(true);
        destinationField.setAccessible(true);

        Object value = sourceField.get(source);
        if (ObjectUtils.isEmpty(value)) {
          continue;
        }

        Class<?> destinationFieldType = destinationField.getType();
        if (destinationFieldType.isAssignableFrom(value.getClass())) {
          // 값의 타입이 호환되면 그대로 설정
          destinationField.set(entity, value);
        } else {
          // 값의 타입이 호환되지 않는 경우 형변환 시도
          try {
            // 본인을 호출하는 부분이므로 무한히 들어갈 가능성이 있음
            Object convertedValue = convertValue(value, destinationFieldType);
            destinationField.set(entity, convertedValue);
          } catch (Exception e) {
            // 형변환이 실패하면 예외 처리
            throw new IllegalArgumentException(
                "Failed to convert value for field: " + sourceFieldName, e);
          }
        }
      } catch (NoSuchFieldException e) {
        // TODO logger.debug 작성하여 DB 입력 안될 시 확인 가능하도록 해야함(필드명이 같아야 입력됨)
      }
    }
    return entity;
  }
}
