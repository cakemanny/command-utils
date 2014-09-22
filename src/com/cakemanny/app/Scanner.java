package com.cakemanny.app;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class Scanner {

    // VisibleForTesting
    static ClassLoader cl = Scanner.class.getClassLoader();

    private static Predicate<String> implementsInterface(Class<?> clazz) {
        ClassLoader classLoader = cl; // capture for save of thread-safety
        return className -> {
            try {
                return clazz.isAssignableFrom(Class.forName(className, false, classLoader));
            } catch (ClassNotFoundException e) {
                // This should not happen, all classes should exist on the
                // classpath
                throw new RuntimeException(e);
            } catch (NoClassDefFoundError err) {
                // We get a NoClassDefFoundError when attempting to do this for
                // classes in bcprov signed security provider jar
                return true;
            }
        };
    }

    private static String toClassName(String pathName) {
        return pathName.replaceAll("\\.class$","")
            .replaceAll("^\\.","").replace('/','.');
    }

    public static String appClassName() {
        try (Stream<String> apps = apps()) {
            List<String> results = apps.distinct().collect(toList());
            if (results.size() > 1) {
                throw new IllegalStateException("Multiple candidates " +
                        "implement com.cakemanny.app.App:\n" +
                        String.join("\n", results) +
                        "\nPlease specify one using the prog.className " +
                        "system property");
            } else if (results.isEmpty()) {
                throw new NoSuchElementException("Unable to find any classes " +
                        "that implement com.cakemanny.app.App on the classpath");
            } else {
                return results.get(0);
            }
        }
    }

    private static Stream<String> apps() {
        if (cl instanceof URLClassLoader) {
            URLClassLoader ucl = (URLClassLoader) cl;

            return Stream.of(ucl.getURLs())
            .map(propagating(url -> Paths.get(url.toURI())))
            .flatMap(propagating(path -> {
                if (Files.isRegularFile(path)) {
                    return zipContents(path);
                } else if (Files.isDirectory(path)) {
                    return Files.walk(path)
                        .map(subpath -> path.relativize(subpath))
                        .map(subpath -> subpath.toString())
                        .filter(subpath -> subpath.endsWith(".class"))
                        .map(Scanner::toClassName);
                } else {
                    return Stream.empty();
                }
            }))
            .filter(x -> !x.startsWith("com.cakemanny.app."))
            .filter(implementsInterface(App.class));
        } else {
            return Stream.empty();
        }
    }

    static interface ExplosiveFunc<T, R> {
        R apply(T t) throws Exception;
    }
    static interface ExplosivePred<T> {
        boolean test(T t) throws Exception;
    }

    private static <T> T explode(java.util.concurrent.Callable<T> x) {
        try {
            return x.call();
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            else throw new RuntimeException(e);
        }
    }
    private static <T> Predicate<T> propagatingPred(ExplosivePred<T> pred) {
        return s -> explode(() -> pred.test(s));
    }

    private static <S,T> Function<S, T> propagating(ExplosiveFunc<S, T> func) {
        return s -> explode(() -> func.apply(s));
    }

    // Stream of classNames
    private static Stream<String> zipContents(Path p) throws IOException {
        JarFile jarFile = new JarFile(p.toFile());
        return jarFile.stream()
            .map(entry -> entry.toString())
            .filter(path -> path.endsWith(".class"))
            .map(Scanner::toClassName)
            .onClose(() -> { try { jarFile.close(); } catch (IOException e) {}});
    }

    private Scanner() { throw new UnsupportedOperationException(); }
}

