package com.s2d.spirit;

import java.nio.ByteBuffer;
import java.util.Collection;

public final class SpiritUtils
{
	/**
	 * Takes an array of bytes and converts it to a comma
	 * separated string of Hex encoded bytes. This is a 
	 * read only operation, it does not modify the array.
	 * @param data - array of byte data
	 * @return A string representation of the data
	 */
	public static String byteArrayToString ( byte[] data )
	{
		StringBuilder builder = new StringBuilder ( data.length * 2 );
		
		for ( byte b : data )
		{
			String formatted = String.format ( "%02X", Byte.valueOf ( b ) );
			builder.append ( formatted ).append ( ',' );
		}
		
		return builder.toString ();
	}
	
	/**
	 * Takes a ByteBuffer in read mode and converts it to a
	 * comma separated string of Hex encoded bytes. This is a
	 * read only operation. When this operation is complete the
	 * ByteBuffer will be rewound.
	 * @param buffer - Byte Data Buffer
	 * @return A string representation of the data
	 */
	public static String byteBufferToString ( ByteBuffer buffer )
	{
		StringBuilder builder = new StringBuilder ( buffer.remaining () * 2 );
		
		while ( buffer.hasRemaining () )
		{
			String formatted = String.format ( "%02X", Byte.valueOf ( buffer.get () ) );
			builder.append ( formatted ).append ( ',' );
		}
		
		buffer.rewind ();
		
		return builder.toString ();
	}
	
	/**
	 * Turn a collections of <code>SpiritFlag</code> into a string.
	 * @param flags
	 * @return
	 */
	public static String flagsToString( Collection < SpiritFlag > flags )
	{
		StringBuilder builder = new StringBuilder();
		for ( SpiritFlag flag : flags )
			builder.append ( flag.toString () );
		return builder.toString ();
	}
}
