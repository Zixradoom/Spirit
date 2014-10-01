package com.s2d.spirit;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.s2d.spirit.event.ConnectEvent;
import com.s2d.spirit.event.ServerCloseEvent;
import com.s2d.spirit.event.ServerListener;
import com.s2d.spirit.event.ServerTimeoutEvent;
import com.s2d.spirit.exception.SpiritException;

public final class Server implements AutoCloseable
{
	private static final Logger LOGGER = LogManager.getLogger ( Server.class );
	private static final AtomicLong ID_SEED = new AtomicLong ();
	
	private volatile boolean closed = false;
	private volatile ServerListener listener = null;
	
	private final long id;
	private final ServerSocket serverSocket;
	private final ServerImp serverImp;
	
	public Server ( int port )
	{
		this ( port, 0 );
	}
	
	public Server ( int port, int timeout ) throws SpiritException
	{
		id = ID_SEED.incrementAndGet ();
		serverSocket = SpiritSocketFactory.newServerSocket ( port );
		try
		{
			serverSocket.setSoTimeout ( timeout );
		}
		catch ( SocketException e )
		{
			throw new SpiritException ( e.getMessage (), e );
		}
		LOGGER.printf ( Level.INFO, "Server [%d] on port [%d] running",
				this.getIdLong (), Integer.valueOf ( serverSocket.getLocalPort () ) );
		serverImp = new ServerImp ( this );
	}
	
	@Override
	public void close ()
	{
		if ( !closed )
		{
			LOGGER.printf ( Level.INFO, "Server [%d] has closed", this.getIdLong () );
			ServerListener l = getServerListener ();
			// Immutable, Only need 1
			ServerCloseEvent event = new ServerCloseEvent ( System.nanoTime (), this ); 
			if ( l != null )
				l.onClose ( event );
		}
		closed = true;
		serverImp.close ();
	}
	
	public boolean isClosed ()
	{
		return closed;
	}
	
	public ServerListener getServerListener ()
	{
		return listener;
	}
	
	public void setServerListener ( ServerListener serverListener )
	{
		this.listener = serverListener;
	}
	
	public long getId ()
	{
		return id;
	}
	
	public Long getIdLong ()
	{
		return Long.valueOf ( id );
	}
	
	void onConnect ( Socket socket )
	{
		ServerListener l = getServerListener ();
		if ( l != null )
			l.onConnect ( new ConnectEvent ( System.nanoTime (), this, new Client ( socket ) ) );
	}

	void onTimeout ( SocketTimeoutException ste )
	{
		ServerListener l = getServerListener ();
		if ( l != null )
			l.onTimeout ( new ServerTimeoutEvent ( System.nanoTime (), this, ste ) );
	}

	ServerSocket getServerSocket ()
	{
		return serverSocket;
	}
}
