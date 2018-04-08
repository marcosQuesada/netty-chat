package com.marcosquesada.netty.chat.executor;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorTest {

    @Test
    public void ScheduleTaskOnExecutorAndCheckIsDone() {
        AtomicInteger counter = new AtomicInteger();

        Scheduler e = new Scheduler("test", 1);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                counter.incrementAndGet();
            }
        };
        ScheduledFuture<?> future =  e.schedule(task, 10L);
        while (!future.isDone()){
            try {
                Thread.sleep(100L);
            }catch (Exception ex) {
                Assert.fail();
            }
        }

        Assert.assertEquals(counter.get(), 1);
    }
}
