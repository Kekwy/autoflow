package com.kekwy.autoflow;

public class DataFlow<I, O> {

    private final String name;

    public String getName() {
        return name;
    }

    private DataFlow(String name) {
        this.name = name;
    }

    public O launch(I input) {
        return null;
    }

    @FunctionalInterface
    public interface Supplier<I, O> {
        void accept(Flow<I, O> flow);
    }

    private static class FlowImpl<I, O> implements Flow<I, O> {

        private final DataFlow<I, O> dataFlow;

        public FlowImpl(String name) {
            dataFlow = new DataFlow<>(name);
        }

        public DataFlow<I, O> get() {
            return dataFlow;
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


}



