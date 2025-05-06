package com.soprasteria.clinic.appointment.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Arrays;

public final class NullPropertyUtils {
    private NullPropertyUtils() { /* prevent instantiation */ }

    /**
     * Returns an array of property names whose values in the given object are null.
     */
    public static String[] getNullPropertyNames(Object source) {
        BeanWrapper src = new BeanWrapperImpl(source);
        return Arrays.stream(src.getPropertyDescriptors())
                .map(pd -> pd.getName())
                .filter(name -> src.getPropertyValue(name) == null)
                .toArray(String[]::new);
    }
}
