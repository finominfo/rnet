package hu.finominfo.rnet.taskqueue;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import hu.finominfo.rnet.frontend.controller.allcounter.AllCounter;
import hu.finominfo.rnet.frontend.controller.allcounter.CounterPanel;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
                case REFRESH_ALL_COUNTER:
                    refreshAllCounter();
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

    private void refreshAllCounter() {
        AllCounter allCounter = Globals.get().getAllCounter();
        if (allCounter == null) {
            return;
        }
        try {
            CounterPanel[] panels = allCounter.getPanels();
            if (panels != null) {
                final AtomicInteger i = new AtomicInteger(0);
                Globals.get().serverClients.entrySet().stream().forEach(ipClient -> {
                    try {
                        String status = ipClient.getValue().getStatus();
                        Optional<String> counterOptional = Arrays.stream(status.split("\n")).filter(row -> row.startsWith("Counter")).findFirst();
                        if (counterOptional.isPresent()) {
                            String counter = counterOptional.get().substring(9);
                            panels[i.get()].setCounter(counter.toLowerCase().contains("invisible") ? "" : counter);
                            panels[i.get()].setTitle(ipClient.getKey());
                            i.incrementAndGet();
                        }
                    } catch (Exception e) {
                        logger.error(Utils.getStackTrace(e));
                    }
                });
            }
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }


    }

    private String getIpPart(String ip) {
        return ip.substring(ip.lastIndexOf('.') + 1);
    }

    private void refreshServantList() {
        List<String> existingNameAndIpPart = Globals.get().serverClients.entrySet().stream()
                .filter(entry -> entry.getValue().getContext() != null)
                .sorted((o1, o2) -> (getIpPart(o1.getKey()).compareTo(getIpPart(o2.getKey()))))
                .map(entry -> entry.getValue().getName() + " (" + getIpPart(entry.getKey()) + ")")
                .collect(Collectors.toList());
        List<String> elements = Collections.list(Globals.get().getFrontEnd().servantsListModel.elements());
        if (!existingNameAndIpPart.equals(elements)) {
            List<String> selectedValuesList = Globals.get().getFrontEnd().servantsList.getSelectedValuesList();
            Globals.get().getFrontEnd().servantsListModel.clear();
            existingNameAndIpPart.forEach(nameOrIp -> Globals.get().getFrontEnd().servantsListModel.addElement(nameOrIp));
            if (selectedValuesList != null) {
                for (int i = 0; i < existingNameAndIpPart.size(); i++) {
                    if (selectedValuesList.contains(Globals.get().getFrontEnd().servantsListModel.elementAt(i))) {
                        Globals.get().getFrontEnd().servantsList.setSelectedIndex(i);
                    }
                }
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
