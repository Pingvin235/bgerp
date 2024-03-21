package ru.bgcrm.util;

import java.util.Random;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGMessageException;

/**
 * Password utilities.
 *
 * @author Shamil Vakhitov
 */
// TODO: Change to Config.
public class PswdUtil {
    /** Empty value for sending to FE. */
    public static final String EMPTY_PASSWORD = "*******";

    private static final String DEFAULT_PSWD_CHARS = "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";

    private ConfigMap setup;
    private String prefix;

    public PswdUtil(ConfigMap config, String prefix) {
        this.setup = config;
        this.prefix = prefix;
    }

    public static class UserPswdUtil extends PswdUtil {
        public UserPswdUtil(ConfigMap setup) {
            super(setup, "user.");
        }
    }

    public String generatePassword() {
        int length = getPasswordLengthAuto();
        String passwordChars = getPasswordChars();

        StringBuffer result = new StringBuffer();

        // набор символов
        char[] array = passwordChars.toCharArray();

        // длина набора символов
        final int arrayLength = array.length;

        long time = System.currentTimeMillis();

        // последние 4 цифры текущего времени в миллисекундах
        time = time % 10000L;

        int[] offsets = new int[4];
        for (int i = 3; i >= 0; i--) {
            offsets[i] = (int) time % 10;
            time /= 10;
        }

        Random random = new Random();

        // генерация пароля
        for (int i = 0; i < length; i++) {
            int pos = (offsets[i % 4] + random.nextInt(arrayLength)) % arrayLength;
            result.append(array[pos]);
        }

        return result.toString();
    }

    public void checkPassword(String pswd) throws BGMessageException {
        if (EMPTY_PASSWORD.equals(pswd))
            return;
        checkPassword(pswd, pswd);
    }

    public void checkPassword(String pswd1, String pswd2) throws BGMessageException {
        int passwordMin = getPasswordLengthMin();
        int passwordMax = getPasswordLengthMax();
        String passwordChars = getPasswordChars();

        if (Utils.isEmptyString(pswd1) || pswd1.length() < passwordMin || pswd1.length() > passwordMax) {
            throw new BGMessageException(
                    "Длина пароля должна быть от " + passwordMin + " до " + passwordMax + " символов!");
        } else if (!checkPasswordChars(pswd1, passwordChars)) {
            throw new BGMessageException("Пароль должен состоять из следующих символов - " + passwordChars);
        } else if (pswd2 != null && !pswd1.equals(pswd2)) {
            throw new BGMessageException("Пароли не совпадают!");
        }
    }

    private boolean checkPasswordChars(String password, String passwordChars) {
        boolean result = true;
        if (password != null && passwordChars != null) {
            for (int index = 0; index < password.length(); index++) {
                if (passwordChars.indexOf(password.charAt(index)) < 0) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    public int getPasswordLengthMin() {
        return setup.getInt(prefix + "password.length.min", 5);
    }

    public int getPasswordLengthMax() {
        return setup.getInt(prefix + "password.length.max", 10);
    }

    public int getPasswordLengthAuto() {
        return setup.getInt(prefix + "password.length.auto", getPasswordLengthMax());
    }

    public String getPasswordChars() {
        return setup.get(prefix + "password.chars", DEFAULT_PSWD_CHARS);
    }
}