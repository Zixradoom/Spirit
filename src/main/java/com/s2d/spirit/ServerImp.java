package com.s2d.spirit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class ServerImp extends Thread implements AutoCloseable
{
	private final static Logger LOGGER = LogManager.getLogger ( ServerImp.class );
	
	private volatile boolean run = true;
	
	private final Server server;
	private final ServerSocket serverSocket;

	public ServerImp ( Server server )
	{
		this.server = server;
		this.serverSocket = server.getServerSocket ();
		this.setName ( String.format ( "Sever-%03d-Thread", server.getIdLong () ) );
		this.start ();
	}

	@Override
	public void run ()
	{
		try
		{
			LOGGER.printf ( Level.DEBUG, "Server [%d] runing on port [%d]",
					server.getIdLong (),
					Integer.valueOf ( serverSocket.getLocalPort () ) );
			while ( run )
			{
				try
				{
					Socket socket = serverSocket.accept ();
					LOGGER.printf ( Level.DEBUG,
							"Server [%d] recieved connection from [%s]",
							server.getIdLong (),
							socket.getRemoteSocketAddress () );
					if ( server.isClosed () )
					{
						socket.close ();
						LOGGER.printf ( Level.DEBUG,
								"Server [%d] refused connection from [%s]. Server is closed",
								server.getIdLong (),
								socket.getRemoteSocketAddress () );
					}
					else
					{
						server.onConnect ( socket );
					}
				}
				catch ( SocketTimeoutException ste )
				{
					server.onTimeout ( ste );
				}
			}
			serverSocket.close ();
			LOGGER.printf ( Level.DEBUG,
					"Server [%d] on port [%d] has closed",
					server.getIdLong (),
					Integer.valueOf ( serverSocket.getLocalPort () ) );
		}
		catch ( IOException e )
		{
			LOGGER.catching ( Level.WARN, e );
		}
		finally
		{
			server.close ();
			try
			{
				serverSocket.close ();
			}
			catch ( IOException e )
			{
				LOGGER.catching ( e );
			}
		}
	}

	@Override
	public void close ()
	{
		if ( !server.isClosed () )
			server.close ();
		run = false;
	}
}
