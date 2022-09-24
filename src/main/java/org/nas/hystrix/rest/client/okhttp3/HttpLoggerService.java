package org.nas.hystrix.rest.client.okhttp3;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;

/**
 * HttpLoggerService.java.
 *
 * @author Nassim MOUALEK.
 */
@Slf4j
public class HttpLoggerService {

    public void log(LoggingInterceptor.HttpRequestReport httpRequestReport) {
        log.info(Markers.append("http_report", httpRequestReport), "Perform HTTP request {}", httpRequestReport);
    }

}
