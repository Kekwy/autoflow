package com.kekwy.autoflow.dsl;

import java.util.HashMap;

public interface Task<T> {

    Result<T> result();

    interface Supplier<I, T> {
        TaskFunction<I, T> get();
    }

}
