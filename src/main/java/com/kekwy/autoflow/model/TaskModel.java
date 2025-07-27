package com.kekwy.autoflow.model;

import com.kekwy.autoflow.dsl.Result;
import com.kekwy.autoflow.dsl.Task;
import com.kekwy.autoflow.dsl.TaskFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TaskModel<T> {

    @Getter
    private final List<TaskModel<?>> dependencies = new ArrayList<>();

    @Getter
    private final List<TaskModel<?>> dependents = new ArrayList<>();

    @Getter
    private final Result<T> result = new Result<T>() {
    };

    @Getter
    private final String name;

    @Getter
    @Setter
    private TaskFunction<?, T> function;

    public void addDependent(TaskModel<?> dependency) {
        dependents.add(dependency);
        dependency.dependencies.add(this);
    }

}
