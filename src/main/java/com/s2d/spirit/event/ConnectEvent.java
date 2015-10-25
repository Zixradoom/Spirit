package com.s2d.spirit.event;

import com.s2d.spirit.Client;
import com.s2d.spirit.Server;

public final class ConnectEvent extends SpiritEvent
{
	private Client client;
	
	public ConnectEvent ( long timestamp, Server source, Client client )
	{
		super ( timestamp, source, Type.CONNECT );
		if ( client == null )
			throw new NullPointerException ( "Client is null" );
		
		this.client = client;
	}

	@Override
	public Server getSource ()
	{
		return Server.class.cast ( super.getSource () );
	}
	
	public Server getServer ()
	{
		return getSource ();
	}
	
	public Client getClient ()
	{
		return client;
	}
}
