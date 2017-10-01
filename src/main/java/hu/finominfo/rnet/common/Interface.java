package hu.finominfo.rnet.common;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.17..
 */
public class Interface {

    public static volatile boolean interfaceOK = false;
    public static final List<Long> addresses = new ArrayList<>();
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
                        addresses.add(value);
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
            String fileName = "addr.txt";
            File file = new File(fileName);
            if (file.exists()) {
                String address = new String(Files.readAllBytes(Paths.get(fileName)));
                if (address.startsWith("71690003")) {
                    interfaceOK = true;
                    final String content = addresses.stream().map(Object::toString).collect(Collectors.joining("-"));
                    Files.write(Paths.get(fileName), content.getBytes());
                } else {
                    List<Long> fileAddresses = Arrays.asList(address.split("-")).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
                    if (Utils.isAddressEquals(addresses, fileAddresses)) {
                        interfaceOK = true;
                    }
                }
            }

        } catch (Exception ex) {
        }
    }
}
