package com.s2d.spirit.event;

import com.s2d.spirit.Client;

public final class CloseEvent extends SpiritEvent
{
	public CloseEvent ( long timestamp, Client source )
	{
		super ( timestamp, source, Type.CLOSE );
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
}
