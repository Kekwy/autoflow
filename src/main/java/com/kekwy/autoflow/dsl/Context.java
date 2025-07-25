package com.kekwy.autoflow.dsl;

public interface Context<I> {

    <T> T get(Result<T> result);

    I input();

}
