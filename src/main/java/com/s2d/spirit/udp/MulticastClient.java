package com.s2d.spirit.udp;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.s2d.spirit.SpiritFlag;
import com.s2d.spirit.exception.SpiritException;
import com.s2d.spirit.udp.event.MulticastClientListener;

public final class MulticastClient implements AutoCloseable
{
  static final Logger LOGGER = LogManager.getLogger ( MulticastClient.class );

  private final ExecutorService pool;
  private final InetSocketAddress address;
  private final Listener listener;
  private final Sender sender;
  private final List < MulticastClientListener > listeners;

  private volatile boolean closed = false;
  
  /**
   * A <code>MulticastClient</code> listens to and sends to the
   * multicast group specified by the {@link InetSocketAddress}
   * passed to this constructor.
   * @param address the multicast group IP address and port
   */
  public MulticastClient ( InetSocketAddress address )
  {
    this.address = Objects.requireNonNull ( address );
    pool = Executors.newCachedThreadPool ();
    listeners = new CopyOnWriteArrayList < MulticastClientListener > ();
    listener = new Listener ( this );
    sender = new Sender ( this );
    pool.execute ( listener );
    pool.execute ( sender );
  }

  /**
   * Send binary message to the group.
   * @param data binary data to send
   */
  public void send ( byte[] data )
  {
    if ( data != null )
    {
      byte[] copy = Arrays.copyOf ( data, data.length );
      this.send ( EnumSet.noneOf ( SpiritFlag.class ), copy );
    }
  }

  /**
   * Send a {@link String} message to the group.
   * @param message character data to send
   */
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
    try ( ByteArrayOutputStream baos = new ByteArrayOutputStream (); 
        DataOutputStream dos = new DataOutputStream ( new BufferedOutputStream ( baos ) ) )
    {
      dos.writeLong ( 1L ); // magic number
      
      int fields = 0x0;
      for ( SpiritFlag f : flags )
        fields = fields | f.getFlagValue ();

      dos.writeShort ( 0x0000FFFF & fields ); // flags
      dos.writeInt ( data.length );
      dos.write ( data );
      dos.flush ();
      byte[] message = baos.toByteArray ();
      sender.send ( new DatagramPacket ( message, message.length, this.getInetSocketAddress () ) );
    }
    catch ( IOException e )
    {
      LOGGER.catching ( e );
      throw LOGGER.throwing ( new RuntimeException ( e ) );
    }
  }

  /**
   * Close this network endpoint
   */
  @Override
  public void close ()
  {
    if ( !closed )
    {
      closed = true;
      pool.shutdown ();
      listener.close ();
      sender.close ();
      
      for ( MulticastClientListener l : listeners )
        l.onClose ( this );
    }
  }

  public void addListener ( MulticastClientListener listener )
  {
    if ( listener != null )
      listeners.add ( listener );
  }

  public InetSocketAddress getInetSocketAddress ()
  {
    return address;
  }

  protected ExecutorService getThreadPool ()
  {
    return pool;
  }

  protected void onException ( Throwable t )
  {
    for ( MulticastClientListener mcl : listeners )
      mcl.onException ( this, t );

    this.close ();
  }

  protected void onRecive ( DatagramPacket packet )
  {
    try ( DataInputStream dis = new DataInputStream ( new ByteArrayInputStream ( packet.getData () ) ) )
    {
      dis.readLong (); // magic number
      short flags = dis.readShort ();
      int dataLength = dis.readInt ();
      byte[] data = new byte[ dataLength ];
      dis.read ( data );
      
      if ( ( flags & SpiritFlag.STRING.getFlagValue () ) > 0 )
        this.onString ( packet.getAddress (), new String ( data, "UTF-8" ) );
      else
        this.onBin ( packet.getAddress (), data );
    }
    catch ( IOException e )
    {
      LOGGER.catching ( e );
      this.onException ( e );
    }
  }
  
  private void onString ( InetAddress remote, String message )
  {
    for ( MulticastClientListener mcl : listeners )
      mcl.onMessage ( this, remote, message );
  }

  private void onBin ( InetAddress remote, byte[] message )
  {
    for ( MulticastClientListener mcl : listeners )
      mcl.onMessage ( this, remote, Arrays.copyOf ( message, message.length ) );
  }
}
