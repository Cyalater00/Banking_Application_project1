package com.banking.util;

import com.banking.exceptions.SessionTimeoutException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** Reads console input asynchronously and preserves the existing session timeout. */
public final class InputReader {
    private static final int SESSION_TIMEOUT_SECONDS = 60;
    private static final String END_OF_INPUT = "__END_OF_INPUT__";
    private static final BlockingQueue<String> INPUT_QUEUE = new LinkedBlockingQueue<>();

    private InputReader() { }

    public static void start() {
        Thread readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                while ((line = reader.readLine()) != null) INPUT_QUEUE.put(line);
                INPUT_QUEUE.put(END_OF_INPUT);
            } catch (Exception e) {
                INPUT_QUEUE.offer(END_OF_INPUT);
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public static String ask(String prompt, boolean loggedIn)
            throws InterruptedException, SessionTimeoutException {
        System.out.print(prompt);
        String answer;
        if (loggedIn) {
            answer = INPUT_QUEUE.poll(SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (answer == null) throw new SessionTimeoutException();
        } else {
            answer = INPUT_QUEUE.take();
        }
        return END_OF_INPUT.equals(answer) ? null : answer;
    }
}
