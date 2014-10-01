package com.s2d.spirit.udp;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.s2d.spirit.SpiritFlag;
import com.s2d.spirit.exception.SpiritException;

public final class UDPClient implements AutoCloseable
{
  private static final Logger LOGGER = LogManager.getLogger ( UDPClient.class );

  private final InetSocketAddress address;

  public UDPClient ( InetSocketAddress address )
  {
    this.address = Objects.requireNonNull ( address );
  }

  public void send ( byte[] data )
  {
    if ( data != null )
    {
      Arrays.copyOf ( data, data.length );
      this.send ( EnumSet.noneOf ( SpiritFlag.class ), data );
    }
  }

  public void send ( String message )
  {
    if ( message != null )
    {
      try
      {
        this.send ( EnumSet.of ( SpiritFlag.STRING ), message.getBytes ( "UTF-8" ) );
      }
      catch ( UnsupportedEncodingException e )
      {
        LOGGER.throwing ( new SpiritException ( e ) );
      }
    }
  }

  private void send ( Set < SpiritFlag > flags, byte[] data )
  {

  }

  @Override
  public void close () throws Exception
  {
    // TODO Auto-generated method stub

  }
}
