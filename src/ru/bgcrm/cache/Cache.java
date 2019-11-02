package ru.bgcrm.cache;

public abstract class Cache<C extends Cache<C>>
{
	protected abstract C newInstance();
		
	protected Cache()
	{}
}