package com.kekwy.autoflow.model;

import com.kekwy.autoflow.dsl.Result;
import com.kekwy.autoflow.exception.AutoFlowException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ContextModel<I> {

    private final Map<Result<?>, Object> resultMap = new HashMap<>();
    @Getter
    private final I input;

    public <T> void set(Result<T> result, Object data) {
        resultMap.put(result, data);
    }

    public <T> T getResult(Result<T> result) {
        try {
            //noinspection unchecked
            return (T) resultMap.get(result);
        } catch (ClassCastException e) {
            throw new AutoFlowException(e);
        }
    }


}
