package otserver4j.config;

import java.math.BigInteger;
import java.util.Map;

import org.apache.qpid.server.SystemLauncher;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

@Configuration @Slf4j
public class AMQPConfiguration {

  public static final String IN_GAME_ACTIONS_QUEUE = "inGameActionsQueue";

  @Autowired public AMQPConfiguration(
      @Value("classpath:qpid-configuration.json") Resource qpidConfigurationFile) {
    try { new SystemLauncher().startup(Map.of("type", "Memory",
      "initialConfigurationLocation", qpidConfigurationFile.getFile().getAbsolutePath(),
      "startupLoggedToSystemOut", Boolean.TRUE)); }
    catch(Exception exc) {
      log.error("Unable to start Embedded AMQP Service: {}", exc.getMessage(), exc);
      System.exit(-BigInteger.ONE.intValue()); }
  }

  @Bean public ConnectionFactory connectionFactory() {
    final com.rabbitmq.client.ConnectionFactory connectionFactory =
      new com.rabbitmq.client.ConnectionFactory();
    connectionFactory.setUsername("admin");
    connectionFactory.setPassword("1Q2w3e4r");
    connectionFactory.setPort(com.rabbitmq.client.AMQP.PROTOCOL.PORT);
    return new org.springframework.amqp.rabbit.connection.CachingConnectionFactory(connectionFactory);
  }

  @Bean public org.springframework.amqp.core.AmqpTemplate amqpTemplate(
    ConnectionFactory connectionFactory) { return new
      org.springframework.amqp.rabbit.core.RabbitTemplate(connectionFactory); }

  @Bean public Queue inGameActionsQueue() { return new Queue(IN_GAME_ACTIONS_QUEUE,
    Boolean.FALSE, Boolean.FALSE, Boolean.FALSE); }

}
