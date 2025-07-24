import com.kekwy.autoflow.DataFlow;
import com.kekwy.autoflow.Result;
import com.kekwy.autoflow.Task;
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

                flow.output(task3);
            });

    @Test
    public void test1() {

        TestInput input = new TestInput();

        Integer result = dataFlow.launch(input);
        System.out.println(result);

    }
}
