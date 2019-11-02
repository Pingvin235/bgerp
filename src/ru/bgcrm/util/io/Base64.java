package ru.bgcrm.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Всякие утильные методы Base64. Используют для кодирования/декодирования
 * стримы Base64OutputStream/Base64InputStream.
 * 
 * Стандартного механизма подобного нету, только в разных сторонних либах,
 * типа остермиллер/джаспер/org.apache.batik.блабла/sun.com.блабла/org.wc3.блабла,
 * по проекту сотни использований разных этих методов. Надо использовать один
 * стандартный.
 */
public class Base64
{
	/** символы для индексации */
	static String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	/** заполнитель */
	static char PAD = '=';

	/**
	 * Кодирование строки. Входная строка преобразуется перед кодированием в
	 * поток байтов по ДЕФОЛТНОЙ кодировке.
	 * 
	 * @param str
	 *            Исходная строка.
	 * @return закодированная строка.
	 * @throws RuntimeException
	 *             При любых ошибках фатальных.
	 */
	public static String encode( String str )
		throws RuntimeException
	{
		byte[] bytes = str.getBytes();
		byte[] encoded = encode( bytes );
		try
		{
			return new String( encoded, "ASCII" );
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException( "Unsupported charset: ASCII", e );
		}
	}

	/**
	 * Кодирование строки. Входная строка преобразуется в поток байтов по
	 * указанной кодировке.
	 * 
	 * @param str
	 *            Исходная строка.
	 * @param charset
	 *            Имя кодировки.
	 * @return закодированная строка.
	 * @throws RuntimeException
	 *             При любых ошибках фатальных.
	 */
	public static String encode( String str, String charset )
		throws RuntimeException
	{
		byte[] bytes;
		try
		{
			bytes = str.getBytes( charset );
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException( "Unsupported charset: " + charset, e );
		}
		byte[] encoded = encode( bytes );
		try
		{
			return new String( encoded, "ASCII" );
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException( "Unsupported charset: ASCII", e );
		}
	}

	/**
	 * Декодирование строки. Полученный поток байтов преобразуется в строку по
	 * ДЕФОЛТНОЙ кодировке.
	 * 
	 * @param str
	 *            зашифрованная строка.
	 * @return расшифрованная строка.
	 * @throws RuntimeException
	 *             При любых ошибках фатальных.
	 */
	public static String decode( String str )
		throws RuntimeException
	{
		byte[] bytes;
		try
		{
			bytes = str.getBytes( "ASCII" );
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException( "Unsupported charset: ASCII", e );
		}
		byte[] decoded = decode( bytes );
		return new String( decoded );
	}

	/**
	 * Декодирование строки. Полученный поток байтов преобразуется в строку по
	 * указанной кодировке.
	 * 
	 * @param str
	 *            зашифрованная строка.
	 * @param charset
	 *            Имя кодировки.
	 * @return расшифрованная строка.
	 * @throws RuntimeException
	 *             При любых ошибках фатальных.
	 */
	public static String decode( String str, String charset )
		throws RuntimeException
	{
		byte[] bytes;
		try
		{
			bytes = str.getBytes( "ASCII" );
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException( "Unsupported charset: ASCII", e );
		}
		byte[] decoded = decode( bytes );
		try
		{
			return new String( decoded, charset );
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException( "Unsupported charset: " + charset, e );
		}
	}

	/**
	 * Кодирование байтового массива. Для больших данных возьми-ка
	 * {@link Base64OutputStream} лучше.
	 * 
	 * @param bytes
	 *            исходный массив.
	 * @return зашифрованный массив.
	 * @throws RuntimeException
	 *             При любых ошибках фатальных внутренних.
	 */
	public static byte[] encode( byte[] bytes )
		throws RuntimeException
	{
		return encode( bytes, 0 );
	}

