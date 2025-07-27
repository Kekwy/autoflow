import com.kekwy.autoflow.DataFlow;
import com.kekwy.autoflow.dsl.Result;
import com.kekwy.autoflow.dsl.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DataFlowTest {

    private static class TestInput {
        public Integer inc;
        public Integer idx;
        public List<Integer> list;
    }

    // auto parallel
    private final DataFlow<TestInput, Integer> dataFlow =
            DataFlow.define("dataflow1", flow -> {
                Task<Integer> task1 = flow.task("task1", () ->
                        ctx -> {
                            int i = ctx.input().inc;
                            return i + 1;
                        }
                );

                Task<Integer> task2 = flow.task("task2", () ->
                        ctx -> {
                            int i = ctx.input().inc;
                            return i + 6;
                        }
                );

                Task<Integer> task3 = flow.task("task3", () -> {
                    Result<Integer> result1 = task1.result();
                    Result<Integer> result2 = task2.result();
                    return ctx -> {
                        Integer res1 = ctx.get(result1);
                        Integer res2 = ctx.get(result2);
                        Integer i = ctx.input().idx;
                        List<Integer> list = ctx.input().list;

                        return res1 + res2 + list.get(i);
                    };
                });

                Task<Integer> task4 = flow.task("task4", () -> {
                    task1.result();
                    task2.result();
                    return (ctx) -> 111;
                });

                Task<Integer> task5 = flow.task("task5", () ->
                        (ctx) -> 1
                );

                Task<Integer> task6 = flow.task("task6", () -> {
                    task3.result();
                    task4.result();
                    task5.result();
                    return (ctx) -> 1;
                });

                flow.output(task6);
            });

    @Test
    public void test1() {
        dataFlow.print();

        TestInput input = new TestInput();

        Integer result = dataFlow.launch(input);
        System.out.println(result);

    }
}
