package ru.bgcrm.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Обёртка для декодирования base64 потока.
 * 
 * Base64InputStream считывает данные из base64-потока. Декодирует данные,
 * возвращает расшифрованные байты.
 */
public class Base64InputStream
	extends InputStream
{
	/**
	 * Спрятанный стрим.
	 */
	private InputStream inputStream;

	/**
	 * буфер
	 */
	private int[] currentBuffer;

	/**
	 * число значений в буфере.
	 */
	private int currentBufferCounter = 0;

	/**
	 * флаг окончания стрима.
	 */
	private boolean eof = false;

	/**
	 * Создаётся стрим декодирующий.
	 * 
	 * @param inputStream
	 *            Основной поток, из которого закодированные данные считываются.
	 */
	public Base64InputStream( InputStream inputStream )
	{
		this.inputStream = inputStream;
	}

	@Override
	public int read()
		throws IOException
	{
		if ( currentBuffer == null || currentBufferCounter == currentBuffer.length )
		{
			if ( eof )
			{
				return -1;
			}
			flushbuffer();
			if ( currentBuffer.length == 0 )
			{
				currentBuffer = null;
				return -1;
			}
			currentBufferCounter = 0;
		}
		return currentBuffer[currentBufferCounter++];
	}

	/**
	 * Считывается всё и конвертируется.
	 */
	private void flushbuffer()
		throws IOException
	{
		char[] four = new char[4];
		int i = 0;
		do
		{
			int b = inputStream.read();
			if ( b == -1 )
			{
				if ( i != 0 )
				{
					throw new IOException( "Bad base64 stream" );
				}
				else
				{
					currentBuffer = new int[0];
					eof = true;
					return;
				}
			}
			char c = (char)b;
			if ( Base64.CHARS.indexOf( c ) != -1 || c == Base64.PAD )
			{
				four[i++] = c;
			}
			else if ( c != '\r' && c != '\n' )
			{
				throw new IOException( "Bad base64 stream" );
			}
		}
		while ( i < 4 );
		boolean padded = false;
		for ( i = 0; i < 4; i++ )
		{
			if ( four[i] != Base64.PAD )
			{
				if ( padded )
				{
					throw new IOException( "Bad base64 stream" );
				}
			}
			else
			{
				if ( !padded )
				{
					padded = true;
				}
			}
		}
		int l;
		if ( four[3] == Base64.PAD )
		{
			if ( inputStream.read() != -1 )
			{
				throw new IOException( "Bad base64 stream" );
			}
			eof = true;
			if ( four[2] == Base64.PAD )
			{
				l = 1;
			}
			else
			{
				l = 2;
			}
		}
		else
		{
			l = 3;
		}
		int aux = 0;
		for ( i = 0; i < 4; i++ )
		{
			if ( four[i] != Base64.PAD )
			{
				aux = aux | (Base64.CHARS.indexOf( four[i] ) << (6 * (3 - i)));
			}
		}
		currentBuffer = new int[l];
		for ( i = 0; i < l; i++ )
		{
			currentBuffer[i] = (aux >>> (8 * (2 - i))) & 0xFF;
		}
	}

	@Override
	public void close()
		throws IOException
	{
		inputStream.close();
	}
}