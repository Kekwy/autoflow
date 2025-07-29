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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataFlow<I, O> {

    private static final String NOTICE = "Results are only available when defining task, not during the stage.";

    @Getter
    private final String name;
    private final TaskModel<I, O> outputTask;

    private DataFlow(String name, TaskModel<I, O> outputTask) {
        this.name = name;
        this.outputTask = outputTask;
    }

    public O launch(I input) {
        return launch(input, Executors.newCachedThreadPool());
    }

    public O launch(I input, ExecutorService executorService) {
        ContextModel<I> context = new ContextModel<>(input);
        Executor<I, O> executor = new DefaultExecutor<>();
        return executor.execute(context, outputTask, executorService);
    }

    public String toPlantuml() {
        return "================ tasks graph begin ================\n" +
                Printer.toPlantuml(outputTask) +
                "================ tasks graph end   ================\n";
    }

    @FunctionalInterface
    public interface Supplier<I, O> {
        void accept(Flow<I, O> flow);
    }

    private static class FlowImpl<I, O> implements Flow<I, O> {

        private final String name;
        private final Map<Task<?>, TaskModel<I, ?>> taskModelMap = new HashMap<>();
        private TaskModel<I, O> outputTask;
        private final StateModel<I> state = new StateModel<>();

        public FlowImpl(String name) {
            this.name = name;
            state.setStage(StateModel.Stage.DEFINE_FLOW);
        }

        public DataFlow<I, O> get() {
            if (state.getStage() != StateModel.Stage.DEFINE_FLOW) {
                throw new AutoFlowException(NOTICE);
            }
            state.setStage(StateModel.Stage.COMPLETE);
            return new DataFlow<>(name, outputTask);
        }

        @Override
        public void output(Task<O> task) {
            if (state.getStage() != StateModel.Stage.DEFINE_FLOW) {
                throw new AutoFlowException(NOTICE);
            }
            try {
                //noinspection unchecked
                outputTask = (TaskModel<I, O>) Optional.ofNullable(taskModelMap.get(task))
                        .orElseThrow(() -> new AutoFlowException("No such task: " + task));
            } catch (ClassCastException e) {
                throw new AutoFlowException(e);
            }
        }

        @Override
        public <R> Task<R> task(String name, TaskFunction.Supplier<I, R> supplier) {
            if (state.getStage() != StateModel.Stage.DEFINE_FLOW) {
                throw new AutoFlowException(NOTICE);
            }
            TaskModel<I, R> taskModel = new TaskModel<>(name);

            state.setTask(taskModel);
            state.setStage(StateModel.Stage.DEFINE_TASK);

            TaskImpl<I, R> task = new TaskImpl<>(name, taskModel, state, supplier.get());

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

    private static class TaskImpl<I, R> implements Task<R> {

        @Getter
        private final String name;
        private final TaskModel<I, R> taskModel;
        private final StateModel<I> state;

        public TaskImpl(String name, TaskModel<I, R> taskModel, StateModel<I> state, TaskFunction<I, R> function) {
            this.name = name;
            this.taskModel = taskModel;
            this.state = state;
            taskModel.setFunction(function);
        }


        @Override
        public Result<R> result() {
            if (state.getStage() != StateModel.Stage.DEFINE_TASK) {
                throw new AutoFlowException(NOTICE);
            }
            // process dependencies
            this.taskModel.addDependent(state.getTask());
            return taskModel.getResult();
        }
    }

}



