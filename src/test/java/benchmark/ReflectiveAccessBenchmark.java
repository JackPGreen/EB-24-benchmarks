package benchmark;

import org.openjdk.jmh.annotations.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/** {@link Benchmark}s different ways to access properties reflectively to compare performance */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
// For graphs, we want throughput results instead
//@BenchmarkMode(Mode.Throughput)
//@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class ReflectiveAccessBenchmark {
    private SomeSource source;
    private Method method;
    private MethodHandle methodHandle;

    @Setup
    public void setup() {
        source = new SomeSource();

        try {
            method = source.getClass().getDeclaredMethod("longMethod");
            methodHandle = MethodHandles.lookup().unreflect(method);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public long direct() throws Throwable {
        return source.longMethod();
    }

    @Benchmark
    public long reflection() throws ReflectiveOperationException {
        return ((Long) method.invoke(source)).longValue();
    }

    @Benchmark
    public long methodHandleInvoke() throws Throwable {
        return ((Long) methodHandle.invoke(source)).longValue();
    }

    @Benchmark
    public long methodHandleInvokeExact() throws Throwable {
        return (long) methodHandle.invokeExact(source);
    }

    /** Some dummy class to query metrics for using reflection */
    private class SomeSource {
        private static long counter = Long.MAX_VALUE;

        /** Not thread-safe but not relevant for our purposes */
        protected long longMethod() {
            return counter--;
        }
    }
}
