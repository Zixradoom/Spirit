package com.s2d.spirit.event;

public abstract class SpiritEvent
{
	private final long timestamp;
	private final Type type;
	protected final Object source;
	
	protected SpiritEvent ( long timestamp, Object source, Type type )
	{
		if ( source == null )
			throw new NullPointerException ( "Source is null" );
		if ( type == null )
			throw new NullPointerException ( "Type is null" );
		this.timestamp = timestamp;
		this.source = source;
		this.type = type;
	}
	
	public final long getTimestamp ()
	{
		return timestamp;
	}
	
	public final Type getType ()
	{
		return type;
	}
	
	public Object getSource ()
	{
		return source;
	}
}
