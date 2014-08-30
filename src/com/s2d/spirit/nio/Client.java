package com.s2d.spirit.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Objects;

final class Client implements Runnable
{
	private static final Charset CHARSET = Charset.forName ( "UTF-8" ); 
	private static final int DEFAULT_BUFFER_SIZE = 4 * 1024;

	private final ReadableByteChannel in;
	private final WritableByteChannel out;
	private final ByteBuffer inBuffer;
	private final ByteBuffer outBuffer;
	private final CharsetEncoder encoder = CHARSET.newEncoder ();
	private final CharsetDecoder decoder = CHARSET.newDecoder ();

	public Client ( ReadableByteChannel in, WritableByteChannel out )
	{
		this.in = Objects.requireNonNull ( in, "in is null" );
		this.out = Objects.requireNonNull ( out, "out is null" );
		this.inBuffer = ByteBuffer.allocate ( DEFAULT_BUFFER_SIZE );
		this.outBuffer = ByteBuffer.allocate ( DEFAULT_BUFFER_SIZE );
	}

	@Override
	public void run ()
	{
		int readBytes = 0;
		try
		{
			while ( ( readBytes = in.read ( inBuffer ) ) != -1 )
			{
				inBuffer.flip ();

				if ( inBuffer.hasRemaining () )
				{
					CharBuffer charBuffer = CHARSET.decode ( inBuffer );
					System.out.println ( charBuffer.toString () );
				}

				if ( inBuffer.hasRemaining () )
					inBuffer.compact ();
				else
					inBuffer.clear ();
			}
		}
		catch ( IOException e )
		{
			e.printStackTrace ();
		}
	}

	public void send ( String message )
	{
		if ( message != null && !message.isEmpty () )
		{
			try
			{
				CharBuffer charBuffer = CharBuffer.wrap ( message );
				CoderResult result = null;
				do
				{
					result = encoder.encode ( charBuffer, outBuffer, false );
					outBuffer.flip ();
					out.write ( outBuffer );
					
				}
				while ( result != CoderResult.UNDERFLOW );
				
			}
			catch ( IOException e )
			{
				e.printStackTrace ();
			}
		}
	}
}
