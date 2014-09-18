package com.cakemanny.app;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Daniel Golding dgolding@phlexglobal.com
 */
public class ResourceInstantiator {

    public static Object instantiate(Class<?> clazz, String value) {
        try {
            return castParameterDirectly(value, clazz);
        } catch (IllegalArgumentException e) {
            try {
                return instantiateStringArgConstructor(value, clazz);
            } catch (IllegalArgumentException e2) {
                e.addSuppressed(e2);
                throw e;
            }
        }
    }
    public static Object instantiate(String type, String value) {
        try {
            Class<?> clazz = forName(type);
            return instantiate(clazz, value);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(type + " is not a valid injection type", e);
        }
    }

    private static Class<?> forName(final String typeName) throws ClassNotFoundException {
        switch (typeName) {
            case "int":
                return Integer.TYPE;
            case "long":
                return Long.TYPE;
            case "double":
                return Double.TYPE;
            case "float":
                return Float.TYPE;
            case "boolean":
                return Boolean.TYPE;
            case "char":
                return Character.TYPE;
            case "byte":
                return Byte.TYPE;
            case "void":
                return Void.TYPE;
            case "short":
                return Short.TYPE;
            default:
                return Class.forName(typeName);
        }
    }

    /*
     * We want to be able to add a File instantiator
     */
    private static <T> Object instantiateStringArgConstructor(Object obj, Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor(String.class);
            constructor.setAccessible(true);
            return constructor.newInstance(obj);
        } catch (InvocationTargetException | IllegalAccessException e) {
            // Cannot deal with these
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Class " + clazz + " is not instatiatable", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class does not have single string constructor", e);
        }
    }

    /*
     * The following is borrowed from the library JUnitParams
     * http://code.google.com/p/junitparams/
     *
     * With minor adjustments
     * Licensed under the Apache 2.0 license, follow link
     * http://www.apache.org/licenses/LICENSE-2.0
     *
     * rawtypes warning is suppressed as Enum.valueOf does not allow
     * parameterised class literal
     */
    @SuppressWarnings("unchecked")
    private static <T> Object castParameterDirectly(Object object, @SuppressWarnings("rawtypes") Class clazz) {
        if (object == null || clazz.isInstance(object) || (!(object instanceof String) && clazz.isPrimitive()))
            return object;
        String strRep = (String) object;
        if (clazz.isEnum())
            return (Enum.valueOf(clazz, strRep));
        if (clazz.isAssignableFrom(String.class))
            return object.toString();
        if (clazz.isAssignableFrom(Integer.TYPE) || clazz.isAssignableFrom(Integer.class))
            return Integer.parseInt(strRep);
        if (clazz.isAssignableFrom(Short.TYPE) || clazz.isAssignableFrom(Short.class))
            return Short.parseShort(strRep);
        if (clazz.isAssignableFrom(Long.TYPE) || clazz.isAssignableFrom(Long.class))
            return Long.parseLong(strRep);
        if (clazz.isAssignableFrom(Float.TYPE) || clazz.isAssignableFrom(Float.class))
            return Float.parseFloat(strRep);
        if (clazz.isAssignableFrom(Double.TYPE) || clazz.isAssignableFrom(Double.class))
            return Double.parseDouble(strRep);
        if (clazz.isAssignableFrom(Boolean.TYPE) || clazz.isAssignableFrom(Boolean.class))
            return Boolean.parseBoolean(strRep);
        if (clazz.isAssignableFrom(Character.TYPE) || clazz.isAssignableFrom(Character.class))
            return object.toString().charAt(0);
        if (clazz.isAssignableFrom(Byte.TYPE) || clazz.isAssignableFrom(Byte.class))
            return Byte.parseByte(strRep);
        throw new IllegalArgumentException("Parameter type cannot be handled! Only primitive types and Strings can be used.");
    }

}


