package com.kekwy.autoflow;

public interface RuntimeContext<I> extends Context<I> {
    <T> void set(Result<T> result, T data);
}
