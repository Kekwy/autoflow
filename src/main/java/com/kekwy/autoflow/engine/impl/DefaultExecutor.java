package com.kekwy.autoflow.engine.impl;

import com.kekwy.autoflow.dsl.Context;
import com.kekwy.autoflow.dsl.Result;
import com.kekwy.autoflow.engine.Executor;
import com.kekwy.autoflow.model.ContextModel;
import com.kekwy.autoflow.model.TaskModel;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class DefaultExecutor<I, O> implements Executor<I, O> {

    // TODO: move into context.
    private Collection<TaskModel<I, ?>> taskList;
    private TaskModel<I, O> outputTask;
    private ExecutorService executorService;
    private boolean complete = false;
    private final Object contextLock = new Object();
    private final Object indegreeMapLock = new Object();
    private Context<I> context;
    private ContextModel<I> contextModel;

    @Override
    public O execute(ContextModel<I> contextModel, Collection<TaskModel<I, ?>> taskList,
                     TaskModel<I, O> outputTask, ExecutorService executorService) {
        this.context = new ContextImpl<>(contextModel);
        this.contextModel = contextModel;
        this.taskList = taskList;
        this.outputTask = outputTask;
        this.executorService = executorService;
        Map<TaskModel<I, ?>, Integer> outdegreeMap = new HashMap<>();
        outdegreeMap.put(outputTask, outputTask.getDependents().size());
        Queue<TaskModel<I, ?>> bfsQueue = new LinkedList<>();
        bfsQueue.add(outputTask);
        while (!bfsQueue.isEmpty()) {
            TaskModel<I, ?> task = Optional.ofNullable(bfsQueue.poll()).orElseThrow(IllegalStateException::new);
            for (TaskModel<I, ?> dependency : task.getDependencies()) {
                if (!outdegreeMap.containsKey(dependency)) {
                    outdegreeMap.put(dependency, dependency.getDependents().size());
                }
            }
        }
        List<TaskModel<I, ?>> executableTasks = getExecutableTasks(outdegreeMap);
        executableTasks.forEach(task ->
                executorService.submit(() -> this.run(task, outdegreeMap))
        );
        O result;
        synchronized (contextLock) {
            if (!complete) {
                try {
                    contextLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (complete) {
                result = contextModel.getResult(outputTask.getResult());
            } else {
                throw new IllegalStateException();
            }
        }
        return result;
    }

    private List<TaskModel<I, ?>> getExecutableTasks(Map<TaskModel<I, ?>, Integer> outdegreeMap) {
        return outdegreeMap.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void run(TaskModel<I, ?> task, Map<TaskModel<I, ?>, Integer> indegreeMap) {
        Object o = task.getFunction().apply(context);
        synchronized (contextLock) {
            contextModel.set(task.getResult(), o);
        }
        if (task == outputTask) {
            complete = true;
            contextLock.notify();
            return;
        }
        synchronized (indegreeMapLock) {
            for (TaskModel<I, ?> dependent : task.getDependents()) {
                Integer outdegree = indegreeMap.get(dependent);
                outdegree--;
                indegreeMap.put(dependent, outdegree);
                if (outdegree == 0) {
                    executorService.submit(() -> run(dependent, indegreeMap));
                }
            }
        }

    }

    @RequiredArgsConstructor
    private static class ContextImpl<O> implements Context<O> {

        private final ContextModel<O> model;

        @Override
        public <T> T get(Result<T> result) {
            return model.getResult(result);
        }

        @Override
        public O input() {
            return model.getInput();
        }
    }

}
