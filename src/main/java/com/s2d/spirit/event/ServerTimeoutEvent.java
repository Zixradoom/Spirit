package com.s2d.spirit.event;

import java.net.SocketTimeoutException;

import com.s2d.spirit.Server;

public final class ServerTimeoutEvent extends SpiritEvent
{
	private final SocketTimeoutException socketTimeoutException;

	public ServerTimeoutEvent ( long timestamp, Server source, SocketTimeoutException ste )
	{
		super ( timestamp, source, Type.SERVER_TIMEOUT );

		if ( ste == null )
			throw new NullPointerException ( "STE is null" );
		socketTimeoutException = ste;
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

	public SocketTimeoutException getSTE ()
	{
		return socketTimeoutException;
	}
}
