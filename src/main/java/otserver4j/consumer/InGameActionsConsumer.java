package otserver4j.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import otserver4j.config.AMQPConfiguration;

@Component
public class InGameActionsConsumer {

  @RabbitListener(queues = AMQPConfiguration.IN_GAME_ACTIONS_QUEUE)
  public void listen(String in) {
    System.out.println("Message read from myQueue : " + in);
  }

}
