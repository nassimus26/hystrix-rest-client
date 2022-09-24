package org.nas.hystrix.rest.client.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * A list of messages which may be sent by a service in case of errors.
 *
 * @author Nassim MOUALEK
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HystrixRestClientMessages implements Serializable {

    /**
     * The type of the error (TECHNICAL, FUNCTIONAL).
     */
    private String type;
    /**
     * The list of the {@link Message}.
     */
    private List<Message> messages;

    @Override
    public String toString() {

        if (CollectionUtils.isEmpty(messages)) {
            return "";
        }
        return messages.stream().map(Message::getMessage).collect(joining(", "));
    }

    /**
     * An error message sent by a service.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message implements Serializable {

        /**
         * The code of the message (eg 'err.tech.badrequest').
         */
        private String code;
        /**
         * The value of the message in english.
         */
        private String message;
        /**
         * The list of parameters used in the message (may be null).
         */
        private Map<String, String> parameters;
        /**
         * The path of the field in error (eg 'user.firstName').
         * may be null.
         */
        private String propertyPath;
    }


}
