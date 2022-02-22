package com.github.xmaiax.protocol.impl;

import java.nio.ByteBuffer;

import org.springframework.stereotype.Component;

import com.github.xmaiax.errors.OTJException;
import com.github.xmaiax.packet.Packet;
import com.github.xmaiax.protocol.TibiaProtocol;

import lombok.extern.slf4j.Slf4j;

@Component @Slf4j
public class LoginSuccessProtocol implements TibiaProtocol {

  @Override
  public Packet executeProtocol(ByteBuffer buffer) throws OTJException {
    log.info("Successful login attemp!");
    return null;
  }

}
