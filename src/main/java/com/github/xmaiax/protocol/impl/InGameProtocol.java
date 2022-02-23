package com.github.xmaiax.protocol.impl;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import org.springframework.stereotype.Component;

import com.github.xmaiax.exception.InGameException;
import com.github.xmaiax.packet.Packet;
import com.github.xmaiax.protocol.Protocol;

import lombok.extern.slf4j.Slf4j;

@Component @Slf4j
public class InGameProtocol implements Protocol {
  
  @Override
  public Packet execute(ByteBuffer buffer, SelectionKey key) throws InGameException {
    log.info("oi");
    return null;
  }
  
}
