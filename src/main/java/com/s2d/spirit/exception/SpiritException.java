package com.s2d.spirit.exception;

public class SpiritException extends RuntimeException
{
	private static final long serialVersionUID = -5767826550628529171L;

	public SpiritException ()
	{
		super ();
	}
	
	public SpiritException ( String message )
	{
		super ( message );
	}
	
	public SpiritException ( Throwable t )
	{
		super ( t );
	}
	
	public SpiritException ( String message, Throwable t )
	{
		super ( message, t );
	}
}
