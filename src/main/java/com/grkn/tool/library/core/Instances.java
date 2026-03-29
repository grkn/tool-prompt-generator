package com.grkn.tool.library.core;

public enum Instances {
    TOOL_MANAGER(DefaultToolManager.getInstance());

    private DefaultToolManager toolManager;

    Instances(DefaultToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public DefaultToolManager getToolManager() {
        return toolManager;
    }
}
