package ru.bgcrm.model.authentication;

import ru.bgcrm.model.BGException;

@SuppressWarnings("serial")
public class BGAuthenticationException
    extends BGException
{
	private AuthenticationResult authenticationResult;
	
	public BGAuthenticationException()
	{
	}

	public BGAuthenticationException( String message )
	{
		super( message );
	}

	public BGAuthenticationException( Throwable cause )
	{
		super( cause );
	}

	public BGAuthenticationException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public BGAuthenticationException( AuthenticationResult authenticationResult )
	{
		super( authenticationResult.getMessage() );

		this.setAuthenticationResult( authenticationResult );
	}

	public AuthenticationResult getAuthenticationResult()
    {
	    return authenticationResult;
    }

	public void setAuthenticationResult( AuthenticationResult authenticationResult )
    {
	    this.authenticationResult = authenticationResult;
    }
}