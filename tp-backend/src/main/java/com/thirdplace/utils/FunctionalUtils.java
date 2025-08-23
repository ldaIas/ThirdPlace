package com.thirdplace.utils;

public class FunctionalUtils {
    
    @FunctionalInterface
    public interface ErrorableSupplier<T> {
        T get() throws Exception;
    }

}
