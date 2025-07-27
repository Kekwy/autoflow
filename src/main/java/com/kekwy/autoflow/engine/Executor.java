package com.kekwy.autoflow.engine;

import com.kekwy.autoflow.dsl.Task;
import com.kekwy.autoflow.model.ContextModel;
import com.kekwy.autoflow.model.TaskModel;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

public interface Executor<O> {

    O execute(ContextModel<?> context, Collection<TaskModel<?>> taskList,
              TaskModel<O> outputTask, ExecutorService executorService);
}
