package otserver4j.configuration;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.qpid.server.SystemLauncher;
import org.apache.qpid.server.model.SystemConfig;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import otserver4j.converter.PacketMessageConverter;

@Slf4j @Configuration
public class AmqpConnectionConfiguration {

  @AllArgsConstructor @Getter
  @JsonPropertyOrder({ "name", "password", "type", })
  public static class QpidUser {
    private String name;
    private String password;
    public String getType() { return "managed"; }
  }

  @AllArgsConstructor @Getter
  @JsonPropertyOrder({ "name", "type", "users", "secureOnlyMechanisms", })
  public static class QpidAuthenticationProvider {
    private String name;
    private List<QpidUser> users;
    public String getType() { return "Plain"; }
    public List<Object> getSecureOnlyMechanism() {
      return Collections.emptyList(); }
  }

  @AllArgsConstructor @Getter @JsonPropertyOrder({ "name", "type", })
  public static class QpidVirtualHostAlias {
    private String name;
    public String getType() { return "defaultAlias"; }
  }

  @AllArgsConstructor @Getter @JsonPropertyOrder({ "name", "port",
    "bindingAddress", "protocols", "authenticationProvider", "virtualhostaliases", })
  public static class QpidPort {
    private String port;
    private String authenticationProvider;
    private List<QpidVirtualHostAlias> virtualhostaliases;
    public String getBindingAddress() { return "localhost"; };
    public String getName() { return "AMQP"; }
    public List<String> getProtocols() {
      return Collections.singletonList("AMQP_0_9_1"); }
  }
  @AllArgsConstructor @Getter @JsonPropertyOrder({
    "name", "type", "defaultVirtualHostNode", "virtualHostInitialConfiguration", })
  public static class QpidVirtualHostNode {
    private String name;
    public String getType() { return "Memory"; }
    public String getDefaultVirtualHostNode() { return Boolean.TRUE.toString(); }
    public String getVirtualHostInitialConfiguration() {
      return String.format("{\"type\":\"%s\"}", this.getType()); }
  }

  @Getter @JsonPropertyOrder(
    { "name", "modelVersion", "authenticationproviders", "ports", "virtualhostnodes", })
  public static class QpidConfiguration {
    private String name;
    private List<QpidAuthenticationProvider> authenticationproviders;
    private List<QpidPort> ports;
    private List<QpidVirtualHostNode> virtualhostnodes;
    public String getModelVersion() { return "7.0"; }
    public QpidConfiguration(String name, Integer port, String user, String password) {
      this.name = String.format("%sEmbeddedBroker", name);
      this.authenticationproviders = Collections.singletonList(
        new QpidAuthenticationProvider(String.format("%s-amqp-user", name.toLowerCase()),
          Collections.singletonList(new QpidUser(user, password))));
      this.ports = Collections.singletonList(new QpidPort(
        port.toString(), String.format("%s-amqp-user", name.toLowerCase()),
        Collections.singletonList(new QpidVirtualHostAlias(name.toLowerCase()))
      ));
      this.virtualhostnodes = Collections.singletonList(
        new QpidVirtualHostNode(name.toLowerCase()));
    }
  }

  @Bean
  public ConnectionFactory amqpConnectionFactory(
      @Value("true") Boolean runEmbeddedAmqpService,
      ObjectMapper objectMapper) {
    if(runEmbeddedAmqpService) { 
      try {
        final Path qpidConfigurationPath = Files.write(Files.createTempFile(null, ".json"),
          objectMapper.writeValueAsBytes(new QpidConfiguration(
            "OTServer4j", 5672, "admin", "1Q2w3e4r")));
        new SystemLauncher().startup(Map.of("startupLoggedToSystemOut", Boolean.TRUE,
          "type", "Memory", SystemConfig.DEFAULT_INITIAL_CONFIG_NAME,
            qpidConfigurationPath.toFile().getAbsolutePath()));
      }
      catch(Exception exc) {
        log.error("Unable to start Embedded AMQP Service: {}", exc.getMessage(), exc);
        System.exit(-BigInteger.ONE.intValue());
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

}
