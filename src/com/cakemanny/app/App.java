package com.cakemanny.app;

import java.util.*;
import java.util.function.*;
import java.io.PrintStream;
import java.util.stream.Collectors;

/**
 * A library for bootstrapping commandline apps and parsing commandline options
 *
 * @author Daniel Golding
 */
public interface App {


    /**
     * The main method of the app.
     * This is run after all of the options are processed!
     */
    abstract public void main();

    /**
     * Prints all the commandline options and their descriptions
     */
    default public void printUsageAndExit(int exitCode) {
        PrintStream ps = (exitCode == 0) ? System.out : System.err;
        String progName = System.getProperty("prog.name");
        ps.println("  usage: " + progName + " [options]");
        ps.println();
        ps.println("Options:");
        List<Pair<String,String>> options = AppState.formattedOptions();
        int maxLeftLength =
            options.stream().map(x -> x.l.length()).reduce(0, Integer::max);
        int maxRightLength =
            options.stream().map(x -> x.r.length()).reduce(0, Integer::max);

        int leftMargin = (maxRightLength < 40)
            ? Integer.max(maxLeftLength, 40)
            : Integer.max(maxLeftLength, 80 - maxRightLength);

        ps.println("  --help" +
                    Strings.repeat(" ", (leftMargin - "  --help".length()) + 1) +
                    "Display this help message");
        for (Pair<String, String> optPair : options) {
            ps.println(optPair.l +
                    Strings.repeat(" ", (leftMargin - optPair.l.length()) + 1) +
                    optPair.r);
        }
        System.exit(exitCode);
    }

    public static void main(String[] args) {
        try {
            AppState.args = args;
            AppState.getOption = Options.getOption(args);

            // instantiate App instance
            String classNameProp = System.getProperty("prog.className");
            String className = (classNameProp != null)
                ? classNameProp
                : Scanner.appClassName();
            App app = (App) Class.forName(className).newInstance();
            if (System.getProperty("prog.name") == null) {
                System.setProperty("prog.name", app.getClass().getSimpleName());
            }
            if (Arrays.asList(args).contains("--help")) {
                app.printUsageAndExit(0);
            }
            // Parse arguments

            // run App.main()
            app.main();

            // run exit hooks?

            // exit
        } catch (ClassNotFoundException
               | InstantiationException
               | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Call this in the app to set the class
     */
    public static <T> T option(String name, T defaultValue, String description) {
        Class<?> type = defaultValue.getClass();
        AppState.options.add(new Option<T>(name, defaultValue, description));
        @SuppressWarnings("unchecked")
        T optionValue  = AppState.getOption.apply(name)
            .map(x -> {
                return (T) ResourceInstantiator.instantiate(type, x);
            })
            .orElse(defaultValue);
        return optionValue;
    }
}

class AppState {

    static String[] args = { };

    static List<Option<?>> options = new ArrayList<>();

    static Function<String, Optional<String>> getOption = Options.getOption(args);

    static List<Pair<String, String>> formattedOptions() {
        return options.stream().map(option -> Pair.of(
            "  --" + option.name + "=<" + option.defaultValue + ">",
            option.description
        )).collect(Collectors.toList());
    }

}

class Option<T> {
    final String name;
    final T defaultValue;
    final String description;
    Option(String name, T defaultValue, String description) {
        this.name = Objects.requireNonNull(name);
        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.description = Objects.requireNonNull(description);
    }
}

class Strings {
    public static String repeat(String str, int numTimes) {
        if (numTimes < 0) throw new IllegalArgumentException("numTimes < 0");
        StringBuilder sb = new StringBuilder();
        while (0 < numTimes--)
            sb.append(str);
        return sb.toString();
    }
}

class Pair<L,R> {
    final L l;
    final R r;
    private Pair(L l, R r) { this.l = l; this.r = r; }
    public static <L,R> Pair<L,R> of(L l, R r) { return new Pair<L, R>(l, r); }
}

class Options {

    static Function<String, Optional<String>> getOption(String[] args) {
        return (optionName) -> getOptionRec(Arrays.asList(args), optionName);
    }
    private static Optional<String> getOptionRec(List<String> argList, String optName) {
        if (argList.isEmpty())
            return Optional.empty();
        if (argList.get(0).equals("--" + optName))
            return (argList.size() > 1) ? Optional.of(argList.get(1)) : Optional.empty();
        if (argList.get(0).startsWith("--" + optName))
            return Optional.of(argList.get(0).split("=")[1]);
        return getOptionRec(argList.subList(1, argList.size()), optName);
    }

}

