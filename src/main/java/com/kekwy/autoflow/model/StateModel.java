package com.kekwy.autoflow.model;

import lombok.Data;

@Data
public class StateModel<I> {

    public enum Stage {
        DEFINE_FLOW,
        DEFINE_TASK,
        COMPLETE,
    }

    private Stage stage;
    private TaskModel<I, ?> task;

}
