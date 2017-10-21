package hu.finominfo.rnet.communication.http;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.19.
 */
public enum Command {
    start,
    stop,
    unknown;

    public static Command get(String uri) {
        for (Command command : Command.values()) {
            if (uri.toLowerCase().endsWith(command.name().toLowerCase())) {
                return command;
            }
        }
        return unknown;
    }
}
