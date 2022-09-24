package org.nas.hystrix.rest.client.feign;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.nas.hystrix.rest.client.okhttp3.HttpLoggerService;
import org.nas.hystrix.rest.client.okhttp3.LoggingInterceptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static okhttp3.Protocol.HTTP_1_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.BDDMockito.*;

/**
 * LoggingInterceptorTest.java.
 *
 * @author Nassim MOUALEK.
 */
@ExtendWith(SpringExtension.class)
public class LoggingInterceptorTest {

    private static final String URL = "http://localhost:22421/message?param=value";

    @InjectMocks
    private LoggingInterceptor interceptor;

    @Mock
    private HttpLoggerService httpLoggerService;

    @Mock
    private Interceptor.Chain chain;

    private Request request = new Request.Builder().url(URL).build();

    @BeforeEach
    public void beforeEach() throws IOException {
        Mockito.reset(chain);
        Mockito.clearInvocations(httpLoggerService);
        given(chain.request()).willReturn(request);
    }

    @Test
    public void should_log_success_request() throws IOException {
        try {
            //Given
            Response response = new Response.Builder()
                    .code(302)
                    .request(request)
                    .protocol(HTTP_1_1)
                    .message("Response")
                    .build();
            given(chain.proceed(any())).willReturn(response);
            ArgumentCaptor<LoggingInterceptor.HttpRequestReport> captorHttpRequestReport = forClass(LoggingInterceptor.HttpRequestReport.class);

            //When
            interceptor.intercept(chain);

            //Then
            verify(httpLoggerService).log(captorHttpRequestReport.capture());
            assertThat(captorHttpRequestReport.getValue().getCodeResponse()).isEqualTo(302);
            assertThat(captorHttpRequestReport.getValue().getStatusFamily()).isEqualTo(LoggingInterceptor.StatusFamily.REDIRECTION);
            assertThat(captorHttpRequestReport.getValue().getMethod()).isEqualTo("GET");
            assertThat(captorHttpRequestReport.getValue().getProtocol()).isEqualTo(HTTP_1_1.toString());
            assertThat(captorHttpRequestReport.getValue().getUrl()).isEqualTo(URL);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void should_log_failure_request() throws IOException {

        //Given
        given(chain.proceed(any())).willThrow(IOException.class);
        ArgumentCaptor<LoggingInterceptor.HttpRequestReport> captorHttpRequestReport = forClass(LoggingInterceptor.HttpRequestReport.class);

        //When
        Throwable t = catchThrowable(() -> interceptor.intercept(chain));

        //Then
        assertThat(t).isInstanceOf(IOException.class);
        verify(httpLoggerService).log(captorHttpRequestReport.capture());
        assertThat(captorHttpRequestReport.getValue().getMethod()).isEqualTo("GET");
        assertThat(captorHttpRequestReport.getValue().getProtocol()).isEqualTo(HTTP_1_1.toString());
        assertThat(captorHttpRequestReport.getValue().getStatusFamily()).isEqualTo(LoggingInterceptor.StatusFamily.SERVER_ERROR);
        assertThat(captorHttpRequestReport.getValue().getUrl()).isEqualTo(URL);
    }

}
