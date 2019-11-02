package ru.bgcrm.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class ThreadedPrintStream extends PrintStream
{
	protected PrintStream wrapped = null;
	protected ThreadLocal<PrintStream> streams = new InheritableThreadLocal<PrintStream>();
	protected ThreadLocal<ByteArrayOutputStream> data = new InheritableThreadLocal<ByteArrayOutputStream>();

	public ThreadedPrintStream( PrintStream wrapped )
	{
		super( wrapped );
		this.wrapped = wrapped;
	}

	public PrintStream getWrapped()
	{
		return wrapped;
	}

	public void startCapture()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream( 128 );
		data.set( baos );
		streams.set( new PrintStream( baos ) );
	}

	public void startCapture( PrintStream printStream )
	{
		streams.set( printStream );
	}

	public void stopCapture()
	{
		streams.set( null );
		data.set( null );
	}

	public String stopCaptureAndGet()
	{
		ByteArrayOutputStream baos = data.get();

		streams.set( null );

		if( baos == null )
		{
			return null;
		}

		data.set( null );
		return baos.toString();
	}

	protected PrintStream getPrintStream()
	{
		PrintStream ps = streams.get();
		if( ps == null  )
		{
			ps = wrapped;
		}
		return ps;
	}

	// ////////////////////

	@Override
    public void flush()
	{
		getPrintStream().flush();
	}

	@Override
    public void close()
	{
		getPrintStream().close();
	}

	@Override
    public boolean checkError()
	{
		return getPrintStream().checkError();
	}

	@Override
    protected void setError()
	{
		// findStream().setError();
	}

	@Override
    public void write( int b )
	{
		getPrintStream().write( b );
	}

	@Override
    public void write( byte[] b ) throws IOException
	{
		getPrintStream().write( b );
	}

	@Override
    public void write( byte[] buf, int off, int len )
	{
		getPrintStream().write( buf, off, len );
	}

	@Override
    public void print( boolean b )
	{
		getPrintStream().print( b );
	}

	@Override
    public void print( char c )
	{
		getPrintStream().print( c );
	}

	@Override
    public void print( int i )
	{
		getPrintStream().print( i );
	}

	@Override
    public void print( long l )
	{
		getPrintStream().print( l );
	}

	@Override
    public void print( float f )
	{
		getPrintStream().print( f );
	}

	@Override
    public void print( double d )
	{
		getPrintStream().print( d );
	}

	@Override
    public void print( char[] s )
	{
		getPrintStream().print( s );
	}

	@Override
    public void print( String s )
	{
		getPrintStream().print( s );
	}

	@Override
    public void print( Object obj )
	{
		getPrintStream().print( obj );
	}

	@Override
    public void println()
	{
		getPrintStream().println();
	}

	@Override
    public void println( boolean x )
	{
		getPrintStream().println( x );
	}

	@Override
    public void println( char x )
	{
		getPrintStream().println( x );
	}

	@Override
    public void println( int x )
	{
		getPrintStream().println( x );
	}

	@Override
    public void println( long x )
	{
		getPrintStream().println( x );
	}

	@Override
    public void println( float x )
	{
		getPrintStream().println( x );
	}

	@Override
    public void println( double x )
	{
		getPrintStream().println( x );
	}

	@Override
    public void println( char[] x )
	{
		getPrintStream().println( x );
	}

	@Override
    public void println( String x )
	{
		getPrintStream().println( x );
	}

	@Override
    public void println( Object x )
	{
		getPrintStream().println( x );
	}
}
