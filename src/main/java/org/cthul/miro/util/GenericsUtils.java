package org.cthul.miro.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class GenericsUtils {
    
    public static Class<?> returnType(Class<?> clazz, String method, Class<?>... args) {
        try {
            Method m = clazz.getMethod(method, args);
            Type ret = m.getGenericReturnType();
            return asClass(ret, clazz, m.getDeclaringClass());
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> asClass(Type type, Class<?> actual, Class<?> declaring) {
        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
        }
        if (type instanceof Class) {
            return (Class) type;
        }
        type = lookUpTypeVar((TypeVariable) type, actual, declaring);
        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
        }
        return (Class) type;
    }
    
    private static Type lookUpTypeVar(TypeVariable var, Class<?> actual, Class<?> declaring) {
        if (actual.equals(declaring)) {
            return var;
        }
        Type type = null;
        ParameterizedType sup = null;
        Class supClass = null;
        for (Type iface: actual.getGenericInterfaces()) {
            if (!(iface instanceof ParameterizedType)) continue;
            ParameterizedType pIface = (ParameterizedType) iface;
            supClass = (Class) pIface.getRawType();
            if (declaring.isAssignableFrom(supClass)) {
                sup = pIface;
                type = lookUpTypeVar(var, supClass, declaring);
                break;
            }
        }
        if (type == null || sup == null) {
            throw new AssertionError(var);
        }
        if (type instanceof Class || type instanceof ParameterizedType) {
            return type;
        }
        var = (TypeVariable) type;
        TypeVariable[] params = supClass.getTypeParameters();
        for (int i = 0; i < params.length; i++) {
            if (params[i].getName().equals(var.getName())) {
                return sup.getActualTypeArguments()[i];
            }
        }
        return null;
    }
    
    
//    private static Type lookUpTypeVariable(Type type, Class<?> actualClass, Class<?> declaringClass) {
//        if (actualClass.equals(declaringClass)) {
//            for (TypeVariable<?> tv: actualClass.getTypeParameters()) {
//                //if (tv.getName().equals(type.))
//            }
//            throw new AssertionError(type);
//        }
//        int index = -1;
//         else {
//            for (Class<?> iface: actualClass.getInterfaces()) {
//                if (declaringClass.isAssignableFrom(iface)) {
//                    index = lookUpTypeVariable(type, iface, declaringClass);
//                    break;
//                }
//            }
//            return index;
//        }
//    }
    
}
