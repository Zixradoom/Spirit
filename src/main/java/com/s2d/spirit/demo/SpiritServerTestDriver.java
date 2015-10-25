package com.s2d.spirit.demo;

import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.s2d.cognition.SystemPropsLoader;
import com.s2d.spirit.Server;
import com.s2d.spirit.event.ConnectEvent;
import com.s2d.spirit.event.ServerCloseEvent;
import com.s2d.spirit.event.ServerListener;
import com.s2d.spirit.event.ServerTimeoutEvent;

public final class SpiritServerTestDriver implements ServerListener
{
	private static final Logger LOGGER = LogManager.getLogger ( SpiritServerTestDriver.class );
	
	private final Server server;
	
	public SpiritServerTestDriver ()
	{
		server = new Server ( 19981, 2500 );
		server.setServerListener ( this );
	}
	
	@Override
	public void onConnect ( ConnectEvent connectEvent )
	{
		LOGGER.printf ( Level.INFO,
				"Server [%d] recieved connection",
				connectEvent.getServer ().getIdLong () );
		new EchoHandler ( connectEvent.getClient () );
	}

	@Override
	public void onTimeout ( ServerTimeoutEvent serverTimeoutEvent )
	{
		LOGGER.printf ( Level.INFO,
				"Server [%d] has timed out",
				serverTimeoutEvent.getServer ().getIdLong () );
	}

	@Override
	public void onClose ( ServerCloseEvent serverCloseEvent )
	{
		LOGGER.printf ( Level.INFO,
				"Server [%d] has closed",
				serverCloseEvent.getServer ().getIdLong () );
	}	
	
	public void close ()
	{
		server.close ();
	}

	public static void main ( String[] args )
	{
		SystemPropsLoader.init ();
		SpiritServerTestDriver serverTestDriver = new SpiritServerTestDriver ();
		
		System.out.println ( "Hit 'Enter' to exit" );
		try
		{
			System.in.read ();
		}
		catch ( IOException e )
		{
			LOGGER.catching ( e );
		}
		
		serverTestDriver.close ();
	}
}
