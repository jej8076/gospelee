//package util;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//
//public class FieldUtil<T> {
//
//    private static final String entityPackagePath = "com.gospelee.api.entity";
//
//    public static<T> Object copyFields(T source) throws IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
//        Class<?> sourceClass = source.getClass();
////        Class<?> destinationClass = destination.getClass();
//        String className = entityPackagePath + "." + sourceClass.getSimpleName().replace("Dto", "Factory");
//        Class<?> destinationClass = Class.forName(className);
//
//        Object instance = destinationClass.newInstance();
//
//        String methodName = "getPublicEntity";
////        Class[] parameterTypes = { String.class, int.class }; // 예시 매개변수 유형
//
//        // 메소드에 접근
//        Method method = destinationClass.getDeclaredMethod(methodName);
//        method.setAccessible(true); // 접근 권한 설정
//
//        Object entity = method.invoke(instance);
//
//        for (Field sourceField : sourceClass.getDeclaredFields()) {
//            try {
//                Field destinationField = destinationClass.getDeclaredField(sourceField.getName());
//                sourceField.setAccessible(true);
//                destinationField.setAccessible(true);
//                Object value = sourceField.get(source);
//                destinationField.set(entity, value);
//            } catch (NoSuchFieldException e) {
//               // TODO logger.debug 작성하여 DB 입력 안될 시 확인 가능하도록 해야함
//            }
//        }
//        return entity;
//    }
//}
