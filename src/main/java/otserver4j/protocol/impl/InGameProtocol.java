package otserver4j.protocol.impl;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import org.springframework.stereotype.Component;

import otserver4j.exception.InGameException;
import otserver4j.packet.Packet;
import otserver4j.protocol.Protocol;

import lombok.extern.slf4j.Slf4j;

@Component @Slf4j
public class InGameProtocol implements Protocol {

  @Override
  public Packet execute(ByteBuffer buffer, SelectionKey key) throws InGameException {
    log.info("oi");
    return new Packet();
  }

}
