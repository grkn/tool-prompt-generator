package com.grkn.tool.library.core;

import com.grkn.tool.library.annotation.ToolParameter;

import java.lang.reflect.Field;
import java.util.*;

public class JsonParserTree implements ParserTree {
    private TNode root = new TNode(null, null, null);

    private static final Set<Class<?>> COLLECTION_INTERFACES = Set.of(
            List.class,
            Set.class,
            Queue.class,
            Deque.class
    );

    @Override
    public void addAll(TNode parent, List<TNode> children) {
        if (parent == root) {
            root.getChildren().addAll(children);
        } else {
            TNode tNode = findByNameAndClazz(parent.getField().getName(), parent.getField().getType());
            tNode.getChildren().addAll(children);
        }
    }

    @Override
    public TNode findByNameAndClazz(String name, Class clazz) {
        TNode tmp = root;
        Queue<TNode> queue = new LinkedList<>();
        queue.add(tmp);
        while (!queue.isEmpty()) {
            tmp = queue.poll();

            if (tmp.getField() != null && tmp.getField().getName().equals(name) && tmp.getField().getType().equals(clazz)) {
                return tmp;
            }

            if (!isLeaf(tmp)) {
                for (TNode child : tmp.getChildren()) {
                    queue.offer(child);
                }
            }
        }
        return null;
    }

    private String createJson(TNode tNode) {
        boolean isCollection = tNode.getField() != null
                && (Collection.class.isAssignableFrom(tNode.getField().getType())
                || COLLECTION_INTERFACES.contains(tNode.getField().getType()));

        String key = tNode.getField().getName();
        String value = getDescription(tNode, key);

        if (isCollection) {
            return """
                    "%s": ["%s"]
                    """.formatted(key, value);
        }

        return """
                "%s": "%s"
                """.formatted(key, value);
    }

    private String createNestedObject(TNode tNode) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        List<TNode> children = tNode.getChildren();
        for (int i = 0; i < children.size(); i++) {
            TNode child = children.get(i);
            boolean isLeaf = isLeaf(child);

            if (isLeaf) {
                json.append(createJson(child).trim());
            } else {
                boolean isCollection = child.getField() != null
                        && (Collection.class.isAssignableFrom(child.getField().getType())
                        || COLLECTION_INTERFACES.contains(child.getField().getType()));

                if (isCollection) {
                    json.append("\"")
                            .append(child.getField().getName())
                            .append("\": [")
                            .append(createNestedObject(child))
                            .append("]");
                } else {
                    json.append("\"")
                            .append(child.getField().getName())
                            .append("\": ")
                            .append(createNestedObject(child));
                }
            }

            if (i < children.size() - 1) {
                json.append(", ");
            }
        }

        json.append("}");
        return json.toString();
    }

    @Override
    public String createContentAsString(TNode tNode) {
        boolean isCollection = tNode.getField() != null && Collection.class.isAssignableFrom(tNode.getField().getType());

        if (isCollection) {
            return """
                    [%s]
                    """.formatted(createNestedObject(tNode));
        }

        return createNestedObject(tNode);
    }

    private static String getDescription(TNode tmp, String variableName) {
        ToolParameter toolParameter = tmp.getField().getDeclaredAnnotation(ToolParameter.class);
        if (toolParameter == null) {
            throw new IllegalArgumentException(String.format("Parameter: %s must implement ToolParameter", variableName));
        }
        return toolParameter.description();
    }

    private static boolean isLeaf(TNode tNode) {
        return tNode.getChildren() == null || tNode.getChildren().isEmpty();
    }

    public TNode getRoot() {
        return root;
    }

    public void setRoot(TNode root) {
        this.root = root;
    }

    public static class TNode {
        private Field field;
        private List<TNode> children;
        private TNode parent;

        @Override
        public String toString() {
            return "TNode{" +
                    "field=" + field == null ? "root" : field.getName() +
                    ", children=" + children +
                    '}';
        }

        public TNode(Field field, List<TNode> children, TNode parent) {
            this.field = field;
            this.children = children;
            this.parent = parent;
        }

        public List<TNode> getChildren() {
            if (children == null) {
                children = new ArrayList<>();
            }
            return children;
        }

        public void setChildren(List<TNode> children) {
            this.children = children;
        }

        public Field getField() {
            return field;
        }

        public void setField(Field field) {
            this.field = field;
        }

        public TNode getParent() {
            return parent;
        }

        public void setParent(TNode parent) {
            this.parent = parent;
        }
    }
}
