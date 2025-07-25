package com.kekwy.autoflow;

import com.kekwy.autoflow.dsl.Context;
import com.kekwy.autoflow.dsl.Flow;
import com.kekwy.autoflow.dsl.Result;
import com.kekwy.autoflow.dsl.Task;
import com.kekwy.autoflow.dsl.TaskFunction;
import com.kekwy.autoflow.engine.Executor;
import com.kekwy.autoflow.engine.impl.DefaultExecutor;
import com.kekwy.autoflow.exception.AutoFlowException;
import com.kekwy.autoflow.model.ContextModel;
import com.kekwy.autoflow.model.StateModel;
import com.kekwy.autoflow.model.TaskModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataFlow<I, O> {

    @Getter
    private final String name;
    private final List<Task<?>> taskList;
    private final Task<O> outputTask;

    private DataFlow(String name, List<Task<?>> taskList, Task<O> outputTask) {
        this.name = name;
        this.taskList = taskList;
        this.outputTask = outputTask;
    }

    public O launch(I input) {
        return launch(input, Executors.newCachedThreadPool());
    }

    public O launch(I input, ExecutorService executorService) {
        ContextModel<I> context = new ContextModel<>(input);
        Executor<O> executor = new DefaultExecutor<>();
        return executor.execute(context, taskList, outputTask, executorService);
    }

    @FunctionalInterface
    public interface Supplier<I, O> {
        void accept(Flow<I, O> flow);
    }

    private static class FlowImpl<I, O> implements Flow<I, O> {

        //        private final DataFlow<I, O> dataFlow;
        private final StateModel state;

        public FlowImpl(String name) {
//            dataFlow = new DataFlow<>(name);
            this.state = new StateModel();
            state.setStage(StateModel.Stage.DEFINE_FLOW);
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
            TaskModel<R> taskModel = new TaskModel<>(name, state);

            state.setStage(StateModel.Stage.DEFINE_TASK);
            state.setTask(taskModel);

            new TaskImpl<>(name, taskModel, state, supplier.get());
            return null;
        }

    }

    public static <I, O> DataFlow<I, O> define(String name, Supplier<I, O> supplier) {
        Flow<I, O> flow = new FlowImpl<>(name);
        supplier.accept(flow);
        return flow.get();
    }

    private static class TaskImpl<T> implements Task<T> {

        @Getter
        private final String name;
        private final TaskModel<T> taskModel;
        private final StateModel state;

        public TaskImpl(String name, TaskModel<T> taskModel, StateModel state, TaskFunction<?, T> function) {
            this.name = name;
            this.taskModel = taskModel;
            this.state = state;
            taskModel.setFunction(function);
        }

        private static final String NOTICE = "Results are only available when defining task, not during the stage.";

        @Override
        public Result<T> result() {
            switch (state.getStage()) {
                case DEFINE_FLOW:
                case RUNTIME:
                    throw new AutoFlowException(NOTICE);
                case DEFINE_TASK: {
                    // process dependencies
                    this.taskModel.addDependency(state.getTask());
                }
            }
            return taskModel.getResult();
        }
    }

}



