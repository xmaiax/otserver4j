package otserver4j;

import static org.springframework.boot.SpringApplication.run;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j @SpringBootApplication public class Application {

  public static void main(String[] args) { run(Application.class, args); }

  @Value("${server.port}") private Integer webServerPort;
  
  @PostConstruct
  public void startedMessage() {
    log.info("Rest service starting in port: {}", this.webServerPort);
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
  }

}
