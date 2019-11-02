package ru.bgcrm.util.distr.call;

import java.io.File;

import ru.bgcrm.util.Preferences;

public class RemoveFile implements InstallationCall {
    @Override
    public boolean call(Preferences setup, File zip, String param) {
        boolean result = false;

        try {
            File file = new File(param);
            if (file.exists()) {
                System.out.println("Removing file: " + param);
                file.deleteOnExit();
                result = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
