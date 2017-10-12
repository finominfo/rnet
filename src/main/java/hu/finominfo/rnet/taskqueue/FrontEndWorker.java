package hu.finominfo.rnet.taskqueue;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by kk on 2017.09.23..
 */
public class FrontEndWorker extends Worker {
    private final static Logger logger = Logger.getLogger(FrontEndWorker.class);

    public FrontEndWorker() {
        super();
    }

    public void runCurrentTask() {


        try {
            switch (currentTask.getFrontEndTaskToDo()) {
                case LOAD_NAME_ADDRESS:
                    loadNameAddress();
                    break;
                case SAVE_NAME_ADDRESS:
                    saveNameAddress();
                    break;
                case REFRESH_SERVANT_LIST:
                    refreshServantList();
                    break;
                default:
                    logger.error("Not implemented task: " + currentTask.getTaskToDo().toString());
                    break;
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            currentTaskFinished();
        }

    }

    private void refreshServantList() {
        List<String> existingNameOrIp = Globals.get().serverClients.values().stream()
                .filter(clientParam -> clientParam.getContext() != null)
                .map(ClientParam::getName).collect(Collectors.toList());
        List<String> elements = Collections.list(Globals.get().getFrontEnd().servantsListModel.elements());
        existingNameOrIp.stream()
                .filter(nameOrIp -> !elements.contains(nameOrIp))
                .forEach(Globals.get().getFrontEnd().servantsListModel::addElement);
        for (int i = 0; i < elements.size(); i++) {
            if (!existingNameOrIp.contains(elements.get(i))) {
                Globals.get().getFrontEnd().servantsListModel.remove(i);
            }
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
                logger.info("Loaded status: " + key + " -> " + value.stream().map(Object::toString).collect(Collectors.joining(",")));
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

    @Override
    protected Task getTask() {
        return Globals.get().frontEndTasks.poll();
    }

}
