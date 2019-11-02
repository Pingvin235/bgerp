package ru.bgcrm.dynamic;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.log4j.Logger;

import ru.bgcrm.dynamic.model.CompilationMessage;
import ru.bgcrm.dynamic.model.CompilationResult;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Pair;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

/**
 * Враппер для компилятора.
 */
public final class CompilerWrapper {
    private static final Logger logger = Logger.getLogger(CompilerWrapper.class);

    private File outputdirRoot;
    private File outputdir;
    private File srcdir;

    public CompilerWrapper(File srcdir, File outputdirRoot) {
        this.srcdir = srcdir;
        this.outputdirRoot = outputdirRoot;
    }

    /**
     * Компилирует поданные исходники
     * 
     * @param srcFiles список полных путей к файлам
     * @return пара: результат компиляции и список скомпилированных файлов
     */
    public Pair<CompilationResult, List<CompiledUnit>> compile(List<String> srcFiles)
            throws CompilationFailedException {
        // берем конпейлятор
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // добавляем диагностик листенера
        DiagnosticListenerImpl listener = new DiagnosticListenerImpl();

        // берем файл-менеджера
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(listener, null, null);

        // получаем ява-объекты из сорцов
        Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjectsFromStrings(srcFiles);

        // говорим куда и как конпейлять
        Setup setup = Setup.getSetup();
        String encoding = setup.get("dynamic.src.encoding", Utils.UTF8.name());

        // устанавливаем временную папку для скомпилированных файлов с уникальным именем
        outputdir = new File(outputdirRoot.getAbsoluteFile() + File.separator + UUID.randomUUID().toString());
        outputdir.mkdir();

        String[] options;
        if (Utils.notBlankString(encoding)) {
            options = new String[] { "-classpath", getClasspath(), "-sourcepath", srcdir.getAbsolutePath(), "-encoding",
                    encoding, "-d", outputdir.getAbsolutePath(), "-verbose", "-Xlint:deprecation" };
        } else {
            options = new String[] { "-classpath", getClasspath(), "-sourcepath", srcdir.getAbsolutePath(), "-d",
                    outputdir.getAbsolutePath(), "-verbose", "-Xlint:deprecation" };
        }

        // создаем задание на конпейляцию
        StringWriter sw = new StringWriter();
        CompilationTask task = compiler.getTask(sw, fileManager, listener, Arrays.asList(options), null, fileObjects);

        // конпейлируем!
        boolean callResult = task.call();
        CompilationResult result = listener.getCompilationResult();

        if (!callResult) {
            throw new CompilationFailedException(result);
        }

        return new Pair<CompilationResult, List<CompiledUnit>>(result, compiledFiles(outputdir));
    }

    private static String classpath = null;

    private static String getClasspath() {
        if (classpath != null) {
            return classpath;
        }

        StringBuilder sb = new StringBuilder(300);
        String[] classpathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
        for (String classPath : classpathEntries) {
            sb.append(new File(classPath)).append(File.pathSeparator);
        }

        return classpath = sb.toString();
    }

    /**
     * Получает список объектов с информацией о скомпилированных файлах, лежащих в
     * указанной директории.
     * 
     * @param directory
     * @return
     */
    private List<CompiledUnit> compiledFiles(File directory) {
        List<CompiledUnit> result = new ArrayList<CompiledUnit>();

        for (File tmpdirFile : directory.listFiles()) {
            // если файл (а не папка), добавляем его данные в список
            if (tmpdirFile.isFile()) {
                File compiledFile = tmpdirFile;
                String filePath = compiledFile.getAbsolutePath();

                String className = filePath.substring(outputdir.getAbsolutePath().length() + 1)
                        .replace(File.separatorChar, '.');
                className = className.substring(0, className.length() - 6);

                if (logger.isDebugEnabled()) {
                    logger.debug("Found compiled unit: " + className + " with file " + compiledFile.getAbsolutePath());
                }

                CompiledUnit unit = new CompiledUnit();
                unit.className = className;
                unit.classFile = compiledFile;
                unit.srcFile = DynamicClassManager.getClassFile(className);
                result.add(unit);
            } else if (tmpdirFile.isDirectory()) // если папка, то проверяем ее содержимое
            {
                result.addAll(compiledFiles(tmpdirFile));
            }
        }

        return result;
    }

    /*
     * удаляет директорию outputdir, в которой находятся скомпилированные файлы
     */
    public void deleteClassDir() {
        if (outputdir != null && outputdir.exists()) {
            deleteDirWithFiles(outputdir);
        }
    }

    private void deleteDirWithFiles(File dir) {
        for (File tmpdirFile : dir.listFiles()) {
            if (tmpdirFile.isFile()) {
                tmpdirFile.delete();
            } else if (tmpdirFile.isDirectory()) {
                deleteDirWithFiles(tmpdirFile);
            }
        }

        dir.delete();
    }

    private class DiagnosticListenerImpl implements DiagnosticListener<JavaFileObject> {
        private CompilationResult result = new CompilationResult();

        private CompilationResult getCompilationResult() {
            return result;
        }

        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
            CompilationMessage message = new CompilationMessage();
            message.setMessage(diagnostic.getMessage(Locale.ENGLISH));
            message.setLine(diagnostic.getLineNumber());
            message.setColumn(diagnostic.getColumnNumber());
            message.setSource(diagnostic.getSource() != null ? diagnostic.getSource().getName() : null);

            switch (diagnostic.getKind()) {
            case ERROR: {
                result.getErrors().add(message);
                break;
            }
            case WARNING:
            case MANDATORY_WARNING: {
                result.getWarnings().add(message);
                break;
            }
            case NOTE:
                break;
            case OTHER:
                break;
            default:
                break;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Code->" + diagnostic.getCode());
                logger.debug("Column Number->" + diagnostic.getColumnNumber());
                logger.debug("End Position->" + diagnostic.getEndPosition());
                logger.debug("Kind->" + diagnostic.getKind());
                logger.debug("Line Number->" + diagnostic.getLineNumber());
                logger.debug("Message->" + diagnostic.getMessage(Locale.ENGLISH));
                logger.debug("Position->" + diagnostic.getPosition());
                logger.debug("Source" + diagnostic.getSource());
                logger.debug("Start Position->" + diagnostic.getStartPosition());
            }
        }
    }

    static class CompiledUnit {
        String className;
        File classFile;
        File srcFile;
    }

    public static class CompilationFailedException extends BGException {
        private CompilationResult result;

        public CompilationResult getCompilationResult() {
            return result;
        }

        public CompilationFailedException(CompilationResult result) {
            this.result = result;
        }
    }
}