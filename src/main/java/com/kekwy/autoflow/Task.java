package com.kekwy.autoflow;

public interface Task<T> {

    Result<T> result();

    interface Supplier<I, T> {
        Function<I, T> get();
    }

    interface Function<I, T> {
        T apply(Context<I> ctx);
    }

}
