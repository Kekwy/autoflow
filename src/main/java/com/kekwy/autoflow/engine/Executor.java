package com.kekwy.autoflow.engine;

import com.kekwy.autoflow.dsl.Task;
import com.kekwy.autoflow.model.ContextModel;

import java.util.List;
import java.util.concurrent.ExecutorService;

public interface Executor<O> {

    O execute(ContextModel<?> context, List<Task<?>> taskList,
              Task<O> outputTask, ExecutorService executorService);
}
