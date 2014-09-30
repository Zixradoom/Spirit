package com.s2d.spirit.test;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.s2d.spirit.Client;
import com.s2d.spirit.SpiritUtils;
import com.s2d.spirit.event.BinMessageEvent;
import com.s2d.spirit.event.ClientListener;
import com.s2d.spirit.event.CloseEvent;
import com.s2d.spirit.event.MessageEvent;
import com.s2d.spirit.event.PingEvent;
import com.s2d.spirit.event.PongEvent;
import com.s2d.spirit.event.TimeoutEvent;

public final class EchoHandler implements ClientListener
{
	private static final Logger LOGGER = LogManager.getLogger ( EchoHandler.class );
	
	private final Client client;
	
	public EchoHandler ( Client client )
	{
		if ( client == null )
			throw new NullPointerException ( "Client is null" );
		
		this.client = client;
		this.client.addClientListener ( this );
	}
	
	@Override
	public void onClose ( CloseEvent closeEvent )
	{
		LOGGER.printf ( Level.INFO,
				"EchoClient [%s] has closed",
				closeEvent.getClient ().getIdLong () );
	}

	@Override
	public void onMessage ( MessageEvent messageEvent )
	{
		String message = messageEvent.getMessage (); 
		LOGGER.printf ( Level.INFO,
				"EchoClient [%d] has revieved the message [%s]",
				messageEvent.getClient ().getIdLong () ,
				message );
		messageEvent.getClient ().send ( message );
		messageEvent.getClient ().close ();
	}
	
	@Override
	public void onBinMessage ( BinMessageEvent binEvent )
	{
		byte[] data = binEvent.getData ();
		LOGGER.printf ( Level.INFO, 
				"EchoClient [%d] has revieved the message [%s]",
				binEvent.getClient ().getIdLong (),
				SpiritUtils.byteArrayToString ( data ) );
		binEvent.getClient ().send ( data );
		binEvent.getClient ().close ();
	}

	@Override
	public void onTimeout ( TimeoutEvent timeoutEvent )
	{
		LOGGER.printf ( Level.INFO,
				"EchoClient [%d] has timed out while waiting for data",
				timeoutEvent.getClient ().getIdLong () );
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
