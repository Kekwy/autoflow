package com.kekwy.autoflow.model;

import com.kekwy.autoflow.dsl.Result;
import com.kekwy.autoflow.dsl.TaskFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TaskModel<I, R> {

    @Getter
    private final List<TaskModel<I, ?>> dependencies = new ArrayList<>();

    @Getter
    private final List<TaskModel<I, ?>> dependents = new ArrayList<>();

    @Getter
    private final Result<R> result = new Result<R>() {
    };

    @Getter
    private final String name;

    @Getter
    @Setter
    private TaskFunction<I, R> function;

    public void addDependent(TaskModel<I, ?> dependency) {
        dependents.add(dependency);
        dependency.dependencies.add(this);
    }

}
