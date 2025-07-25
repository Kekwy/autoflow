package com.kekwy.autoflow.model;

import lombok.Data;

@Data
public class StateModel {

    public enum Stage {
        DEFINE_FLOW,
        DEFINE_TASK,
        RUNTIME,
    }

    private Stage stage;
    private TaskModel<?> task;

}
