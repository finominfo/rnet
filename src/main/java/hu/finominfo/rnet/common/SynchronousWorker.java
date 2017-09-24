package hu.finominfo.rnet.common;

import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by kk on 2017.09.23..
 */
public abstract class SynchronousWorker extends Worker {
    private final static Logger logger = Logger.getLogger(SynchronousWorker.class);

    public SynchronousWorker() {
        super();
    }

    public void runCurrentTask() {

        switch (currentTask.getTaskToDo()) {
            case LOAD_NAME_ADDRESS:
                loadNameAddress();
                currentTaskFinished();
                break;
            case SAVE_NAME_ADDRESS:
                saveNameAddress();
                currentTaskFinished();
                break;
            default:
                runCurrentAsynchronousTask();
                break;
        }
    }

    private void loadNameAddress() {
        try (Stream<String> stream = Files.lines(Paths.get(Globals.ADDRESSES))) {
            stream.forEach(str -> {
                String[] strings = str.split("=");
                String key = strings[0];
                List<Long> value = new ArrayList<>();
                for (String s : strings[1].split("-")) {
                    value.add(Long.parseLong(s));
                }
                Globals.get().clientNameAddress.put(key, value);
                logger.info("Loaded address: " + key + " -> " + value.stream().map(Object::toString).collect(Collectors.joining(",")));
            });
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void saveNameAddress() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, List<Long>> entry : Globals.get().clientNameAddress.entrySet()) {
            builder.append(entry.getKey()).append('=');
            builder.append(entry.getValue().stream().map(Object::toString).collect(Collectors.joining("-")));
            builder.append(System.lineSeparator());
        }
        try (PrintStream out = new PrintStream(new FileOutputStream(Globals.ADDRESSES))) {
            out.print(builder.toString());
        } catch (FileNotFoundException e) {
            logger.error(e);
        }
    }

    public abstract void runCurrentAsynchronousTask();
}