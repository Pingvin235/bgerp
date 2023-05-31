package ru.bgcrm.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.bgerp.app.bean.Bean;
import org.bgerp.custom.java.CompilationResult;
import org.bgerp.custom.java.CompilerWrapper.CompilationFailedException;
import org.bgerp.custom.java.CompilerWrapper.CompiledUnit;
import org.bgerp.util.Log;

import ru.bgcrm.dynamic.model.DynamicClass;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Pair;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

/**
 * Загрузчик динамического кода. Подгружает скомпилированные классы из каталога dyn.
 */
@Deprecated
public class DynamicClassManager {
    private static final Log log = Log.getLog();

    public static final String DYNAMIC_CLASS_PACKAGE = "ru.bgcrm.dyn.";
    private static final Setup setup = Setup.getSetup();

    private static class InstanceHolder {
        //типа ленивая инициализация со значением по умолчанию
        private static DynamicClassManager instance = new DynamicClassManager(
                Thread.currentThread().getContextClassLoader());
    }

    public static final File getScriptsDir() {
        return Utils.createDirectoryIfNoExistInWorkDir(setup.get("dynamic.src.dir", setup.get("dyn.src.dir", "dyn")));
    }

    /**
     * Возвращает File, соответствующий файлу с исходным кодом указанного класа.
     * @param className имя класса
     * @return file
     */
    public static File getClassFile(String className) {
        // разделитель для случая вложенного класса
        int innerClassDelim = className.indexOf('$');

        String path;
        if (innerClassDelim >= 0) {
            path = className.substring(0, innerClassDelim);
        } else {
            path = className;
        }
        path = path.replace('.', File.separatorChar);

        return new File(getScriptsDir(), path + ".java");
    }

    /**
     * Возвращает инстанцию менеджера динамических классов. По умолчанию инициализируется ReadOnlyClassManager'ом.
     * @return
     */
    public static DynamicClassManager getInstance() {
        return InstanceHolder.instance;
    }

    protected ClassLoader parentClassLoader;
    private CompilerWrapper javac;

    //private ConcurrentHashMap<String, byte[]> classData = new ConcurrentHashMap<String, byte[]>();
    protected ConcurrentHashMap<String, Class<?>> loadedClasses = new ConcurrentHashMap<String, Class<?>>();

    private DynamicClassManager(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
        this.javac = new CompilerWrapper(getScriptsDir());
    }

    /**
     * Возвращает список имён классов.
     * @return список классов
     * @throws BGException
     */
    public List<String> getDynamicClassNames() throws BGException {
        List<DynamicClass> classList = new ArrayList<DynamicClass>();

        traverse(classList, getScriptsDir(), "");

        List<String> result = new ArrayList<String>(classList.size());
        for (DynamicClass dynClass : classList) {
            result.add(dynClass.getClassName());
        }

        return result;
    }

    private void traverse(List<DynamicClass> classes, File directory, String prefix) {
        for (File file : directory.listFiles()) {
            //исключаем скрытые директории, типа .git или .svn
            if (file.isDirectory() && !file.getName().startsWith(".")) {
                traverse(classes, file, prefix + file.getName() + ".");
            } else if (file.getName().endsWith(".java")) {
                classes.add(new DynamicClass(prefix + file.getName().substring(0, file.getName().length() - 5),
                        file.lastModified()));
            }
        }
    }

    public CompilationResult recompileAll() throws BGException {
        CompilationResult result = null;

        try {
            loadedClasses.clear();

            result = recompile(getDynamicClassNames());

            if (!result.isResult())
                throw new CompilationFailedException(result);

            log.info("Successfully recompiled dyn classes.");
        } catch (CompilationFailedException ex) {
            result = ex.getCompilationResult();
        }

        log.info("Compile dyn classes result:");
        log.info(result.getLogString());

        return result;
    }

    private CompilationResult recompile(List<String> targetClassNames) throws BGException {
        List<String> srcFiles = new ArrayList<String>();
        for (String name : targetClassNames) {
            srcFiles.add(getClassFile(name).getAbsolutePath());
        }

        if (srcFiles.size() == 0) {
            return new CompilationResult();
        }

        //компилируем..
        Pair<CompilationResult, List<CompiledUnit>> result = javac.compile(srcFiles);
        List<CompiledUnit> units = result.getSecond();

        Map<String, byte[]> classData = new HashMap<String, byte[]>();

        //компиляция успешна - пишем в мап
        for (CompiledUnit unit : units) {
            try {
                //запись в мап
                FileInputStream fis = new FileInputStream(unit.classFile);
                classData.put(unit.className, IOUtils.toByteArray(fis));
            } catch (IOException ex) {
                throw new BGException(ex);
            }
        }

        // удаляем временную папку со скомпилированными файлами
        javac.deleteClassDir();

        ClassLoader loader = new DatabaseClassLoader(classData, parentClassLoader);

        //перезагрузка классов из базы
        for (CompiledUnit unit : units) {
            try {
                //подгрузка класслоадером
                Class<?> loaded = loader.loadClass(unit.className);
                loadedClasses.put(unit.className, loaded);

                if (log.isDebugEnabled()) {
                    log.debug("Add loaded class: " + unit.className);
                }

                //получение информации об интерфейсах, запись ее в базу
                //dao.updateClassInterfaces( loaded );
            } catch (ClassNotFoundException e) {
                throw new BGException(e);
            } catch (Throwable e) {
                //ошибки класслоадера низкого уровня, пока пусть тоже будет BGException
                throw new BGException(e);
            }
        }

        return result.getFirst();
    }

    /**
     * Возвращает актуальную версию загруженного класса по его имени.
     * @param className полное имя класса
     * @return класс
     * @throws BGException если возникли проблемы
     */
    private Class<?> loadClass(String className) throws BGException {
        return loadedClasses.get(className);
    }

    /**
     * Возвращает актуальный класс по его имени. Динамический либо обычный.
     *
     * @param className
     * @return
     * @throws BGException
     * @throws ClassNotFoundException
     */
    public static Class<?> getClass(String className) throws BGException, ClassNotFoundException {
        Class<?> clazz = null;

        if (className.startsWith(DYNAMIC_CLASS_PACKAGE)) {
            log.warn("Using deprecated dynamic class: {}", className);
            clazz = (Class<?>) getInstance().loadClass(className);
            if (clazz == null) {
                throw new ClassNotFoundException();
            }
        } else {
            clazz = Bean.getClass(className);
        }

        return clazz;
    }

    /**
     * Возвращает актуальный инстанс имплементации некоего интерфейса.
     * Данный метод всегда будет возвращать актуальную версию некоего класса.
     *
     * @param implClassName полное имя класса
     * @return объект, реализующий интерфейс
     * @throws BGException, ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static final <T> T newInstance(String implClassName) throws BGException, ClassNotFoundException {
        try {
            Class<T> clazz = (Class<T>) getClass(implClassName);
            if (clazz != null) {
                return clazz.getDeclaredConstructor().newInstance();
            }
            return null;
        } catch (ClassNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BGException(ex);
        }
    }
}