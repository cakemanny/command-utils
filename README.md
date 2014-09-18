# Command Utils

_A simple bootstrap for commandline java applications_

See the com.cakemanny.app.Example class for an idea of how to use:

```java
import java.io.File;
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
```

Then run with
```
> java -cp ".:Command.jar" -Dprog.name=Example \
                 -Dprog.className=com.cakemanny.app.Example \
                 com.cakemanny.app.App
```

- Idea for improvement: add a quick classpath scan for implementor of App
  to save having to pass the full class name as an option


