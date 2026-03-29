package com.grkn.tool.library.core;


import com.grkn.tool.library.annotation.Tool;
import com.grkn.tool.library.annotation.ToolParameter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

public final class DefaultToolManager implements ToolManager {

    private static final Set<Class<?>> WRAPPER_TYPES = Set.of(
            Boolean.class, Character.class, Byte.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class, Void.class, String.class
    );

    private static final Set<Class<?>> COLLECTION_INTERFACES = Set.of(
            List.class,
            Set.class,
            Queue.class,
            Deque.class
    );


    private DefaultToolManager() {
    }

    static synchronized DefaultToolManager getInstance() {
        return new DefaultToolManager();
    }

    @Override
    public String prepareToolPrompt(String scanPackage) {
        try {
            List<Class> classList = getClassesForToolInformation(scanPackage);
            Map<String, ToolData> toolMap = extractedToolInformationFromPackageScan(classList);
            return """
                    Your goal is to complete the requested task using available tools.
                    
                    Available Tools
                    %s
                   
                    """.formatted(describeTools(toolMap));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Class> getClassesForToolInformation(String scanPackage) throws ClassNotFoundException, IOException {
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

    public static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    private String describeTools(Map<String, ToolData> map) {
        StringBuilder sb = new StringBuilder();
        for (ToolData tool : map.values()) {
            sb.append("- ").append(tool.getName())
                    .append(": ")
                    .append(tool.getDescription())
                    .append("\n")
                    .append(tool.getToolPayloadDescription())
                    .append("\n");
        }
        return sb.toString();
    }

    private static Map<String, ToolData> extractedToolInformationFromPackageScan(List<Class> classList) {
        final Map<String, ToolData> toolDataMap = new HashMap<>();
        classList.stream().filter(DefaultToolManager::containsToolAnnotationForClass)
                .map(DefaultToolManager::listOfMethodsThatContainToolAnnotation)
                .forEach(methods ->
                        methods.forEach(method -> iterateOverEachToolParameterAnnotationForToolInput(method, toolDataMap)
                        ));

        return toolDataMap;
    }

    private static void iterateOverEachToolParameterAnnotationForToolInput(Method method, Map<String, ToolData> toolDataMap) {
        Tool tool = method.getDeclaredAnnotation(Tool.class);

        List<Parameter> list = Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.getDeclaredAnnotation(ToolParameter.class) != null)
                .toList();

        if (list.size() != 1) {
            throw new IllegalArgumentException("ToolParameter must be one for tool payload");
        }
        Parameter parameter = list.get(0);

        if (isWrapperType(parameter.getType()) || parameter.getType().isPrimitive()) {
            final String parameterDescription = getParameterDescriptionForSimpleClass(parameter, tool);
            toolDataMap.put(tool.name(), new ToolData(tool.name(), tool.description(), parameterDescription));
        } else {
            final String parameterDescription = getParameterDescriptionForMixedPayload(parameter, tool);
            toolDataMap.put(tool.name(), new ToolData(tool.name(), tool.description(), parameterDescription));
        }
    }

    private static String getParameterDescriptionForMixedPayload(Parameter parameter, Tool tool) {
        Queue<QNode> queue = new LinkedList<>();
        QNode root = new QNode(parameter.getName(), parameter.getType(), null, null);
        queue.add(root);
        JsonParserTree parserTree = generateJsonShape(queue, new JsonParserTree());

        return """
                
                Tool input must be JSON shape when you use %s tool
                
                JSON shape:
                %s
                
                """
                .formatted(tool.name(), parserTree.createContentAsString(parserTree.getRoot()));
    }

    private static JsonParserTree generateJsonShape(Queue<QNode> queue, JsonParserTree parserTree) {
        while (!queue.isEmpty()) {
            QNode tmp = queue.poll();

            List<Field> fields = getFieldsExceptMapImplementations(tmp);

            for (Field field : fields) {
                if (isWrapperType(field.getType()) || field.getType().isPrimitive()) {
                    findAndInsertIntoTree(parserTree, field, tmp);
                } else {
                    findAndInsertIntoTree(parserTree, field, tmp);
                    queue.offer(new QNode(field.getName(), field.getType(), tmp, field.getGenericType()));
                }
            }

        }
        return parserTree;
    }

    private static List<Field> getFieldsExceptMapImplementations(QNode tmp) {
        List<Field> fields = List.of(tmp.getClazz().getDeclaredFields());

        boolean isCollection = Collections.class.isAssignableFrom(tmp.getClazz())
                || COLLECTION_INTERFACES.contains(tmp.getClazz());
        Type genericType = tmp.getGenericType();

        if (isCollection && genericType instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            //Support for Collections and not support map implementations
            Type type = typeArguments[0];
            if (type instanceof Class<?> typeArgClass && !isWrapperType(typeArgClass)) {
                fields = List.of(typeArgClass.getDeclaredFields());
            }
        }
        return fields;
    }

    private static void findAndInsertIntoTree(JsonParserTree parserTree, Field field, QNode tmp) {
        JsonParserTree.TNode tNode = parserTree.findByNameAndClazz(tmp.getName(), tmp.getClazz());
        if (tNode == null) {
            parserTree.addAll(parserTree.getRoot(), List.of(new JsonParserTree.TNode(field, null, null)));
        } else {
            boolean isCollection = Collections.class.isAssignableFrom(field.getType())
                    || COLLECTION_INTERFACES.contains(field.getType());
            Type genericType = field.getGenericType();
            if (isCollection && genericType instanceof ParameterizedType parameterizedType) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                //Support for Collections and not support map implementations
                Type type = typeArguments[0];
                if (type instanceof Class<?> typeArgClass && !isWrapperType(typeArgClass)) {
                    List<JsonParserTree.TNode> list = new ArrayList<>();
                    for (Field declaredField : List.of(typeArgClass.getDeclaredFields())) {
                        list.add(new JsonParserTree.TNode(declaredField, null, tNode));
                    }
                    parserTree.addAll(tNode, list);
                } else {
                    parserTree.addAll(tNode, List.of(new JsonParserTree.TNode(field, null, tNode)));
                }
            } else {
                parserTree.addAll(tNode, List.of(new JsonParserTree.TNode(field, null, tNode)));
            }
        }
    }

    private static String getParameterDescriptionForSimpleClass(Parameter parameter, Tool tool) {
        String description = parameter.getDeclaredAnnotation(ToolParameter.class).description();
        String variableName = parameter.getName();
        return """
                
                Tool input must be JSON shape when you use %s tool
                
                JSON shape:
                {
                    "%s":"%s"
                }
                
                """
                .formatted(tool.name(),
                        variableName,
                        description);
    }

    private static List<Method> listOfMethodsThatContainToolAnnotation(Class aClass) {
        return Arrays.stream(aClass.getDeclaredMethods())
                .filter(method -> method.getDeclaredAnnotation(Tool.class) != null).toList();
    }

    private static boolean containsToolAnnotationForClass(Class aClass) {
        return aClass.getDeclaredAnnotation(Tool.class) != null;
    }

    private static class QNode {
        private String name;
        private Class clazz;
        private QNode parent;
        private Type genericType;

        public QNode(String name, Class clazz, QNode parent, Type genericType) {
            this.name = name;
            this.clazz = clazz;
            this.parent = parent;
            this.genericType = genericType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Class getClazz() {
            return clazz;
        }

        public void setClazz(Class clazz) {
            this.clazz = clazz;
        }

        public QNode getParent() {
            return parent;
        }

        public void setParent(QNode parent) {
            this.parent = parent;
        }

        public Type getGenericType() {
            return genericType;
        }

        public void setGenericType(Type genericType) {
            this.genericType = genericType;
        }
    }

    private static class ToolData {
        private String name;
        private String description;
        private String toolPayloadDescription;

        public ToolData(String name, String description, String toolPayloadDescription) {
            this.name = name;
            this.description = description;
            this.toolPayloadDescription = toolPayloadDescription;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getToolPayloadDescription() {
            return toolPayloadDescription;
        }

        public void setToolPayloadDescription(String toolPayloadDescription) {
            this.toolPayloadDescription = toolPayloadDescription;
        }

        @Override
        public String toString() {
            return "ToolData{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", toolPayloadDescription=" + toolPayloadDescription +
                    '}';
        }
    }


}
