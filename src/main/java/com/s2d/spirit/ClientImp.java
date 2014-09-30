package com.s2d.spirit;

import static com.s2d.spirit.SpiritConstants.SPIRIT_CHARSET;
import static com.s2d.spirit.SpiritConstants.SPIRIT_CHARSET_DEFAUTL;
import static com.s2d.spirit.SpiritConstants.SPIRIT_MAGIC_NUMBER;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.s2d.spirit.event.Type;
import com.s2d.spirit.exception.SpiritException;

final class ClientImp extends Thread implements AutoCloseable
{
	private static final Logger LOGGER = LogManager.getLogger ( ClientImp.class );
	public static final Charset CHARSET = ClientImp.initCharset ();

	private final Client client;
	private final Socket socket;
	private final DataInputStream inputStream;
	private final DataOutputStream outputStream;

	public ClientImp ( Client client, int timeout ) throws IOException
	{
		this.client = client;
		this.socket = client.getSocket ();
		this.socket.setSoTimeout ( timeout );
		this.inputStream = new DataInputStream ( new BufferedInputStream ( socket.getInputStream () ) );
		this.outputStream = new DataOutputStream ( new BufferedOutputStream ( socket.getOutputStream () ) );

		this.setName ( String.format ( "Client-%03d-Thread", Long.valueOf ( client.getId () ) ) );
		this.setDaemon ( true );
		this.start ();
	}

	public void send ( int version, Set < SpiritFlag > flags, byte[] data )
	{
		if ( client.isClosed () )
			throw LOGGER.throwing ( new IllegalStateException ( "Client is closed" ) );
		try
		{
			LOGGER.trace ( "Message version[{}] flags[{}] data length[{}] data[{}]",
					Integer.valueOf ( version ),
					SpiritUtils.flagsToString ( flags ),
					Integer.valueOf ( data.length ),
					SpiritUtils.byteArrayToString ( data ) );
			outputStream.writeLong ( SPIRIT_MAGIC_NUMBER );
			outputStream.writeShort ( 0x0000FFFF & version );

			int fields = 0x0;
			for ( SpiritFlag f : flags )
				fields = fields | f.getFlagValue ();

			outputStream.writeShort ( 0x0000FFFF & fields );
			outputStream.writeInt ( data.length );
			outputStream.write ( data );
			outputStream.flush ();
		}
		catch ( IOException e )
		{
			throw LOGGER.throwing ( new SpiritException ( e.getMessage (), e ) );
		}
	}

	@Override
	public void run ()
	{
		try
		{
			boolean run = true;
			LOGGER.debug ( "Connection [{}] entring read loop",
					Long.valueOf ( client.getId () ) );
			while ( run )
			{
				try
				{
					long msgMagic = inputStream.readLong ();
					int msgVersion = inputStream.readUnsignedShort ();
					int msgFlags = inputStream.readUnsignedShort ();
					int msgBytelength = inputStream.readInt ();
					LOGGER.printf ( Level.TRACE,
							"Connection [%d] read message magic number [%d] version [%d] flags [0x%X] byte length [%s]",
							Long.valueOf ( client.getId () ),
							Long.valueOf ( msgMagic ),
							Integer.valueOf ( msgVersion ),
							Integer.valueOf ( msgFlags ),
							Integer.valueOf ( msgBytelength ) );
					byte[] inData = new byte[ msgBytelength ];
					int byteRead = inputStream.read ( inData );
					LOGGER.trace ( "Connection [{}] read [{}] bytes",
							Long.valueOf ( client.getId () ),
							Integer.valueOf ( byteRead ) );
					if ( byteRead < 0 )
					{
						run = false;
					}
					else if ( byteRead > 0 )
					{
						if ( ( SpiritFlag.STRING.getFlagValue () & msgFlags ) > 0 )
						{
							String message = new String ( inData, CHARSET );
							LOGGER.trace ( "Connection [{}] read message [{}]",
									Long.valueOf ( client.getId () ),
									message );
							client.onMessage ( message );
						}
						else if ( ( SpiritFlag.PING.getFlagValue () & msgFlags ) > 0 )
						{
						  LOGGER.trace ( "Connection [{}] read message [{}]",
                  Long.valueOf ( client.getId () ), Type.PING );
              client.onPing ();
						}
						else if ( ( SpiritFlag.PONG.getFlagValue () & msgMagic ) > 0 )
						{
						  LOGGER.trace ( "Connection [{}] read message [{}]",
                  Long.valueOf ( client.getId () ), Type.PONG );
              client.onPong ();
						}
						else
						{
							LOGGER.trace ( "Connection [{}] read bin message [{}]",
									Long.valueOf ( client.getId () ),
									SpiritUtils.byteArrayToString ( inData ) );
							client.onBin ( inData );
						}
					}
				}
				catch ( EOFException eofe )
				{
					run = false;
					LOGGER.catching ( Level.TRACE, eofe );
				}
				catch ( SocketTimeoutException ste )
				{
					client.onTimeout ( ste );
				}
			}
			LOGGER.debug ( "Connection [{}] exiting read loop",
					Long.valueOf ( client.getId () ) );
			inputStream.close ();
		}
		catch ( SocketException se )
		{
			if ( !"socket closed".equalsIgnoreCase ( se.getMessage ().trim () ) )
				LOGGER.catching ( Level.WARN, se );
			else
				LOGGER.catching ( Level.TRACE, se );
		}
		catch ( IOException e )
		{
			LOGGER.catching ( Level.WARN, e );
		}
		finally
		{
			try
			{
				client.close ();
			}
			catch ( SpiritException e )
			{
				LOGGER.catching ( Level.DEBUG, e );
			}
			try
			{
				socket.close ();
			}
			catch ( IOException e )
			{
				LOGGER.catching ( Level.DEBUG, e );
			}
		}
	}

	/**
	 * Close this client. This is an idempodent operation.
	 */
	@Override
	public void close () throws SpiritException
	{
		if ( !client.isClosed () )
			client.close ();
		try
		{
			LOGGER.debug ( "Client [{}] Closing output stream", this.client.getIdLong () );
			outputStream.close ();
		}
		catch ( IOException e )
		{
			throw LOGGER.throwing ( new SpiritException ( e.getMessage (), e ) );
		}
	}

	private static Charset initCharset ()
	{
		String charsetName = System.getProperty ( SPIRIT_CHARSET, SPIRIT_CHARSET_DEFAUTL );
		Charset charset = null;
		try
		{
			charset = Charset.forName ( charsetName );
		}
		catch ( Exception e )
		{
			LOGGER.warn ( String.format ( "Could not init charset [%s]", charsetName ), e );
			charset = Charset.forName ( SPIRIT_CHARSET_DEFAUTL );
		}
		LOGGER.info ( String.format ( "Using Charset [%s]", charset ) );
		return charset;
	}
}
