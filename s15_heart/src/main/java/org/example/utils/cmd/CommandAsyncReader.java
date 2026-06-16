package org.example.utils.cmd;

import java.io.*;
import java.util.concurrent.Callable;

/**
 * shell命令异步读取
 */
public class CommandAsyncReader implements Callable<String> {
    private final InputStream input;

    private volatile boolean stopped = false;

    public CommandAsyncReader(InputStream input) {
        this.input = input;
    }

    public void stop() {
        stopped = true;
    }

    @Override
    public String call() {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while (!stopped && (line = reader.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            result.append("Error happen:").append(System.lineSeparator());
            result.append(e.getMessage()).append(System.lineSeparator());
        }
        return result.toString();
    }
}
