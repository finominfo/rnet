package hu.finominfo.rnet.communication;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.17..
 */
public class Interface {

    public static final List<Long> adresses = new ArrayList<>();
    public static final List<String> ips = new ArrayList<>();

    public static void getInterfaces() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                byte[] hardwareAddress = networkInterface.getHardwareAddress();
                if (null != hardwareAddress) {
                    long value = 0;
                    for (int i = 0; i < hardwareAddress.length; i++) {
                        value <<= 8;
                        value |= hardwareAddress[i] & 0xff;
                    }
                    if (value > 1000) {
                        adresses.add(value);
                        if (networkInterface.isUp() && networkInterface.getInetAddresses().hasMoreElements()) {
                            for (Enumeration<InetAddress> enumIpAddress = networkInterface.getInetAddresses(); enumIpAddress.hasMoreElements(); ) {
                                InetAddress inetAddress = enumIpAddress.nextElement();
                                if (inetAddress.isSiteLocalAddress()) {
                                    ips.add(inetAddress.getHostAddress());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
    }
}
