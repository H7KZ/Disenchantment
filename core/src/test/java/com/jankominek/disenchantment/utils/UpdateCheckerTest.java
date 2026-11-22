package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static org.junit.jupiter.api.Assertions.*;

class UpdateCheckerTest extends DisenchantmentTestBase {

    private List<String> captureLogsWhile(Runnable action) {
        List<String> messages = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                messages.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        Disenchantment.logger.addHandler(handler);
        try {
            action.run();
        } finally {
            Disenchantment.logger.removeHandler(handler);
        }
        return messages;
    }

    // -> response validation

    @ParameterizedTest
    @ValueSource(strings = {"6.5.12", "v6.5.12", "6.5", "10", "6.5.12.1", "6.5.12-SNAPSHOT", "7.0.0-beta.1"})
    void givenVersionLikeText_whenIsVersion_thenTrue(String text) {
        assertTrue(UpdateChecker.isVersion(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {"<!DOCTYPE", "<html>", "Just", "error", "", "6.5.x", "..", "1.2.3.4.5"})
    void givenNonVersionText_whenIsVersion_thenFalse(String text) {
        assertFalse(UpdateChecker.isVersion(text));
    }

    @Test
    void givenNull_whenIsVersion_thenFalse() {
        assertFalse(UpdateChecker.isVersion(null));
    }

    // -> numeric comparison

    @Test
    void givenNewerRemote_whenIsNewer_thenTrue() {
        assertTrue(UpdateChecker.isNewer("6.5.12", "6.5.13"));
        assertTrue(UpdateChecker.isNewer("6.9.0", "6.10.0"));
        assertTrue(UpdateChecker.isNewer("6.5", "6.5.1"));
        assertTrue(UpdateChecker.isNewer("6.5.12", "v7.0.0"));
    }

    @Test
    void givenSameOrOlderRemote_whenIsNewer_thenFalse() {
        assertFalse(UpdateChecker.isNewer("6.5.12", "6.5.12"));
        assertFalse(UpdateChecker.isNewer("6.5.13", "6.5.12"), "newer local dev build must not be reported as outdated");
        assertFalse(UpdateChecker.isNewer("6.10.0", "6.9.9"));
        assertFalse(UpdateChecker.isNewer("6.5.12-SNAPSHOT", "6.5.12"));
        assertFalse(UpdateChecker.isNewer("6.5.0", "6.5"));
    }

    // -> scheduled task

    @Test
    void givenNewerRemote_whenTaskRuns_thenUpdateLogged() {
        UpdateChecker checker = new UpdateChecker(1, () -> "99.0.0");

        List<String> logs = captureLogsWhile(checker.runnableUpdateTask("6.5.12"));

        assertTrue(logs.stream().anyMatch(m -> m.contains("new version available: v99.0.0")), "got: " + logs);
    }

    @Test
    void givenOlderRemote_whenTaskRuns_thenNothingAnnounced() {
        UpdateChecker checker = new UpdateChecker(1, () -> "6.0.0");

        List<String> logs = captureLogsWhile(checker.runnableUpdateTask("6.5.12"));

        assertTrue(logs.stream().noneMatch(m -> m.contains("new version available")), "got: " + logs);
    }

    @Test
    void givenHtmlResponse_whenTaskRuns_thenIgnored() {
        UpdateChecker checker = new UpdateChecker(1, () -> "<!DOCTYPE");

        List<String> logs = captureLogsWhile(checker.runnableUpdateTask("6.5.12"));

        assertTrue(logs.stream().noneMatch(m -> m.contains("new version available")), "got: " + logs);
    }

    @Test
    void givenCheckerDisabled_whenTaskRuns_thenNoRequestMade() {
        setConfig("update-checker", false);
        AtomicInteger requests = new AtomicInteger();
        UpdateChecker checker = new UpdateChecker(1, () -> {
            requests.incrementAndGet();
            return "99.0.0";
        });

        List<String> logs = captureLogsWhile(checker.runnableUpdateTask("6.5.12"));

        assertEquals(0, requests.get(), "No network access while update-checker is false");
        assertTrue(logs.stream().noneMatch(m -> m.contains("new version available")));
    }

    @Test
    void givenCheckerReEnabled_whenTaskRunsAgain_thenRequestMade() {
        AtomicInteger requests = new AtomicInteger();
        UpdateChecker checker = new UpdateChecker(1, () -> {
            requests.incrementAndGet();
            return "1.0.0";
        });
        Runnable task = checker.runnableUpdateTask("6.5.12");

        setConfig("update-checker", false);
        task.run();
        setConfig("update-checker", true);
        task.run();

        assertEquals(1, requests.get());
    }
}
