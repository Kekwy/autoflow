package com.kekwy.autoflow.engine.impl;

import com.kekwy.autoflow.dsl.Task;
import com.kekwy.autoflow.engine.Executor;
import com.kekwy.autoflow.model.ContextModel;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class DefaultExecutor<O> implements Executor<O> {
    @Override
    public O execute(ContextModel<?> context, List<Task<?>> taskList, Task<O> outputTask, ExecutorService executorService) {
        return null;
    }
}
