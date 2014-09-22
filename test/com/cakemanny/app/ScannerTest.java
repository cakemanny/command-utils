package com.cakemanny.app;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import org.junit.Test;

public class ScannerTest {

    @Test(expected = NoSuchElementException.class)
    public void survivesSecurityProviderOnClassPath() throws Exception {
        Scanner.cl = createClassLoader("test-files/bcprov-jdk15on-151.jar");
        Scanner.appClassName(); // should throw
    }

    private URLClassLoader createClassLoader(String jarPath) throws Exception {
        URL[] urls = { Paths.get(jarPath).toUri().toURL() };
        return new URLClassLoader(urls);
    }

    @Test(expected = NoSuchElementException.class)
    public void avoidsExampleInOurPackage() throws Exception {
        // contains com.cakemanny.app.Example
        Scanner.cl = createClassLoader("test-files/app.Example.jar");
        Scanner.appClassName(); // throw
    }

    @Test
    public void findsExampleInOtherPackage() throws Exception {
        Scanner.cl = createClassLoader("test-files/example.Example.jar");
        assertThat(Scanner.appClassName(), is("com.cakemanny.example.Example"));
    }

    @Test(expected = IllegalStateException.class)
    public void throwsIllegalStateExceptionOnMultipleImplementors() throws Exception {
        Scanner.cl = createClassLoader("test-files/multiple.Examples.jar");
        Scanner.appClassName(); // throw
    }

}

