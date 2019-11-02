package ru.bgcrm.event.listener;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.ParamChangingEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.util.RegexpCheckerConfig;
import ru.bgcrm.util.sql.ConnectionSet;

public class ParamValidatorSystemListener
{
	public ParamValidatorSystemListener()
	{
		EventProcessor.subscribe( new EventListener<ParamChangingEvent>()
		{
			@Override
            public void notify( ParamChangingEvent e, ConnectionSet connectionSet )
				throws BGException
            {
	            paramChanging( e, connectionSet );
            }
			
		}, ParamChangingEvent.class );
	}
	
	private void paramChanging( ParamChangingEvent e, ConnectionSet connectionSet )
		throws BGException
	{
		Parameter param = e.getParameter();
		
		if( !Parameter.TYPE_TEXT.equals( param.getType() ) )
		{
			return;
		}
		/* пустая строка не проверяется
		if( Utils.isBlankString( (String)e.getValue() ) )
		{
			return;
		}*/
		
		RegexpCheckerConfig config = param.getConfigMap().getConfig( RegexpCheckerConfig.class );
		config.checkValue( (String)e.getValue() );
	}
}
