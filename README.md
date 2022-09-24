#  Hystrix Rest Client



## Content

- Spring Boot
- Lombok
- OkHttp3
- OpenFeign 

    
## Feign makes writing java http clients easier
Feign is a java to http client binder inspired by Retrofit, JAXRS-2.0, and WebSocket.

## Usage
  

### 1. Enable Rest client  
##### Here's an example:
      
```java

@EnableRestClient(basePackages = { "org.nas.rest.clients" })
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
 ```

  
### 2. Create Rest client  
##### Here's an example:
      

```java
@RestClient(name = "client", fallback = ClientServiceFallback.class, useThreads = false, 
                                                        hystrixTimeoutMilliseconds = 500)
public interface ClientService {
     @RequestLine("GET /message")
     Response getMessage();
}

public class ClientServiceFallback implements ClientService {
   @Autowired
   private ObjectMapper defaultObjectMapper;

   @Override
   public Response getMessage() throws JsonProcessingException {
       byte[] bytes = defaultObjectMapper.writeValueAsBytes(new Message());
       return Response.builder().status(200).body(bytes).headers(new HashMap<>()).build();
   }
}
```



This is [OpenFeign documentation](https://github.com/OpenFeign/feign) .

This is [Hystrix documentation](https://github.com/OpenFeign/feign/tree/master/hystrix) .

In RestClient annotation, you can configure 

1. **name** : This property sets the service name.
2. **fallback** : Fallback class, default value InterfaceNameFallback
3. **url** : This property sets the absolute URL or resolvable hostname (the protocol is optional). if url is not set, the default value of url is ${eco.rest.name.url}
4. **useThreads** : This property indicates which isolation strategy HystrixCommand.run() executes with, one of the following two choices.

        TRUE : THREAD — it executes on a separate thread and concurrent requests are limited by the number of threads in the thread-pool
        FALSE : SEMAPHORE — it executes on the calling thread and concurrent requests are limited by the semaphore count
            
5. **hystrixTimeoutMilliseconds** :  This property sets the time in milliseconds after which the caller will observe a timeout and walk away from the command execution.
 Hystrix marks the HystrixCommand as a TIMEOUT, and performs fallback logic. Note that there is configuration for turning off timeouts per-command.
  If time equal to 0, there is no time out. Defaults to 60000. Value must be higher than read and connect timeouts.
6. **semaphoreMaxConcurrentRequests** : This property sets the number of concurrent request and fallback authorized  when the strategy semaphore is activated
7. **encoder** : This property sets the encoding used to encode the request 

            JACKSON_ENCODER: the default encoder, it used for Json request
            MULTIPART_ENCODER: it used for Multi-Part request
8. **decoder** : This property sets the decoding used to decode the response
9. **connectTimeoutMilliseconds** : Sets a specified connect timeout value, in milliseconds, to be used when opening a communications link to the resource referenced
   by this url. If the timeout expires before the connection can be established, performs fallback logic. A timeout of zero is interpreted as an infinite timeout. Defaults to 10000.
10. **readTimeoutMilliseconds** : Sets the read timeout to a specified timeout, in milliseconds. A non-zero value specifies the timeout when
    reading from Input stream when a connection is established to a resource. If the timeout expires before there is data available for read, performs fallback logic. A timeout of zero is interpreted as an infinite timeout. Defaults to 10000.
  
  In a configuration file, you can configure: 
* hystrixTimeoutMilliseconds
* connectTimeoutMilliseconds
* readTimeoutMilliseconds

```yaml
hystrix-rest-client:
      <parameter-name-from-the-annotation>:
        hystrixTimeoutMilliseconds: 1000
```

### 3. Use service
##### Here's an example:

```java
@Service
public class MessageService {
    @Autowired
    private  ExapmleClientService clientService;
    public Message getMessage() {
        feign.Response response=clientService.getMessage();
         Message result = defaultObjectMapper.readValue(response.body().asInputStream(), Message.class);
         return result;
    }
}   
```

### 4. Download

Download the  JAR gradle


```gradle
  compile 'io.github.nassimus26:hystrix-rest-client:0.1'
```

## Requirement

You need to get a JDK 8 and you're good.

## Build

This task will run tests and build the library. 

    ./gradlew build

[Nassim MOUALEK](https://www.linkedin.com/in/nassim-moualek-8ab7231a/), Nov.2020    