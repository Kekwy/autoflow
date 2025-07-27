package com.kekwy.autoflow;

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
import com.kekwy.autoflow.util.Printer;
import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataFlow<I, O> {

    @Getter
    private final String name;
    private final Collection<TaskModel<?>> tasks;
    private final TaskModel<O> outputTask;

    private DataFlow(String name, Collection<TaskModel<?>> tasks, TaskModel<O> outputTask) {
        this.name = name;
        this.tasks = tasks;
        this.outputTask = outputTask;
    }

    public O launch(I input) {
        return launch(input, Executors.newCachedThreadPool());
    }

    public O launch(I input, ExecutorService executorService) {
        ContextModel<I> context = new ContextModel<>(input);
        Executor<O> executor = new DefaultExecutor<>();
        return executor.execute(context, tasks, outputTask, executorService);
    }

    public void print() {
        System.out.println("================ tasks graph begin ================\n" +
                Printer.toPlantuml(outputTask) +
                "================ tasks graph end   ================\n"
        );
    }

    @FunctionalInterface
    public interface Supplier<I, O> {
        void accept(Flow<I, O> flow);
    }

    private static class FlowImpl<I, O> implements Flow<I, O> {

        private final String name;
        private final Map<Task<?>, TaskModel<?>> taskModelMap = new HashMap<>();
        private TaskModel<O> outputTask;
        private final StateModel state = new StateModel();

        public FlowImpl(String name) {
            this.name = name;
            state.setStage(StateModel.Stage.DEFINE_FLOW);
        }

        public DataFlow<I, O> get() {
            return new DataFlow<>(name, taskModelMap.values(), outputTask);
        }

        @Override
        public void output(Task<O> task) {
            try {
                //noinspection unchecked
                outputTask = (TaskModel<O>) Optional.ofNullable(taskModelMap.get(task))
                        .orElseThrow(() -> new AutoFlowException("No such task: " + task));
            } catch (ClassCastException e) {
                throw new AutoFlowException(e);
            }
        }

        @Override
        public <R> Task<R> task(String name, TaskFunction.Supplier<I, R> supplier) {
            TaskModel<R> taskModel = new TaskModel<>(name);

            state.setTask(taskModel);
            state.setStage(StateModel.Stage.DEFINE_TASK);

            TaskImpl<R> task = new TaskImpl<>(name, taskModel, state, supplier.get());

            state.setStage(StateModel.Stage.DEFINE_FLOW);
            taskModelMap.put(task, taskModel);
            return task;
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
                    this.taskModel.addDependent(state.getTask());
                }
            }
            return taskModel.getResult();
        }
    }

}



