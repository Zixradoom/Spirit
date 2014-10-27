package com.s2d.spirit.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Objects;

import org.apache.logging.log4j.Level;

final class Listener implements Runnable, AutoCloseable
{
  private final MulticastClient client;

  private volatile boolean run = true;

  public Listener ( MulticastClient client )
  {
    this.client = Objects.requireNonNull ( client );
  }

  @Override
  public void run ()
  {
    try ( MulticastSocket socket = new MulticastSocket ( null ) )
    {
      socket.setReuseAddress ( true );
      socket.setSoTimeout ( 5000 );
      socket.setLoopbackMode ( false );
      socket.bind ( new InetSocketAddress ( client.getInetSocketAddress ().getPort () ) );
      socket.joinGroup ( client.getInetSocketAddress ().getAddress () );

      while ( run )
      {
        try
        {
          byte[] buf = new byte[ 4096 ];
          DatagramPacket packet = new DatagramPacket ( buf, buf.length );
          socket.receive ( packet );
          client.onRecive ( packet );
        }
        catch ( SocketTimeoutException ste )
        {
          MulticastClient.LOGGER.catching ( Level.TRACE, ste );
        }
      }

      socket.leaveGroup ( client.getInetSocketAddress ().getAddress () );
    }
    catch ( IOException e )
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
}