package com.grkn.tool.library.resources;

import com.grkn.tool.library.annotation.ToolParameter;

import java.util.List;

public class BaseToolRequest {
    @ToolParameter(description = "name of user")
    private String name;
    @ToolParameter(description = """
            Inner class to test
            """)
    private InnerExampleClazz inner;

    @ToolParameter(description = "age of user")
    private int age;

    @ToolParameter(description = "sum of something")
    private Float sum;

    @ToolParameter(description = "test of something")
    private List<String> test;

    @ToolParameter(description = "test of inner2 something")
    private List<InnerExampleClazz2> testOfInners;

    static class InnerExampleClazz {
        @ToolParameter(description = "hidden name of user")
        private String innerName;

        @ToolParameter(description = "hidden surname of user")
        private String surname;
        @ToolParameter(description = """
            Inner2 class to test
            """)
        private InnerExampleClazz2 innerExampleClazz2;

    }

    static class InnerExampleClazz2 {
        @ToolParameter(description = "hidden name2 of user")
        String innerName2;
    }

}

