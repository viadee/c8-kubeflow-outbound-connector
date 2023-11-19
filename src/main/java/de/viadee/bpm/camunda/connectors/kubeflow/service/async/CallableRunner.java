package de.viadee.bpm.camunda.connectors.kubeflow.service.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CallableRunner {
    public static <T> T runCallableAfterDelay(Callable<T> task, long delay, TimeUnit timeUnit) throws InterruptedException, ExecutionException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        try {
            // Schedule the callable task to run after the specified delay
            ScheduledFuture<T> future = scheduler.schedule(task, delay, timeUnit);

            // Wait for the callable to finish and retrieve the result
            T result = future.get(); // This blocks until the result is available

            // Use the result
            return result;
        } finally {
            scheduler.shutdown(); // It's important to shut down the executor service to avoid resource leaks
        }
    }
}
