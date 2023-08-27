package otserver4j.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
@lombok.extern.slf4j.Slf4j public class AMQPConfiguration {

  public static final String IN_GAME_ACTIONS_QUEUE = "inGameActionsQueue";

  @org.springframework.beans.factory.annotation.Autowired public AMQPConfiguration(
      @org.springframework.beans.factory.annotation.Value("classpath:qpid-configuration.json")
      org.springframework.core.io.Resource qpidConfigurationFile) { try {
    new org.apache.qpid.server.SystemLauncher().startup(java.util.Map.of("type", "Memory",
      "initialConfigurationLocation", qpidConfigurationFile.getFile().getAbsolutePath(),
      "startupLoggedToSystemOut", Boolean.TRUE)); }
    catch(Exception exc) {
      log.error("Unable to start Embedded AMQP Service: {}", exc.getMessage(), exc);
      System.exit(-java.math.BigInteger.ONE.intValue()); }}

  @Bean public org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory() {
    final com.rabbitmq.client.ConnectionFactory connectionFactory =
      new com.rabbitmq.client.ConnectionFactory();
    connectionFactory.setUsername("admin");
    connectionFactory.setPassword("1Q2w3e4r");
    connectionFactory.setPort(com.rabbitmq.client.AMQP.PROTOCOL.PORT);
    return new org.springframework.amqp.rabbit.connection.CachingConnectionFactory(connectionFactory);
  }

  @Bean public org.springframework.amqp.core.AmqpTemplate amqpTemplate(
    org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) { return new
    org.springframework.amqp.rabbit.core.RabbitTemplate(connectionFactory); }

  @Bean public Queue inGameActionsQueue() { return new Queue(IN_GAME_ACTIONS_QUEUE,
    Boolean.FALSE, Boolean.TRUE, Boolean.TRUE); }

}
