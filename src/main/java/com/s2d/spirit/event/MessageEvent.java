package com.s2d.spirit.event;

import com.s2d.spirit.Client;

public final class MessageEvent extends SpiritEvent
{
	private final String message;
	
	public MessageEvent ( long timestamp, Client source, String message )
	{
		super ( timestamp, source, Type.MESSAGE );
		if ( message == null )
			throw new NullPointerException ( "Message is null" );
		
		this.message = message;
	}
	
	@Override
	public Client getSource ()
	{
		return Client.class.cast ( super.getSource () );
	}
	
	public Client getClient ()
	{
		return getSource ();
	}
	
	public String getMessage ()
	{
		return message;
	}
}
