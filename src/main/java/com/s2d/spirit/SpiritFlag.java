package com.s2d.spirit;

public enum SpiritFlag
{
	STRING ( ( short ) 0x2 ),
	PING   ( ( short ) 0x4 ),
	PONG   ( ( short ) 0x8 );
	
	private final int flagValue;
	
	private SpiritFlag ( short flag )
	{
		flagValue = 0x0000FFFF & flag;
	}
	
	public int getFlagValue ()
	{
		return flagValue;
	}
}
