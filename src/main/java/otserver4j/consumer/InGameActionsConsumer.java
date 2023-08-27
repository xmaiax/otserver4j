package otserver4j.consumer;

@org.springframework.stereotype.Component public class InGameActionsConsumer {

  @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = {
    otserver4j.config.AMQPConfiguration.IN_GAME_ACTIONS_QUEUE, })
  public void listen(String in) {
    System.out.println("Message read from myQueue : " + in);
  }

}
