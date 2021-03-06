package me.urielsalis.cursebot.extensions;

import me.urielsalis.cursebot.api.Util;
import me.urielsalis.cursebot.extensions.Profanity.Main;
import org.apache.commons.collections4.bag.SynchronizedSortedBag;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class ExtensionHandler {
    public static ExtensionApi api = new ExtensionApi();
    public void init() {
        loadJars();
        Configuration configuration = new ConfigurationBuilder().addUrls(ClasspathHelper.forJavaClassPath());

        Reflections reflections = new Reflections(configuration);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Extension.class);
        for(Class clazz : annotated) {
            while (clazz != Object.class) {
                // need to iterated thought hierarchy in order to retrieve methods from above the current instance
                // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
                final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(clazz.getDeclaredMethods()));
                for (final Method method : allMethods) {
                    if (method.isAnnotationPresent(ExtensionInit.class)) {
                        try {
                            method.invoke(null, api); //invoker is null as its static
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            Util.logger.log(Level.INFO, "Error while trying to run method");
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                }
                // move to the upper class in the hierarchy in search for more methods
                clazz = clazz.getSuperclass();
            }
        }
    }

    @Retention(value = RetentionPolicy.RUNTIME)
    @Target(value = ElementType.METHOD)
    public @interface ExtensionInit {
        String value();
    }

    private static void loadJars() {
        File directory = new File("extensions");
        File[] files = directory.listFiles((dir, name) -> new File(dir, name).isDirectory());

        for (int i = 0; i < files.length; i++)
        {
            files[i] = new File(files[i].toPath() + File.separator + files[i].listFiles()[0].getName());
        }

        for (File file : files) {
            try {
                Util.logger.log(Level.INFO, "Loading .jar: " + file.getName());
                ClassPathHacker.addFile(file);
            } catch (IOException e) {
                Util.logger.log(Level.SEVERE, "This should never happen, this is bad");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
