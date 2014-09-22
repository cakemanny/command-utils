package com.cakemanny.app;

import java.io.File;

/**
 * An example of how to use the App class.
 * <p>
 * How to run: java -cp ".[:;]Command.jar" -Dprog.name=Example \
 *                  com.cakemanny.app.App
 *
 * @author Daniel Golding
 */
public class Example implements App {

    String poop = App.option("poop", "Hi", "The required poop. A long description message");
    int port = App.option("port", 8912, "The port to listen on");
    File file = App.option("file", new File("."), "The file ya know!");

    public void main() {

        System.out.println("hello");
        System.out.println("port=" + port);
        System.out.println("poop=" + poop);
        System.out.println("file=" + file);
        System.out.println("file.exists()=" + file.exists());

    }

}

