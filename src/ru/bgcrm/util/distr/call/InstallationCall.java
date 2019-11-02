package ru.bgcrm.util.distr.call;

import java.io.File;

import ru.bgcrm.util.Preferences;

public interface InstallationCall {
    public boolean call(Preferences setup, File zip, String param);
}
