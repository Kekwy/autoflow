package com.kekwy.autoflow;

public interface Context<I> {
    <T> T get(Input<T> input);

    <T> T get(Result<T> result);

    I input();
}
