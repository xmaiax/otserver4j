package com.github.xmaiax.protocol.impl;

import java.nio.ByteBuffer;

import org.springframework.stereotype.Component;

import com.github.xmaiax.errors.LoginException;
import com.github.xmaiax.packet.Packet;
import com.github.xmaiax.protocol.Protocol;

import lombok.extern.slf4j.Slf4j;

@Component @Slf4j
public class InGameProtocol implements Protocol {
  
  @Override
  public Packet execute(ByteBuffer buffer) throws LoginException {
    log.info("oi");
    return null;
  }
  
}