	/**
	 * Кодирование байтового массива. Каждые wrapAt байт переход на новую строку
	 * (0 -отключение). Для больших данных возьми-ка {@link Base64OutputStream}
	 * лучше.
	 * 
	 * @param bytes
	 *            исходный массив.
	 * @param wrapAt
	 *            максимальная длина строки для зашифрованных данных (0
	 *            -отключение).
	 * @return зашифрованный массив.
	 * @throws RuntimeException
	 *             При любых ошибках фатальных внутренних.
	 */
	public static byte[] encode( byte[] bytes, int wrapAt )
		throws RuntimeException
	{
		ByteArrayInputStream inputStream = new ByteArrayInputStream( bytes );
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			encode( inputStream, outputStream, wrapAt );
		}
		catch( IOException e )
		{
			throw new RuntimeException( "Unexpected I/O error", e );
		}
		finally
		{
			try
			{
				inputStream.close();
			}
			catch( Throwable t )
			{
			}
			try
			{
				outputStream.close();
			}
			catch( Throwable t )
			{
			}
		}
		return outputStream.toByteArray();
	}

	/**
	 * Декодирование байтового массива. Для больших данных возьми-ка
	 * {@link Base64InputStream} лучше.
	 * 
	 * @param bytes
	 *            зашифрованная последовательность.
	 * @return расшифрованные байты.
	 * @throws RuntimeException
	 *             При любых ошибках фатальных внутренних.
	 */
	public static byte[] decode( byte[] bytes )
		throws RuntimeException
	{
		ByteArrayInputStream inputStream = new ByteArrayInputStream( bytes );
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			decode( inputStream, outputStream );
		}
		catch( IOException e )
		{
			throw new RuntimeException( "Unexpected I/O error", e );
		}
		finally
		{
			try
			{
				inputStream.close();
			}
			catch( Throwable t )
			{
			}
			try
			{
				outputStream.close();
			}
			catch( Throwable t )
			{
			}
		}
		return outputStream.toByteArray();
	}

	/**
	 * Кодирование. Данные из входного стрима кодируются в выходной стрим.
	 * Входной поток читается до разрушения. Ничего внутри не закрывается.
	 * Ничего внутри не флушится.
	 * 
	 * @param inputStream
	 *            входной стрим исходный.
	 * @param outputStream
	 *            выходной стрим куда пишется зашифрованное.
	 * @throws IOException
	 *             при ошибке I/O.
	 */
	public static void encode( InputStream inputStream, OutputStream outputStream )
		throws IOException
	{
		encode( inputStream, outputStream, 0 );
	}

	/**
	 * Кодирование. Данные из входного стрима кодируются в выходной стрим.
	 * Каждые wrapAt байт переход на новую строку (0 -отключение). Входной поток
	 * читается до разрушения. Ничего внутри не закрывается. Ничего внутри не
	 * флушится.
	 * 
	 * @param inputStream
	 *            входной стрим исходный.
	 * @param outputStream
	 *            выходной стрим куда пишется зашифрованное.
	 * @param wrapAt
	 *            максимальная длина строки для зашифрованных данных (0
	 *            -отключение).
	 * @throws IOException
	 *             при ошибке I/O.
	 */
	public static void encode( InputStream inputStream, OutputStream outputStream, int wrapAt )
		throws IOException
	{
		Base64OutputStream aux = new Base64OutputStream( outputStream, wrapAt );
		IOUtils.flush( inputStream, aux );
		aux.flushbuffer();
	}

	/**
	 * Декодирование. Входной поток читается до разрушения. Ничего внутри не
	 * закрывается. Ничего внутри не флушится.
	 * 
	 * @param inputStream
	 *            входной стрим зашифрованный.
	 * @param outputStream
	 *            выходной стрим, куда пишется расшифрованный поток байтов.
	 * @throws IOException
	 *             при ошибке I/O.
	 */
	public static void decode( InputStream inputStream, OutputStream outputStream )
		throws IOException
	{
		IOUtils.flush( new Base64InputStream( inputStream ), outputStream );
	}
	
	/**
	 * Костыль. Синтетическая функция превращает стрим (например, файловый) в
	 * строку закодированную. Используется при передаче файлов, например.
	 * @param inputStream стрим незакодированный
	 * @return строка базе64-закодированная.
	 * @throws IOException 
	 */
	public static String encode( InputStream inputStream ) throws IOException	
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			encode( inputStream, outputStream );
		}
		finally
		{
			try
			{
				outputStream.close();
			}
			catch( Throwable t )
			{
			}
		}
		return outputStream.toString();
	}
	
	/**
	 * Кодирование строки для использования её в вебе, например, при создании
	 * url, в get-запросе, например. Иначе символы типа + рвут все параметры.
	 * 
	 * Стандартом Base64-кодирования URL адресов, признается вариант, когда
	 * символы '+' и '/' заменяются, соответственно, на '-' и '_' (RFC3548,
	 * раздел 4).
	 * 
	 * @param base64
	 *            входящая строка в виде base64
	 * 
	 * @return выходная строка для веба
	 */
	public static String encodeWeb( String base64 )
	{
		return base64.replace( '+', '-' ).replace( '/', '_' );
	}
	
	/**
	 * Обратная к {@link #encodeWeb()}.
	 * 
	 * @param base64web
	 *            входящая строка в виде base64-web
	 * @return выходная строка для расшифрования из base64
	 * @throws IOException
	 */
	public static String decodeWeb( String base64web )
	{
		return base64web.replace( '-', '+' ).replace( '_', '/' );
	}
}
