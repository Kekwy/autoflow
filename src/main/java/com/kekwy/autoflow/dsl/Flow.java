package com.kekwy.autoflow.dsl;

import com.kekwy.autoflow.DataFlow;

public interface Flow<I, O> {

    <R> Task<R> task(String name, Task.Supplier<I, R> supplier);

    void output(Task<O> task);

    DataFlow<I, O> get();

}
