package org.example.tools.cmd;

import java.io.*;
import java.util.concurrent.Callable;

/**
 * shell命令异步读取
 */
public class CommandAsyncReader implements Callable<String> {
    private final InputStream input;
    private final String osCharset;

    private volatile boolean stopped = false;

    public CommandAsyncReader(InputStream input, String osCharset) {
        this.input = input;
        this.osCharset = osCharset;
    }

    public void stop() {
        stopped = true;
    }

    @Override
    public String call() {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, osCharset))) {
            String line;
            while (!stopped && (line = reader.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            // do noThing
        }
        return result.toString();
    }
}
