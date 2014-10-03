package com.s2d.spirit.udp.demo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.s2d.spirit.SpiritUtils;
import com.s2d.spirit.udp.MulticastClient;
import com.s2d.spirit.udp.event.MulticastClientListener;

public final class MulticastDemo implements MulticastClientListener
{
  private static final Logger LOGGER = LogManager.getLogger ( MulticastDemo.class );
  
  private final Timer timer;
  private final MulticastClient client;
  
  public MulticastDemo ( InetSocketAddress address )
  {
    this.timer = new Timer ( true );
    this.client = new MulticastClient ( address );
    client.addListener ( this );
    timer.schedule ( new MulticastClientTask ( client ), 1000, 2000 );
  }

  @Override
  public void onException ( MulticastClient client, Throwable t )
  {
    client.close ();
    LOGGER.catching ( t );
  }

  @Override
  public void onMessage (
      MulticastClient multicastClient, InetAddress remote, String message )
  {
    LOGGER.info ( "Recieved message from [{}][{}]", remote, message );
  }

  @Override
  public void onMessage (
      MulticastClient multicastClient, InetAddress remote, byte[] message )
  {
    LOGGER.info ( "Recieved message from [{}][{}]", remote, SpiritUtils.byteArrayToString ( message ) );
  }

  public static void main ( String[] args )
  {
    
    MulticastDemo md = new MulticastDemo ( new InetSocketAddress ( "239.255.1.1", 56781 ) );
  }
  
  private static final class MulticastClientTask extends TimerTask
  {
    private final MulticastClient client;
    
    private int count = 0;
    private int max = 5;
    
    public MulticastClientTask ( MulticastClient client )
    {
      this.client = Objects.requireNonNull ( client );
    }

    @Override
    public void run ()
    {
      client.send ( "Hello World!!" );
      
      if ( count > max )
      {
        this.cancel ();
        client.close ();
      }
      count++;
    }
  }
}
