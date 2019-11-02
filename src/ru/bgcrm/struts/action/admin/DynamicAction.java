package ru.bgcrm.struts.action.admin;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.dynamic.model.CompilationResult;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.RunClassRequestEvent;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class DynamicAction extends BaseAction {
	@Override
	protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws Exception {
		return processUserTypedForward(conSet, mapping, form, FORWARD_DEFAULT);
	}

	// перекомпиляция всех файлов
	public ActionForward recompileAll(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws Exception {
		CompilationResult result = DynamicClassManager.getInstance().recompileAll();

		EventProcessor.subscribeDynamicClasses();

		form.getResponse().setData("result", result);

		return processUserTypedForward(conSet, mapping, form, FORWARD_DEFAULT);
	}

	public ActionForward runDynamicClass(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws Exception {
		String className = form.getParam("class");
		
		String ifaceType = form.getParam("iface", "event");
		// запуск интерфейса EventListener
		if ("event".equals(ifaceType))
			EventProcessor.processEvent(new RunClassRequestEvent(form), className, conSet);
		// запуск интерфейса Runnable
		else {
			Class<?> clazz = null;
			try {
				clazz = DynamicClassManager.getClass(className);
			} catch (ClassNotFoundException e) {
				throw new BGMessageException("Класс не найден: " + className);
			}

			if (Runnable.class.isAssignableFrom(clazz)) {
			    boolean sync = form.getParamBoolean("sync");
			    if (sync)
			        ((Runnable) clazz.newInstance()).run();
			    else			    
			        new Thread((Runnable) clazz.newInstance()).start();
			} else {
				throw new BGMessageException("Класс не реализует java.land.Runnable: " + className);
			}
		}

		return processJsonForward(conSet, form);
	}
}