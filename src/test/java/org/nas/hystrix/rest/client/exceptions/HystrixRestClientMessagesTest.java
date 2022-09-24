package org.nas.hystrix.rest.client.exceptions;

import com.google.common.testing.EqualsTester;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * MessagesTest.java.
 *
 * @author Nassim MOUALEK.
 */

public class HystrixRestClientMessagesTest {

    @Test
    public void testMessages() {
        //Given
        List<HystrixRestClientMessages.Message> listMessage = asList(HystrixRestClientMessages.Message.builder().code("err.tech").message("error message").build());
        HystrixRestClientMessages.HystrixRestClientMessagesBuilder restClientMessagesBuilder = HystrixRestClientMessages.builder().type("TECHNICAL").messages(listMessage);
        HystrixRestClientMessages restClientMessages = restClientMessagesBuilder.build();
        //Then
        Assertions.assertThat(listMessage).hasSize(1);
        assertThat(restClientMessages.getMessages()).hasSize(1);
        assertThat(restClientMessages.getType()).isEqualTo("TECHNICAL");
        assertThat(restClientMessages.getMessages().get(0).getCode()).isEqualTo("err.tech");
        assertThat(restClientMessages.getMessages().get(0).getMessage()).isEqualTo("error message");
        assertThat(restClientMessages.getMessages().get(0).getParameters()).isNull();
        assertThat(restClientMessages.getMessages().get(0).getPropertyPath()).isNull();
        assertThat(restClientMessagesBuilder.toString()).isNotBlank();

        HystrixRestClientMessages restClientMessages2 = new HystrixRestClientMessages();
        restClientMessages2.setType("FUNCTIONAL");
        restClientMessages2.setMessages(null);
        assertThat(restClientMessages2.getMessages()).isNull();

        HystrixRestClientMessages rcMessages3 = new HystrixRestClientMessages(null, null);

        EqualsTester equalsTester = new EqualsTester()
                .addEqualityGroup(restClientMessages)
                .addEqualityGroup(restClientMessages2)
                .addEqualityGroup(rcMessages3);
        equalsTester.testEquals();

    }

    @Test
    public void testMessage() {
        //Given
        HystrixRestClientMessages.Message.MessageBuilder rcMessageBuilder = HystrixRestClientMessages.Message.builder().code("err.func").message("error message")
                .propertyPath(null).parameters(null);
        HystrixRestClientMessages.Message rcMessage = rcMessageBuilder.build();
        //Then
        assertThat(rcMessage.getCode()).isEqualTo("err.func");
        assertThat(rcMessage.getMessage()).isEqualTo("error message");
        assertThat(rcMessage.getPropertyPath()).isNull();
        assertThat(rcMessage.getParameters()).isNull();

        HystrixRestClientMessages.Message rcMessage2 = new HystrixRestClientMessages.Message();
        rcMessage2.setCode("err.tech");
        rcMessage2.setMessage("error message");
        rcMessage2.setPropertyPath("user name");
        rcMessage2.setParameters(new HashMap<>());

        assertThat(rcMessage2.getParameters()).isNotNull();
        assertThat(rcMessage2.getPropertyPath()).isEqualTo("user name");
        assertThat(rcMessageBuilder.toString()).isNotBlank();

        //Equals
        EqualsTester equalsTester = new EqualsTester()
                .addEqualityGroup(rcMessage2)
                .addEqualityGroup(HystrixRestClientMessages.Message.builder().build())
                .addEqualityGroup(rcMessage);
        equalsTester.testEquals();

    }

}
