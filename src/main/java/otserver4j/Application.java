package otserver4j;

import static java.math.BigInteger.ONE;
import static org.springframework.boot.SpringApplication.run;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.qpid.server.SystemLauncher;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;

import lombok.extern.slf4j.Slf4j;
import otserver4j.converter.PacketMessageConverter;

@Slf4j @SpringBootApplication
public class Application {

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
  
  public static final String PACKET_INPUT_QUEUE = "packetInputQueue";
  public static final String PACKET_OUTPUT_QUEUE = "packetOutputQueue";

  @Bean
  public ConnectionFactory amqpConnectionFactory(
      @Value("true") Boolean runEmbeddedAmqpService,
      @Value("classpath:qpid-configuration.json") Resource qpidConfigurationFile) {
    if(runEmbeddedAmqpService) { 
      try {
        new SystemLauncher().startup(
          Map.of("type", "Memory",
                 "initialConfigurationLocation", qpidConfigurationFile.getFile().getAbsolutePath(),
                 "startupLoggedToSystemOut", Boolean.TRUE)
        );
      }
      catch(Exception exc) {
        log.error("Unable to start Embedded AMQP Service: {}", exc.getMessage(), exc);
        System.exit(-ONE.intValue());
      }
    }
    final com.rabbitmq.client.ConnectionFactory amqpConnectionFactory =
      new com.rabbitmq.client.ConnectionFactory();
    amqpConnectionFactory.setUsername("admin");
    amqpConnectionFactory.setPassword("1Q2w3e4r");
    amqpConnectionFactory.setPort(AMQP.PROTOCOL.PORT);
    return new CachingConnectionFactory(amqpConnectionFactory);
  }

  @Bean
  public AmqpTemplate amqpTemplate(ConnectionFactory amqpConnectionFactory,
      PacketMessageConverter packetMessageConverter) {
    final RabbitTemplate rabbitTemplate = new RabbitTemplate(amqpConnectionFactory);
    rabbitTemplate.setMessageConverter(packetMessageConverter);
    return rabbitTemplate;
  }

  @Bean
  public Queue packetInputQueue() {
    return new Queue(PACKET_INPUT_QUEUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE);
  }

  @Bean
  public Queue packetOutputQueue() {
    return new Queue(PACKET_OUTPUT_QUEUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE);
  }

}
