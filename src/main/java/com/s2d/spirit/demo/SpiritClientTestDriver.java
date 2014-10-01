package com.s2d.spirit.demo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.s2d.cognition.SystemPropsLoader;
import com.s2d.spirit.Client;
import com.s2d.spirit.SpiritUtils;
import com.s2d.spirit.event.BinMessageEvent;
import com.s2d.spirit.event.ClientListener;
import com.s2d.spirit.event.CloseEvent;
import com.s2d.spirit.event.MessageEvent;
import com.s2d.spirit.event.PingEvent;
import com.s2d.spirit.event.PongEvent;
import com.s2d.spirit.event.TimeoutEvent;

public final class SpiritClientTestDriver implements ClientListener, Runnable
{
	private static final Logger LOGGER = LogManager.getLogger ( SpiritClientTestDriver.class );

	private final Client client;

	public SpiritClientTestDriver () throws UnknownHostException
	{
		client = new Client ( InetAddress.getByName ( "localhost" ), 19981 );
		client.addClientListener ( this );
	}

	@Override
	public void run ()
	{
		String message = "Hello";
		LOGGER.printf ( Level.INFO,
				"Client [%d] sending message [%s]",
				Long.valueOf ( client.getId () ), message );
		client.send ( message );
		LOGGER.printf ( Level.INFO,
				"Client [%d] message sent",
				Long.valueOf ( client.getId () ), message );

		LOGGER.printf ( Level.INFO,
				"Client [%d] sleeping",
				Long.valueOf ( client.getId () ), message );
		try
		{
			Thread.sleep ( 5000 );
		}
		catch ( InterruptedException e )
		{
			LOGGER.catching ( e );
		}
		LOGGER.printf ( Level.INFO,
				"Client [%d] done sleeping",
				Long.valueOf ( client.getId () ), message );
	}

	@Override
	public void onClose ( CloseEvent closeEvent )
	{
		LOGGER.printf ( Level.INFO,
				"Client [%d] has closed",
				closeEvent.getClient ().getIdLong () );
	}

	@Override
	public void onMessage ( MessageEvent messageEvent )
	{
		LOGGER.printf ( Level.INFO,
				"Client [%d] recieved [%s]",
				messageEvent.getClient ().getIdLong (),
				messageEvent.getMessage () );
	}
	
	@Override
	public void onBinMessage ( BinMessageEvent binEvent )
	{
		LOGGER.printf ( Level.INFO,
				"Client [%d] recieved [%s]",
				binEvent.getClient ().getIdLong (),
				SpiritUtils.byteArrayToString ( binEvent.getData () ) );
	}

	@Override
	public void onTimeout ( TimeoutEvent timeoutEvent )
	{
		LOGGER.printf ( Level.INFO,
				"Client [%d] has timed out while waiting",
				timeoutEvent.getClient ().getIdLong () );
	}

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws UnknownHostException 
	 */
	public static void main ( String[] args ) throws InterruptedException, UnknownHostException
	{
		SystemPropsLoader.init ();
		ExecutorService pool = Executors.newCachedThreadPool ();
		for ( int index = 0; index < 1; index++ )
		{
			SpiritClientTestDriver clientTestDriver = new SpiritClientTestDriver ();
			pool.execute ( clientTestDriver );
		}

		pool.shutdown ();

		while ( !pool.awaitTermination ( 1, TimeUnit.MINUTES ) );
	}

  @Override
  public void onPing ( PingEvent pingEvent )
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onPong ( PongEvent pongEvent )
  {
    // TODO Auto-generated method stub
    
  }
}
