package com.kekwy.autoflow;

public interface Context<I> {

    <T> T get(Result<T> result);

    I input();

}
