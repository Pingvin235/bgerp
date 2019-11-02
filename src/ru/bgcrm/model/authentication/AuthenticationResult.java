package ru.bgcrm.model.authentication;

public enum AuthenticationResult
{
	SUCCESS( true, "" ),
	USERNAME_AND_PASSWORD_NOT_DEFINED( false, "Не указаны имя пользователя и пароль!" ),
	USERNAME_IS_NOT_DEFINED( false, "Не указано имя пользователя!" ),
	USERNAME_IS_EMPTY( false, "Указано пустое имя пользователя!" ),
	PASSWORD_IS_NOT_DEFINED( false, "Не указан пароль пользователя!" ),
	PASSWORD_IS_EMPTY( false, "Указан пустой пароль пользователя!" ),
	USER_NOT_FOUND( false, "Указанный пользователь не найден!" ),
	USER_NOT_ENABLED( false, "Указанный пользователь на активирован!" ),
	PASSWORD_INCORRECT( false, "Неверный пароль!" ),
	DENY_AUTH_IN_VERSION( false, "Авторизация в данной версии запрещена!" );

	private final boolean isSuccessful;
	private final String message;

	private AuthenticationResult( boolean isSuccessful, String message )
	{
		this.isSuccessful = isSuccessful;
		this.message = message;
	}

	public boolean isSuccessful()
    {
	    return isSuccessful;
    }

	public String getMessage()
    {
	    return message;
    }
}