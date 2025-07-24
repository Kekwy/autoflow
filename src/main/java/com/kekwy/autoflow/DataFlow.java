package com.kekwy.autoflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataFlow<I, O> {

    private final String name;
    private final List<Task<?>> taskList;
    private final Task<O> outputTask;
    private final Executor<O> executor;

    public String getName() {
        return name;
    }

    private DataFlow(String name, List<Task<?>> taskList, Task<O> outputTask) {
        this.name = name;
        this.taskList = taskList;
        this.outputTask = outputTask;
        this.executor = new ExecutorImpl<>();
    }

    public O launch(I input) {
        return launch(input, Executors.newCachedThreadPool());
    }

    public O launch(I input, ExecutorService executorService) {
        RuntimeContext<I> context = new RuntimeContextImpl<>(input, taskList);
        return executor.execute(context, taskList, outputTask, executorService);
    }

    @FunctionalInterface
    public interface Supplier<I, O> {
        void accept(Flow<I, O> flow);
    }


    private static class FlowImpl<I, O> implements Flow<I, O> {

//        private final DataFlow<I, O> dataFlow;

        public FlowImpl(String name) {
//            dataFlow = new DataFlow<>(name);
        }

        public DataFlow<I, O> get() {
//            return dataFlow;
            return null;
        }

        @Override
        public void output(Task<O> task) {

        }

        @Override
        public <R> Task<R> task(String name, Task.Supplier<I, R> supplier) {
            return null;
        }

    }

    public static <I, O> DataFlow<I, O> define(String name, Supplier<I, O> supplier) {
        Flow<I, O> flow = new FlowImpl<>(name);
        supplier.accept(flow);
        return flow.get();
    }


    private static class RuntimeContextImpl<I> implements RuntimeContext<I> {

        private final I input;
        private final Map<Result<?>, Object> resultMap = new HashMap<>();

        public RuntimeContextImpl(I input, List<Task<?>> taskList) {
            this.input = input;

        }

        @Override
        public <T> void set(Result<T> result, T data) {
            resultMap.put(result, data);
        }

        @Override
        public <T> T get(Result<T> result) {
            try {
                //noinspection unchecked
                return (T) resultMap.get(result);
            } catch (ClassCastException e) {
                throw new AutoFlowException(e);
            }
        }

        @Override
        public I input() {
            return input;
        }

    }


    private static class ExecutorImpl<O> implements Executor<O> {

        @Override
        public O execute(RuntimeContext<?> context, List<Task<?>> taskList,
                         Task<O> outputTask, ExecutorService executorService) {
            return null;
        }
    }

}



