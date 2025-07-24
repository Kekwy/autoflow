package com.kekwy.autoflow;

import java.util.List;
import java.util.concurrent.ExecutorService;

public interface Executor<O> {

    O execute(RuntimeContext<?> context, List<Task<?>> taskList,
              Task<O> outputTask, ExecutorService executorService);
}
