package com.s2d.spirit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.s2d.spirit.exception.SpiritException;

public final class SpiritSocketFactory
{
	public static final String SSL = "com.s2d.spirit.ssl";
	public static final String PORT = "com.s2d.spirit.port";
	public static final String SERVER_TIMEOUT = "com.s2d.spirit.server.timeout";
	public static final String SERVER_ACCEPT_QUEUE = "com.s2d.spirit.server.accept.queue";
	public static final String CLIENT_TIMEOUT = "com.s2d.spirit.client.timeout";
	public static final String HOST = "com.s2d.spirit.host";
	public static final boolean SSL_DEFAULT = false;
	public static final int     PORT_DEFAULT = 19981;
	public static final int     SERVER_TIMEOUT_DEFAULT = 1000;
	public static final int     SERVER_ACCEPT_QUEUE_DEFAULT = 50;
	public static final int     CLIENT_TIMEOUT_DEFAULT = 1000;
	public static final String  HOST_DEFAULT = "localhost";

	public static ServerSocket newServerSocket ( int port ) throws SpiritException
	{
		boolean ssl = Boolean.valueOf ( ( System.getProperty ( SSL, Boolean.toString ( SSL_DEFAULT ) ) ) ).booleanValue ();
		if ( ssl )
		{
			try
			{
				ServerSocketFactory serverSocketfactory = SSLServerSocketFactory.getDefault ();
				return serverSocketfactory.createServerSocket ( port );
			}
			catch ( IOException e )
			{
				throw new SpiritException ( e.getMessage (), e );
			}
		}
		else
		{
			try
			{
				return new ServerSocket ( port );
			}
			catch ( IOException e )
			{
				throw new SpiritException ( e.getMessage (), e );
			}
		}
	}

	public static Socket newSocket ( InetAddress address, int port ) throws SpiritException
	{
		boolean ssl = Boolean.valueOf ( ( System.getProperty ( SSL, Boolean.toString ( SSL_DEFAULT ) ) ) ).booleanValue ();
		if ( ssl )
		{
			try
			{
				SocketFactory socketfactory = SSLSocketFactory.getDefault ();
				return socketfactory.createSocket ( address, port );
			}
			catch ( IOException e )
			{
				throw new SpiritException ( e.getMessage (), e );
			}
		}
		else
		{
			try
			{
				return new Socket ( address, port );
			}
			catch ( IOException e )
			{
				throw new SpiritException ( e.getMessage (), e );
			}
		}
	}
}
