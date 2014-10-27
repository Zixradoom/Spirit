package com.s2d.spirit.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

final class Sender implements Runnable, AutoCloseable
{
  private final MulticastClient client;
  private final BlockingQueue < DatagramPacket > packets;

  private volatile boolean run = true;

  public Sender ( MulticastClient client )
  {
    this.client = Objects.requireNonNull ( client );
    this.packets = new LinkedBlockingQueue < DatagramPacket > ();
  }

  @Override
  public void run ()
  {
    try ( DatagramSocket socket = new DatagramSocket () )
    {
      while ( run )
      {
        DatagramPacket packet = packets.poll ( 5, TimeUnit.SECONDS );
        if ( packet != null )
          socket.send ( packet );
      }
    }
    catch ( InterruptedException | IOException e )
    {
      run = false;
      MulticastClient.LOGGER.catching ( e );
      client.onException ( e );
    }
  }

  @Override
  public void close ()
  {
    run = false;
    client.close ();
  }

  public void send ( DatagramPacket packet )
  {
    if ( !run )
      throw new IllegalStateException ( "Closed" );
    if ( packet != null )
      packets.offer ( packet );
  }
}