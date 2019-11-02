package ru.bgcrm.plugin.tele2c;

import ru.bgcrm.util.ParameterMap;

public class Config extends ru.bgcrm.util.Config {
    
    public final String url;
    public final String login;
    public final String password;
    public final String naming;

    public Config(ParameterMap setup) {
        super(setup);

        setup = setup.sub(Plugin.ID + ":");

        url = setup.get("url", "https://newbsms.tele2.ru/api/?operation=send");
        login = setup.get("login", "BGERP");
        password = setup.get("password", "BGERP");
        naming = setup.get("naming", "SINGSMS");
    }

    public String getUrl() {
        return url;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getNaming() {
        return naming;
    }

}