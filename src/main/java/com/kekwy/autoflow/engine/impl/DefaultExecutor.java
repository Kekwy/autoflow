package com.kekwy.autoflow.engine.impl;

import com.kekwy.autoflow.engine.Executor;
import com.kekwy.autoflow.model.ContextModel;
import com.kekwy.autoflow.model.TaskModel;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

public class DefaultExecutor<O> implements Executor<O> {

    @Override
    public O execute(ContextModel<?> context, Collection<TaskModel<?>> taskList, TaskModel<O> outputTask, ExecutorService executorService) {
        return null;
    }
}
