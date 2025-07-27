package com.kekwy.autoflow.engine;

import com.kekwy.autoflow.model.ContextModel;
import com.kekwy.autoflow.model.TaskModel;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

public interface Executor<I, O> {

    O execute(ContextModel<I> context, Collection<TaskModel<I, ?>> taskList,
              TaskModel<I, O> outputTask, ExecutorService executorService);
}
