package otserver4j.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpQueueConfiguration {

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
