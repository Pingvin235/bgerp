package ru.bgerp.plugin.telegram;

import org.w3c.dom.Document;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.DefaultProcessChangeListener.DefaultProcessorChangeContextEvent;
import ru.bgerp.plugin.telegram.bot.BgerpBot;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "telegram";
    
    public Plugin(Document doc) {
        super(doc, ID);
        
        BgerpBot.getInstance();
        
        EventProcessor.subscribe((e, conSet) -> {
            e.getContext().put(ID, new DefaultProcessorFunctions());
        }, DefaultProcessorChangeContextEvent.class );
    }
}
