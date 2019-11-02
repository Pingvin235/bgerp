package ru.bgcrm.util;

import org.apache.log4j.Logger;

import ru.bgcrm.model.BGMessageException;

/**
 * Конфигурация. Создается один раз и хранится в {@link ParameterMap} до его изменения.
 * В конструкторе реализуется парсинг. 
 * Получение объекта через {@link ParameterMap#getConfig(Class)}.
 */
public abstract class Config {
    protected static final Logger log = Logger.getLogger(Config.class);
    
    /**
     * Конфигурация создана в режиме валидации, можно выбрасывать {@link BGMessageException}.
     */
    protected final boolean validate;

    protected Config(ParameterMap setup) {
        this.validate = false;
    }
    
    protected Config(ParameterMap setup, boolean validate) {
        this.validate = validate;
    }

    /*protected static int[] intListToArray(List<Integer> list) {
        int[] result = new int[list.size()];
        int i = 0;
        for (Integer in : list) {
            result[i] = (in == null) ? 0 : in;
            i++;
        }
        return result;
    }

    protected static long[] longListToArray(List<Long> list) {
        long[] result = new long[list.size()];
        int i = 0;
        for (Long in : list) {
            result[i] = (in == null) ? 0 : in;
            i++;
        }
        return result;
    }

    protected static Pattern[] patternListToArray(List<Pattern> list) {
        Pattern[] result = new Pattern[list.size()];
        int i = 0;
        for (Pattern in : list) {
            result[i] = in;
            i++;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected static <T> T[] toArray(List<T> list, Class<?> t) {
        T[] a = (T[]) java.lang.reflect.Array.newInstance(t, list.size());
        int i = 0;
        for (T e : list) {
            a[i] = e;
            i++;
        }

        return a;
    }

    protected static String[] stringListToArray(List<String> list) {
        String[] result = new String[list.size()];
        int i = 0;
        for (String in : list) {
            result[i] = in;
            i++;
        }
        return result;
    }*/
}
