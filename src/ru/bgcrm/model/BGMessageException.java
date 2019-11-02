package ru.bgcrm.model;

/**
 * Исключение, сообщение которого необходимо показать пользователю и не надо писать в лог.
 */
public class BGMessageException 
	extends BGException
{
	// код с ошибкой для автоматической обработки, например, класс исключения при вызове Web сервиса
	private final String exception;
	
	public BGMessageException( String exception, String message )
	{
		super( message );
		this.exception = exception;
	}
	
	public BGMessageException( String message )
	{
		this( null, message );		
	}

	public String getException()
	{
		return exception;
	}
}
