package com.grkn.tool.library.core;

public interface ToolManager {
    /**
     * Prepare available tools prompt to append main prompt
     * @param scanPackage
     * @return
     */
    String prepareToolPrompt(String scanPackage);
}
