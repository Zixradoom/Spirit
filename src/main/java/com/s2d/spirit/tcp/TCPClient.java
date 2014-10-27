package com.s2d.spirit.tcp;

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
import java.net.Socket;
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

public final class TCPClient implements AutoCloseable
{
  private static final Logger LOGGER = LogManager.getLogger ( TCPClient.class );

  private final ExecutorService pool;
  private final InetSocketAddress address;
  private final Listener listener;
  private final Sender sender;
  private final Socket socket;
  private final List < TCPClientListener > listeners;

  private volatile boolean closed = false;
  
  public TCPClient ( InetSocketAddress address ) throws IOException
  {
    this.address = Objects.requireNonNull ( address );
    pool = Executors.newCachedThreadPool ();
    listeners = new CopyOnWriteArrayList < TCPClientListener > ();
    
    try 
    {
      socket = new Socket ();
      socket.setKeepAlive ( true );
      socket.setSoTimeout ( 5000 );
      socket.bind ( address );
      listener = new Listener ( this, socket.getInputStream () );
      sender = new Sender ( this, socket.getOutputStream () );
    }
    catch ( IOException e )
    {
      throw LOGGER.throwing ( new RuntimeException ( e ) );
    }
    
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
      sender.send ( message );
    }
    catch ( IOException e )
    {
      LOGGER.catching ( e );
      throw LOGGER.throwing ( new RuntimeException ( e ) );
    }
  }

  /**
   * Close this network end point. This will close the underlying
   * sockets and also notify listeners assigned to this client.
   */
  @Override
  public void close ()
  {
    if ( !closed )
    {
      closed = true;
      pool.shutdown ();
      sender.close ();
      listener.close ();
      
      for ( TCPClientListener l : listeners )
        l.onClose ( this );
    }
  }

  /**
   * Add a listener to this client
   * @param listener - the listener to add
   */
  public void addListener ( TCPClientListener listener )
  {
    if ( listener != null )
      listeners.add ( listener );
  }

  /**
   * Remove the specified listener from this client
   * @param listener - the listener to be removed
   */
  public void removeListener ( TCPClientListener listener )
  {
    listeners.remove ( listener );
  }
  
  /**
   * Get the Socket Address of this endpoint
   * @return
   */
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
    for ( TCPClientListener mcl : listeners )
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
    for ( TCPClientListener mcl : listeners )
      mcl.onMessage ( this, remote, message );
  }

  private void onBin ( InetAddress remote, byte[] message )
  {
    for ( TCPClientListener mcl : listeners )
      mcl.onMessage ( this, remote, Arrays.copyOf ( message, message.length ) );
  }
}
