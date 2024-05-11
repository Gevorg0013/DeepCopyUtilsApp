/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package com.mycompany.deepcopyutilsApp;

/**
 *
 * @author user
 */
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.IdentityHashMap;
import java.util.Map;
import lombok.Data;

public class DeepCopyUtilsApp {

    private static Map<Object, Object> copiedObjects = new IdentityHashMap<>();

    public static <T> T deepCopy(T original) {
        if (original == null) {
            return null;
        }

        // Handle cyclic references
        if (copiedObjects.containsKey(original)) {
            // If the object has already been copied, return the copied instance
            return (T) copiedObjects.get(original);
        }

        // Mark the original object as copied to handle cyclic references
        copiedObjects.put(original, null);

        try {
            Class<?> clazz = original.getClass();
            if (clazz.isArray()) {
                int length = Array.getLength(original);
                Class<?> componentType = clazz.getComponentType();
                Object newArray = Array.newInstance(componentType, length);
                copiedObjects.put(original, newArray); // Update copiedObjects map with new array reference
                for (int i = 0; i < length; i++) {
                    Object arrayItem = Array.get(original, i);
                    Object copiedItem = deepCopy(arrayItem);
                    Array.set(newArray, i, copiedItem);
                }
                return (T) newArray;
            } else if (clazz.equals(String.class) || clazz.isPrimitive() || clazz.getName().startsWith("java.")) {
                // For String and primitive types, return the original object
                return original;
            } else {
                T newInstance = (T) clazz.getDeclaredConstructor().newInstance();
                copiedObjects.put(original, newInstance); // Update copiedObjects map with new instance reference
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (!Modifier.isStatic(field.getModifiers())) {
                        Object fieldValue = field.get(original);
                        Object copiedValue = deepCopy(fieldValue);
                        field.set(newInstance, copiedValue);
                    }
                }
                return newInstance;
            }
        } catch (Exception e) {
            // Remove original object from copiedObjects map if an exception occurs
            copiedObjects.remove(original);
            System.err.println("Error deep copying object of class: " + original.getClass().getName());
            e.printStackTrace();
            throw new RuntimeException("Could not deep copy object", e);
        }
    }

    public static void main(String[] args) {
        System.out.println("started001");

        // Example usage with a complex object
        ComplexObject original = new ComplexObject();
        ComplexObject copy = deepCopy(original);
        System.out.println("original " + original.getData());
        System.out.println("copy" + copy.getData()); //we added Data anotation so we can use get methods for every fields
    }

    @Data
    static class ComplexObject {

        private int data = 123;
        private Integer[] array = new Integer[]{1, 2, 3};
        private List<Integer> list = new ArrayList<>(Arrays.asList(4, 5, 6));
        private ComplexObject selfReference;

        public ComplexObject() {
            selfReference = this; // Recursive structure example
        }

        @Override
        public String toString() {
            return "ComplexObject{"
                    + "data=" + data
                    + ", array=" + Arrays.toString(array)
                    + ", list=" + list
                    + '}';
        }
    }
}
