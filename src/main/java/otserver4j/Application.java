package otserver4j;

import static org.springframework.boot.SpringApplication.run;
import com.fasterxml.jackson.databind.ObjectMapper;

@org.springframework.boot.autoconfigure.SpringBootApplication
@lombok.extern.slf4j.Slf4j public class Application {
  public static void main(String[] args) { run(Application.class, args); }
  @org.springframework.beans.factory.annotation.Value("${server.port}") private Integer webServerPort;
  @javax.annotation.PostConstruct public void startedMessage() {
    log.info("Rest service starting in port: {}", this.webServerPort); }
  @org.springframework.context.annotation.Bean public ObjectMapper objectMapper() {
    return new ObjectMapper()
      .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
      .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
      .configure(com.fasterxml.jackson.databind.SerializationFeature
        .WRITE_DATES_AS_TIMESTAMPS, Boolean.FALSE);
  }
}
