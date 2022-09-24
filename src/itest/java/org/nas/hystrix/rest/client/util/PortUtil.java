package org.nas.hystrix.rest.client.util;

import org.springframework.util.SocketUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class PortUtil {
    private static AtomicInteger port;

    public static int nextPort() {
        if (port == null) {
            port = new AtomicInteger();
            port.set(SocketUtils.findAvailableTcpPort());
        }
        return port.getAndDecrement();
    }
}
