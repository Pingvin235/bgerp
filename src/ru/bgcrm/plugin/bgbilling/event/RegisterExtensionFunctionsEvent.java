package ru.bgcrm.plugin.bgbilling.event;

import net.sf.saxon.s9api.Processor;
import ru.bgcrm.event.UserEvent;
import ru.bgcrm.struts.form.DynActionForm;

//Событие для регистрации в модулях расширений XSLT.
public class RegisterExtensionFunctionsEvent
	extends UserEvent
{
	private final Processor proc;

	public RegisterExtensionFunctionsEvent( Processor proc, DynActionForm form )
	{
		super( form );
		this.proc = proc;
	}

	public Processor getProc()
	{
		return proc;
	}
}
