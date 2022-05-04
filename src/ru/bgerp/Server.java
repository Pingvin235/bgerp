package ru.bgerp;

import org.bgerp.util.Log;

public class Server {
    public static void main(String[] args) {
        org.bgerp.Server.main(args);
        Log.getLog(Server.class).warn("Use 'org.bgerp.Server' class for starting");
    }
}
