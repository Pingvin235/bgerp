package org.bgerp.custom.java;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;

import org.apache.commons.io.FileUtils;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGException;
import org.bgerp.util.Log;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import ru.bgcrm.model.Pair;
import ru.bgcrm.util.Utils;

/**
 * Java compiler wrapper.
 *
 * @author Kirill Berezin
 * @author Kirill Sergeev
 * @author Amir Absalilov
 * @author Denis Pimenov
 * @author Ildar Fattakhov
 * @author Artur Gareev
 * @author Boris Fedorako
 * @author Shamil Vakhitov
 */
public class CompilerWrapper {
    private static final Log log = Log.getLog();

    private static final String DIR_PREFIX = "bgerp-";

    private final File srcDir;
    private final File outputDir;

    public CompilerWrapper(File srcDir) {
        this.srcDir = srcDir;
        var outputDirRoot = new File(Utils.getTmpDir());
        removeOldTmpDirs(outputDirRoot);
        outputDir = new File(outputDirRoot, DIR_PREFIX + System.currentTimeMillis());
        outputDir.mkdir();
    }

    private void removeOldTmpDirs(File outputDirRoot) {
        try {
            for (File dir : outputDirRoot.listFiles(f -> f.isDirectory() && f.getName().startsWith(DIR_PREFIX))) {
                log.info("Removing: {}", dir);
                FileUtils.deleteDirectory(dir);
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    public File getOutputDir() {
        return outputDir;
    }

    /**
     * Компилирует поданные исходники
     *
     * @param srcFiles список полных путей к файлам
     * @return пара: результат компиляции и список скомпилированных файлов
     */
    public Pair<CompilationResult, List<CompiledUnit>> compile(List<String> srcFiles) {
        String encoding = Setup.getSetup().get("custom.src.encoding", StandardCharsets.UTF_8.name());

        String[] options;
        if (Utils.notBlankString(encoding)) {
            options = new String[] { "-classpath", getClasspath(), "-sourcepath", srcDir.getAbsolutePath(), "-encoding",
                    encoding, "-d", outputDir.getAbsolutePath(), "-verbose", "-Xlint:deprecation" };
        } else {
            options = new String[] { "-classpath", getClasspath(), "-sourcepath", srcDir.getAbsolutePath(), "-d",
                    outputDir.getAbsolutePath(), "-verbose", "-Xlint:deprecation" };
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticListenerImpl listener = new DiagnosticListenerImpl();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(listener, null, null);

        Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjectsFromStrings(srcFiles);

        StringWriter sw = new StringWriter();
        CompilationTask task = compiler.getTask(sw, fileManager, listener, Arrays.asList(options), null, fileObjects);

        boolean callResult = task.call();
        CompilationResult result = listener.getCompilationResult();
        result.setResult(callResult);

        return new Pair<CompilationResult, List<CompiledUnit>>(result, compiledFiles(outputDir));
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

                String className = filePath.substring(outputDir.getAbsolutePath().length() + 1).replace(File.separatorChar, '.');
                className = className.substring(0, className.length() - 6);

                log.debug("Found compiled unit: {} with file {}", className, compiledFile);

                CompiledUnit unit = new CompiledUnit();
                unit.className = className;
                unit.classFile = compiledFile;
                unit.srcFile = getClassSrc(className);
                result.add(unit);
            }
            // если папка, то проверяем ее содержимое
            else if (tmpdirFile.isDirectory()) {
                result.addAll(compiledFiles(tmpdirFile));
            }
        }

        return result;
    }

    protected File getClassSrc(String className) {
        return null;
    }

    /*
     * удаляет директорию outputdir, в которой находятся скомпилированные файлы
     */
    public void deleteClassDir() {
        if (outputDir != null && outputDir.exists()) {
            deleteDirWithFiles(outputDir);
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
                    result.addError(message);
                    break;
                }
                case WARNING:
                case MANDATORY_WARNING: {
                    result.addWarning(message);
                    break;
                }
                case NOTE:
                    break;
                case OTHER:
                    break;
                default:
                    break;
            }

            if (log.isDebugEnabled()) {
                log.debug("Code->" + diagnostic.getCode());
                log.debug("Column Number->" + diagnostic.getColumnNumber());
                log.debug("End Position->" + diagnostic.getEndPosition());
                log.debug("Kind->" + diagnostic.getKind());
                log.debug("Line Number->" + diagnostic.getLineNumber());
                log.debug("Message->" + diagnostic.getMessage(Locale.ENGLISH));
                log.debug("Position->" + diagnostic.getPosition());
                log.debug("Source" + diagnostic.getSource());
                log.debug("Start Position->" + diagnostic.getStartPosition());
            }
        }
    }

    public static class CompiledUnit {
        public String className;
        public File classFile;
        public File srcFile;
    }

    @Deprecated
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