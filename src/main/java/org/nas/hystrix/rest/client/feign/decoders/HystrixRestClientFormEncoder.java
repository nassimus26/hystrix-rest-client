package org.nas.hystrix.rest.client.feign.decoders;

/**
 * Created by Nassim MOUALEK on 24/09/2018.
 */

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A custom {@link feign.codec.Encoder} that supports Multipart requests.
 * It uses {@link HttpMessageConverter}
 */
public class HystrixRestClientFormEncoder implements Encoder {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String EXCEPTION_MESSAGE = "Cannot encode request.";
    private final Encoder delegate;
    private final HttpHeaders multipartHeaders = new HttpHeaders();
    private final HttpHeaders jsonHeaders = new HttpHeaders();
    private final ArrayList<HttpMessageConverter> converters;

    public HystrixRestClientFormEncoder(Encoder defaultEncoder) {
        multipartHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        converters = new ArrayList<>();
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new StringHttpMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new SourceHttpMessageConverter());
        converters.add(new AllEncompassingFormHttpMessageConverter());
        this.delegate = defaultEncoder;
    }

    /**
     * Heuristic check for multipart requests.
     *
     * @param type
     * @return
     * @see feign.Types#
     */
    static boolean isFormRequest(Type type) {
        return MAP_STRING_WILDCARD.equals(type);
    }

    private static boolean isMultipartFile(Object object) {
        return object instanceof MultipartFile;
    }

    /**
     * Wraps an object into a {@link HttpEntity} and sets the {@code Content-type} header to {@code application/json}.
     *
     * @param o
     * @return
     */
    private static HttpEntity<?> encodeJsonObject(Object o) {
        HttpHeaders jsonPartHeaders = new HttpHeaders();
        jsonPartHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(o, jsonPartHeaders);
    }

    private static boolean isMultipartFileArray(Object o) {
        return o != null && o.getClass().isArray() && MultipartFile.class.isAssignableFrom(o.getClass().getComponentType());
    }

    /**
     * Wraps a single {@link MultipartFile} into a {@link HttpEntity} and sets the {@code Content-type} header to {@code application/octet-stream}.
     *
     * @param file
     * @return
     */
    private static HttpEntity<?> encodeMultipartFile(MultipartFile file) {
        HttpHeaders filePartHeaders = new HttpHeaders();
        filePartHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        try {
            Resource multipartFileResource = new MultipartFileResource(file.getOriginalFilename(), file.getSize(), file.getInputStream());
            return new HttpEntity<>(multipartFileResource, filePartHeaders);
        } catch (IOException ex) {
            throw new EncodeException(EXCEPTION_MESSAGE, ex);
        }
    }

    /**
     * Fills the request map with {@link HttpEntity}s containing the given {@link MultipartFile}s.
     * Sets the {@code Content-type} header to {@code application/octet-stream} for each file.
     *
     * @param map   current request map.
     * @param name  the name of the array field in the multipart form.
     * @param files
     */
    private static void encodeMultipartFiles(LinkedMultiValueMap<String, Object> map, String name, List<? extends MultipartFile> files) {
        HttpHeaders filePartHeaders = new HttpHeaders();
        filePartHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        try {
            for (MultipartFile file : files) {
                Resource multipartFileResource = new MultipartFileResource(file.getOriginalFilename(), file.getSize(), file.getInputStream());
                map.add(name, new HttpEntity<>(multipartFileResource, filePartHeaders));
            }
        } catch (IOException ex) {
            throw new EncodeException(EXCEPTION_MESSAGE, ex);
        }
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        if (isFormRequest(bodyType)) {
            encodeMultipartFormRequest((Map<String, ?>) object, template);
        } else {
            delegate.encode(object, bodyType, template);
        }
    }

    /**
     * Encodes the request as a multipart form. It can detect a single {@link MultipartFile}, an
     * array of {@link MultipartFile}s, or POJOs (that are converted to JSON).
     *
     * @param formMap
     * @param template
     * @throws EncodeException
     */
    private void encodeMultipartFormRequest(Map<String, ?> formMap, RequestTemplate template) throws EncodeException {
        if (formMap == null) {
            throw new EncodeException("Cannot encode request with null form.");
        }
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        for (Entry<String, ?> entry : formMap.entrySet()) {
            Object value = entry.getValue();
            if (isMultipartFile(value)) {
                map.add(entry.getKey(), encodeMultipartFile((MultipartFile) value));
            } else if (isMultipartFileArray(value)) {
                encodeMultipartFiles(map, entry.getKey(), Arrays.asList((MultipartFile[]) value));
            } else {
                map.add(entry.getKey(), encodeJsonObject(value));

            }
        }
        encodeRequest(map, multipartHeaders, template);
    }

    /**
     * Calls the conversion chain.
     *
     * @param value
     * @param requestHeaders
     * @param template
     * @throws EncodeException
     */
    private void encodeRequest(Object value, HttpHeaders requestHeaders, RequestTemplate template) throws EncodeException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpOutputMessage dummyRequest = new HttpOutputMessageImpl(outputStream, requestHeaders);
        try {
            Class<?> requestType = value.getClass();
            MediaType requestContentType = requestHeaders.getContentType();
            for (HttpMessageConverter<?> messageConverter : converters) {
                if (messageConverter.canWrite(requestType, requestContentType)) {
                    ((HttpMessageConverter<Object>) messageConverter).write(
                            value, requestContentType, dummyRequest);
                    break;
                }
            }
        } catch (IOException ex) {
            throw new EncodeException(EXCEPTION_MESSAGE, ex);
        }
        HttpHeaders headers = dummyRequest.getHeaders();
        if (headers != null) {
            for (Entry<String, List<String>> entry : headers.entrySet()) {
                template.header(entry.getKey(), entry.getValue());
            }
        }
        /*
        we should use a template output stream... this will cause issues if files are too big,
        since the whole request will be in memory.
         */
        template.body(outputStream.toByteArray(), UTF_8);
    }

    /**
     * Dummy resource class. Wraps file content and its original name.
     */
    @SuppressWarnings("squid:S1185")
    static class MultipartFileResource extends InputStreamResource {

        private final String filename;
        private final long size;

        public MultipartFileResource(String filename, long size, InputStream inputStream) {
            super(inputStream);
            this.size = size;
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

        @Override
        public InputStream getInputStream() throws IOException, IllegalStateException {
            return super.getInputStream(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public long contentLength() throws IOException {
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

    }

    /**
     * Minimal implementation of {@link org.springframework.http.HttpOutputMessage}. It's needed to
     * provide the request body output stream to
     * {@link org.springframework.http.converter.HttpMessageConverter}s
     */
    private class HttpOutputMessageImpl implements HttpOutputMessage {

        private final OutputStream body;
        private final HttpHeaders headers;

        public HttpOutputMessageImpl(OutputStream body, HttpHeaders headers) {
            this.body = body;
            this.headers = headers;
        }

        @Override
        public OutputStream getBody() throws IOException {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

    }

}
