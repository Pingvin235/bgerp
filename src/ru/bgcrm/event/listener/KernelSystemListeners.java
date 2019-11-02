package ru.bgcrm.event.listener;

import ru.bgcrm.dao.Locker;

public class KernelSystemListeners
{
	public KernelSystemListeners()
	{
		new CustomerSystemListener();
		new ParamValidatorSystemListener();
		new ProcessClosingListener();		
		new DefaultProcessChangeListener();
		new NewsEventListener();
		new Locker();
		new LoginEventListener();
		new ProcessFilterCounterListener();
		new TemporaryObjectOpenListener();
		new SphinxSystemListener();
	}
}
