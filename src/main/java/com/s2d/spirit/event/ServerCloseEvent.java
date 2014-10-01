package com.s2d.spirit.event;

import com.s2d.spirit.Server;

public final class ServerCloseEvent extends SpiritEvent
{
	public ServerCloseEvent ( long timestamp, Server source )
	{
		super ( timestamp, source, Type.SERVER_CLOSE );
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
}
