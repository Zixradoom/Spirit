package com.s2d.spirit.event;

import java.net.SocketTimeoutException;

import com.s2d.spirit.Client;

public final class TimeoutEvent extends SpiritEvent
{
	private final SocketTimeoutException socketTimeoutException;
	
	public TimeoutEvent ( long timestamp, Client source, SocketTimeoutException ste )
	{
		super ( timestamp, source, Type.TIMEOUT );
		socketTimeoutException = ste;
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
	
	public SocketTimeoutException getSTE ()
	{
		return socketTimeoutException;
	}
}
