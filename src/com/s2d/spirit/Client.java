package com.s2d.spirit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.s2d.spirit.event.BinMessageEvent;
import com.s2d.spirit.event.ClientListener;
import com.s2d.spirit.event.CloseEvent;
import com.s2d.spirit.event.MessageEvent;
import com.s2d.spirit.event.PingEvent;
import com.s2d.spirit.event.PongEvent;
import com.s2d.spirit.event.TimeoutEvent;
import com.s2d.spirit.event.Type;
import com.s2d.spirit.exception.SpiritException;

/**
 * This class represents a network end point
 * @author Zixradoom
 *
 */
public final class Client implements AutoCloseable
{
	private static final Logger LOGGER = LogManager.getLogger ( ClientImp.class );
	private static final AtomicLong ID_SEED = new AtomicLong ();

	private volatile boolean closed = false;
	
	private final List < ClientListener > listeners;
	private final ClientImp clientImp;
	private final Socket socket;
	private final long id;
	private final InetAddress inetAddress;
	private final InetAddress inetAddressRemote;

	/**
	 * Create a new Client attempting to connect to Host and Port.
	 * @param address
	 * @param port
	 */
	public Client ( InetAddress address, int port )
	{
		this ( SpiritSocketFactory.newSocket ( address, port ) );
	}
	
	/**
	 * Create a client by passing in a already connected Socket
	 * @param socket
	 * @throws SpiritException
	 */
	Client ( Socket socket ) throws SpiritException
	{
		try
		{
			this.listeners = new CopyOnWriteArrayList < ClientListener > ();
			this.socket = socket;
			this.id = ID_SEED.incrementAndGet ();
			this.clientImp = new ClientImp ( this, 0 );
			this.inetAddress = this.socket.getLocalAddress ();
			this.inetAddressRemote = this.socket.getInetAddress ();
		}
		catch ( IOException e )
		{
			LOGGER.catching ( e );
			throw LOGGER.throwing ( new SpiritException ( e.getMessage (), e ) );
		}
	}

	public void sendPing ()
	{
	  if ( closed )
      throw LOGGER.throwing ( new IllegalStateException ( "Client is closed" ) );
	  
	  clientImp.send ( 2, EnumSet.of ( SpiritFlag.PING ), new byte[ 0 ] );
	}
	
	public void sendPong ()
  {
    if ( closed )
      throw LOGGER.throwing ( new IllegalStateException ( "Client is closed" ) );
    
    clientImp.send ( 2, EnumSet.of ( SpiritFlag.PONG ), new byte[ 0 ] );
  }
	
	/**
	 * Send a block of binary data to the remote end point
	 * @param message
	 */
	public void send ( byte[] message )
	{
		if ( closed )
			throw LOGGER.throwing ( new IllegalStateException ( "Client is closed" ) );
		if ( message == null )
			throw LOGGER.throwing ( new NullPointerException ( "Message is null" ) );

		LOGGER.trace ( "Message [{}][{}]",
				Integer.valueOf ( message.length ),
				SpiritUtils.byteArrayToString ( message ) );
		clientImp.send ( 2, EnumSet.noneOf ( SpiritFlag.class ), message );
	}
	
	/**
	 * Send a block of character data to the remote end point 
	 * @param message
	 */
	public void send ( String message )
	{
		if ( closed )
			throw LOGGER.throwing ( new IllegalStateException ( "Client is closed" ) );
		if ( message == null )
			throw LOGGER.throwing ( new NullPointerException ( "Message is null" ) );
		LOGGER.trace ( "Sending Message [{}]", message );
		clientImp.send ( 2, EnumSet.of ( SpiritFlag.STRING ), message.getBytes ( ClientImp.CHARSET ) );
	}
	
