package com.grkn.tool.library.core;

import java.util.List;

public interface ParserTree {

    void addAll(JsonParserTree.TNode parent, List<JsonParserTree.TNode> children);
    JsonParserTree.TNode findByNameAndClazz(String name, Class clazz);
    String createContentAsString(JsonParserTree.TNode tNode);
}
