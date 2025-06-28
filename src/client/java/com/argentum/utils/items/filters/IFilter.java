package com.argentum.utils.items.filters;

public interface IFilter <T> {
    boolean isValid(T item);
}