	/**
	 * Close this Client. This operation is idempotent.
	 */
	@Override
	public void close () throws SpiritException
	{
		if ( !closed )
		{
			LOGGER.info ( "Client [{}] has been closed", this.getIdLong () );
			// Immutable, only need 1
			CloseEvent event = new CloseEvent ( System.nanoTime (), this ); 
			for ( ClientListener l : listeners )
				l.onClose ( event );
		}
		closed = true;
		clientImp.close ();
	}

	/**
	 * Returns true if and only if this Client is closed.
	 * @return
	 */
	public boolean isClosed ()
	{
		return closed;
	}
	
	/**
	 * Get the creation ID of this client.
	 * @return
	 */
	public long getId ()
	{
		return id;
	}
	
	/**
	 * Get the creation ID as a <code>Long</code>.
	 * @return
	 */
	public Long getIdLong ()
	{
		return Long.valueOf ( id );
	}

	/**
	 * Add a listener to this client to receive Client events. 
	 * @param clientListener
	 */
	public void addClientListener ( ClientListener clientListener )
	{
		if ( clientListener == null )
			throw LOGGER.throwing ( new NullPointerException () );
		
		listeners.add ( clientListener );
	}
	
	/**
	 * Remove the supplied listener if it exists.
	 * @param clientListener
	 */
	public void removeClientListener ( ClientListener clientListener )
	{
		listeners.remove ( clientListener );
	}
	
	/**
	 * Remove all listeners
	 */
	public void clearClientListeners ()
	{
		listeners.clear ();
	}
	
	/**
	 * Get a list of listeners on this Client.
	 * @return
	 */
	public List < ClientListener > getClientListeners ()
	{
		return Collections.unmodifiableList ( listeners );
	}
	
	@Deprecated
	public void setClientListener ( ClientListener clientListener )
	{
		this.addClientListener ( clientListener );
	}
	
	@Deprecated
	public ClientListener getClientListener ()
	{
		return null;
	}
	
	public InetAddress getInetAddressLocal ()
	{
		return inetAddress;
	}
	
	public InetAddress getInetAddressRemote ()
	{
		return inetAddressRemote;
	}
	
	/**
	 * Get the underlying <code>Socket</code>.
	 * @return
	 */
	Socket getSocket ()
	{
		return socket;
	}

	void onBin ( byte[] data )
	{
		LOGGER.trace ( "Client [{}] Recieved Message [{}][{}]",
				this.getIdLong (),
				Integer.valueOf ( data.length ),
				SpiritUtils.byteArrayToString ( data ) );
		// The byte array is not immutable so we have to create a new message to
		// send to each listener
		long ts = System.nanoTime ();
		for ( ClientListener l : listeners )
			l.onBinMessage ( new BinMessageEvent ( ts, this, data ) );
	}
	
	void onMessage ( String message )
	{
		LOGGER.trace ( "Client [{}] Recieved Message [{}]",
				this.getIdLong (),
				message );
		// Strings are immutable so we only have to create 1 message object
		MessageEvent event = new MessageEvent ( System.nanoTime (), this, message );
		for ( ClientListener l : listeners )
			l.onMessage ( event );
	}

	void onTimeout ( SocketTimeoutException ste )
	{
		LOGGER.trace ( "Client [{}] has timed out waiting for a message",
				this.getIdLong () );
		// Immutable so we only need 1
		TimeoutEvent event = new TimeoutEvent ( System.nanoTime (), this, ste );
		for ( ClientListener l : listeners )
			l.onTimeout ( event );
	}
	
	void onPing ()
	{
	  LOGGER.trace ( "Client [{}] Recieved [{}]",
        this.getIdLong (),
        Type.PING );
	  
	  PingEvent event = new PingEvent ( System.nanoTime (), this );
	  for ( ClientListener l : listeners )
      l.onPing ( event );
	}
	
	void onPong ()
	{
	  LOGGER.trace ( "Client [{}] Recieved [{}]",
        this.getIdLong (),
        Type.PONG );
	  
	  PongEvent event = new PongEvent ( System.nanoTime (), this );
    for ( ClientListener l : listeners )
      l.onPong ( event );
	}
}
