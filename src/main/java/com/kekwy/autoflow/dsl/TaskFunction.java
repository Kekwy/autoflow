package com.kekwy.autoflow.dsl;

public interface TaskFunction<I, T> {

    T apply(Context<I> ctx);

    interface Supplier<I, T> {
        TaskFunction<I, T> get();
    }
}
