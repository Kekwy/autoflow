package com.kekwy.autoflow.engine.impl;

import com.kekwy.autoflow.dsl.Context;
import com.kekwy.autoflow.dsl.Result;
import com.kekwy.autoflow.engine.Executor;
import com.kekwy.autoflow.exception.AutoFlowException;
import com.kekwy.autoflow.model.ContextModel;
import com.kekwy.autoflow.model.TaskModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class DefaultExecutor<I, O> implements Executor<I, O> {

    @Override
    public O execute(ContextModel<I> contextModel, TaskModel<I, O> outputTask, ExecutorService executorService) {
        Context<I> context = new ContextImpl<>(contextModel);

        CompletionService<ResultDTO<I>> completionService = new ExecutorCompletionService<>(executorService);

        // build outdegreeMap
        Map<TaskModel<I, ?>, Integer> outdegreeMap = new HashMap<>();
        outdegreeMap.put(outputTask, outputTask.getDependencies().size());
        Queue<TaskModel<I, ?>> bfsQueue = new LinkedList<>();
        bfsQueue.add(outputTask);

        while (!bfsQueue.isEmpty()) {
            TaskModel<I, ?> task = Optional.ofNullable(bfsQueue.poll()).orElseThrow(IllegalStateException::new);
            for (TaskModel<I, ?> dependency : task.getDependencies()) {
                if (!outdegreeMap.containsKey(dependency)) {
                    outdegreeMap.put(dependency, dependency.getDependencies().size());
                    bfsQueue.add(dependency);
                }
            }
        }

        List<TaskModel<I, ?>> executableTasks = getExecutableTasks(outdegreeMap);

        executableTasks.forEach(task ->
                completionService.submit(() -> this.run(task, context))
        );

        int i = executableTasks.size();
        while (i > 0) {
            TaskModel<I, ?> task;
            Object data;
            try {
                Future<ResultDTO<I>> completedFuture = completionService.take();
                ResultDTO<I> resultDTO = completedFuture.get();
                task = resultDTO.getTask();
                data = resultDTO.getData();
            } catch (InterruptedException | ExecutionException e) {
                throw new AutoFlowException(e);
            }

            contextModel.set(task.getResult(), data);
            i--;

            if (task != outputTask) {
                for (TaskModel<I, ?> dependent : task.getDependents()) {
                    Integer outdegree = outdegreeMap.get(dependent);
                    outdegree--;
                    outdegreeMap.put(dependent, outdegree);
                    if (outdegree == 0) {
                        completionService.submit(() -> run(dependent, context));
                        i++;
                    }
                }
            }
        }

        return contextModel.getResult(outputTask.getResult());
    }

    private List<TaskModel<I, ?>> getExecutableTasks(Map<TaskModel<I, ?>, Integer> outdegreeMap) {
        return outdegreeMap.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    private static class ResultDTO<I> {
        private TaskModel<I, ?> task;
        private Object data;
    }

    private ResultDTO<I> run(TaskModel<I, ?> task, Context<I> context) {
        Object o = task.getFunction().apply(context);
        return new ResultDTO<>(task, o);
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
