package otserver4j.configuration;

import static com.rabbitmq.client.AMQP.PROTOCOL.PORT;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@lombok.extern.slf4j.Slf4j
@org.springframework.context.annotation.Configuration
public class AmqpConfiguration {

  @AllArgsConstructor @Getter public static class QpidUser {
    private String name;
    private String password;
    public String getType() { return "managed"; }
  }

  @AllArgsConstructor @Getter public static class QpidAuthenticationProvider {
    private String name;
    private List<QpidUser> users;
    public String getType() { return "Plain"; }
    public List<Object> getSecureOnlyMechanisms() {
      return Collections.emptyList(); }
  }

  @AllArgsConstructor @Getter public static class QpidVirtualHostAlias {
    private String name;
    public String getType() { return "defaultAlias"; }
  }

  @AllArgsConstructor @Getter public static class QpidPort {
    private String bindingAddress;
    private String port;
    private String authenticationProvider;
    private List<QpidVirtualHostAlias> virtualhostaliases;
    public String getName() { return "AMQP"; }
    public List<String> getProtocols() {
      return Collections.singletonList("AMQP_0_9_1"); }
  }
  @AllArgsConstructor @Getter public static class QpidVirtualHostNode {
    private String name;
    public String getType() { return "Memory"; }
    public String getDefaultVirtualHostNode() { return Boolean.TRUE.toString(); }
    public String getVirtualHostInitialConfiguration() {
      return String.format("{\"type\":\"%s\"}", this.getType()); }
  }

  @Getter public static class QpidConfiguration {
    private String name;
    private List<QpidAuthenticationProvider> authenticationproviders;
    private List<QpidPort> ports;
    private List<QpidVirtualHostNode> virtualhostnodes;
    public String getModelVersion() { return "7.0"; }
    public QpidConfiguration(String name, String host, Integer port, String user, String password) {
      this.name = String.format("%sEmbeddedBroker", name);
      this.authenticationproviders = Collections.singletonList(
        new QpidAuthenticationProvider(String.format("%s-amqp-user", name.toLowerCase()),
          Collections.singletonList(new QpidUser(user, password))));
      this.ports = Collections.singletonList(new QpidPort(
        host, port.toString(), String.format("%s-amqp-user", name.toLowerCase()),
        Collections.singletonList(new QpidVirtualHostAlias(name.toLowerCase()))
      ));
      this.virtualhostnodes = Collections.singletonList(
        new QpidVirtualHostNode(name.toLowerCase()));
    }
  }

  @Bean
  public org.springframework.amqp.rabbit.connection.ConnectionFactory amqpConnectionFactory(
      ObjectMapper objectMapper, @Value("${amqp.embedded:false}") Boolean isEmbedded,
      @Value("${amqp.host}") String host, @Value("${amqp.port:" + PORT + "}") Integer port,
      @Value("${amqp.username}") String username, @Value("${amqp.password}") String password) {
    if(isEmbedded) try {
      final Path tempQpidConfigFile = Files.write(
        Files.createTempFile("qpid-config-", ".json"), objectMapper.writeValueAsBytes(
          new QpidConfiguration("Embedded-AMQP-Service", host, port, username, password)));
      new org.apache.qpid.server.SystemLauncher().startup(java.util.Map.of("type", "Memory",
        org.apache.qpid.server.model.SystemConfig.INITIAL_CONFIGURATION_LOCATION,
          tempQpidConfigFile.toFile().getAbsolutePath()));
      tempQpidConfigFile.toFile().delete();
    }
    catch(Exception exc) {
      log.error("Unable to start Embedded AMQP Service: {}", exc.getMessage(), exc);
      System.exit(-java.math.BigInteger.ONE.intValue());
    }
    final com.rabbitmq.client.ConnectionFactory amqpConnectionFactory =
      new com.rabbitmq.client.ConnectionFactory();
    amqpConnectionFactory.setHost(host);
    amqpConnectionFactory.setPort(port);
    amqpConnectionFactory.setUsername(username);
    amqpConnectionFactory.setPassword(password);
    return new org.springframework.amqp.rabbit.connection.CachingConnectionFactory(
      amqpConnectionFactory);
  }

  @Bean
  public org.springframework.amqp.core.AmqpTemplate amqpTemplate(
      org.springframework.amqp.rabbit.connection.ConnectionFactory amqpConnectionFactory,
      otserver4j.service.impl.PacketMessageConverter packetMessageConverter) {
    final RabbitTemplate rabbitTemplate = new RabbitTemplate(amqpConnectionFactory);
    rabbitTemplate.setMessageConverter(packetMessageConverter);
    return rabbitTemplate;
  }

  public static final String PACKET_INPUT_QUEUE = "packetInputQueue";
  public static final String PACKET_OUTPUT_QUEUE = "packetOutputQueue";

  @Bean
  public Queue packetInputQueue() {
    return new Queue(PACKET_INPUT_QUEUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE);
  }

  @Bean
  public Queue packetOutputQueue() {
    return new Queue(PACKET_OUTPUT_QUEUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE);
  }

}
