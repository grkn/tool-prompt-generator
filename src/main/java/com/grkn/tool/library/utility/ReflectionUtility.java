package com.grkn.tool.library.utility;

import com.grkn.tool.library.annotation.Tool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class ReflectionUtility {

    private ReflectionUtility() {}

    public static List<Class> scanToolClassesForToolInformation(String scanPackage) throws ClassNotFoundException, IOException {
        String filePathOfPackage = scanPackage.replace(".", "/");
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(filePathOfPackage);
        List<Class> classList = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            File f = new File(url.getFile());
            String[] fileNames = f.list();
            if (fileNames != null) {
                for (String name : fileNames) {
                    if (name.endsWith(".class")) {
                        classList.add(Class.forName(scanPackage + "." + name.replace(".class", "")));
                    }
                }
            }
        }
        return classList;
    }

    public static List<Method> getToolMethodsInToolClass(List<Class> classList) {
        List<Method> toolMethods = new ArrayList<>();
        classList.stream().filter(aClass -> aClass.getDeclaredAnnotation(Tool.class) != null)
                .map(aClass -> Arrays.stream(aClass.getDeclaredMethods())
                        .filter(method -> method.getDeclaredAnnotation(Tool.class) != null).toList())
                .toList().forEach(toolMethods::addAll);
        return toolMethods;
    }
}
