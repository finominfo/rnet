package hu.finominfo.rnet.communication.connection;


/**
 * Created by User on 2017.09.16..
 */
public class Connection {

    public static final byte SEPARATOR = (byte)':';
    public static final int CODE = 1254646757;
    private final String serverIp;
    private final int serverPort;

    public Connection(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Connection) {
            Connection connection = (Connection) obj;
            return this.getServerPort() == connection.getServerPort()
                    && this.getServerIp().equals(connection.getServerIp());
        }
        return false;
    }
}
