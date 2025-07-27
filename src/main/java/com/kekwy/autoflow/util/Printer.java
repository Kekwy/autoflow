package com.kekwy.autoflow.util;

import com.kekwy.autoflow.model.TaskModel;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public class Printer {

    public static String toPlantuml(TaskModel<?, ?> outputTask) {
        StringBuilder builder = new StringBuilder();
        builder.append("@startuml\n");
        builder.append("left to right direction\n");
        Queue<TaskModel<?, ?>> bfsQueue = new LinkedList<>();
        bfsQueue.add(outputTask);
        Set<TaskModel<?, ?>> visited = new HashSet<>();
        visited.add(outputTask);
        builder.append("rectangle ")
                .append(outputTask.getName())
                .append("\n");
        while (!bfsQueue.isEmpty()) {
            int i = bfsQueue.size();
            for (int j = 0; j < i; j++) {
                TaskModel<?, ?> task = Optional.ofNullable(bfsQueue.poll()).orElseThrow(IllegalStateException::new);
                for (TaskModel<?, ?> dependency : task.getDependencies()) {
                    if (!visited.contains(dependency)) {
                        bfsQueue.add(dependency);
                        visited.add(dependency);
                        builder.append("rectangle ")
                                .append(dependency.getName())
                                .append("\n");
                    }
                    builder.append(task.getName())
                            .append(" --> ")
                            .append(dependency.getName())
                            .append("\n");

                }
            }
        }
        builder.append("@enduml\n");
        return builder.toString();
    }

}
