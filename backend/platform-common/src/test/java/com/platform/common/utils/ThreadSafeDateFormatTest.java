package com.platform.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** 日期格式器并发回归, 不依赖数据库或外部服务. */
class ThreadSafeDateFormatTest {
    /** 多线程同时格式化不同日期时不得互相覆盖结果. */
    @Test
    void formatsDifferentDatesConcurrently() throws Exception {
        ThreadSafeDateFormat formatter = new ThreadSafeDateFormat("yyyy-MM-dd");
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int index = 0; index < 400; index++) {
                LocalDate date = LocalDate.of(2021, 1, 1).plusDays(index % 28);
                Date value = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                String expected = date.toString();
                tasks.add(
                        () -> {
                            String actual = formatter.format(value);
                            assertEquals(expected, actual);
                            return actual;
                        });
            }
            List<Future<String>> results = pool.invokeAll(tasks);
            assertEquals(400, results.size());
            for (Future<String> result : results) {
                result.get(10, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    /** 复制格式器不共享线程内实例, 仍保持相同时区配置. */
    @Test
    void copiesTimezoneWithoutSharingFormatter() {
        ThreadSafeDateFormat formatter = new ThreadSafeDateFormat("yyyy-MM-dd HH:mm:ss");
        ThreadSafeDateFormat copy = (ThreadSafeDateFormat) formatter.clone();
        assertEquals(formatter.getTimeZone(), copy.getTimeZone());
        Date value =
                Date.from(
                        LocalDate.of(2022, 2, 3).atStartOfDay(ZoneId.systemDefault()).toInstant());
        assertEquals(formatter.format(value), copy.format(value));
    }
}
