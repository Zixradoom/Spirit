package com.s2d.spirit.event;

import java.util.Arrays;

import com.s2d.spirit.Client;

/**
 * 
 * @author Zixradoom
 *
 */
public final class BinMessageEvent extends SpiritEvent
{
    private final byte[] message;
	
	public BinMessageEvent ( long timestamp, Client source, byte[] message )
	{
		super ( timestamp, source, Type.BINNARY );
		if ( message == null )
			throw new NullPointerException ( "Bin Data is null" );
		
		this.message = Arrays.copyOf ( message, message.length );
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
	
	public byte[] getData ()
	{
		return message;
	}
}
