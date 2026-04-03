package com.grkn.tool.library.test;

import com.grkn.tool.library.core.Instances;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ToolAnnotationTest {

    @Test
    public void test() {
        String descriptionPrompt = Instances.TOOL_MANAGER.getToolManager().prepareToolPrompt("com.grkn.tool.library.resources");
        String result = """
Your goal is to complete the requested task using available tools.

Available Tools
1- Tool Name: READ_FILE
	- Tool Description: Goal: Read file
.....
.....

Tool input must be JSON shape when you use READ_FILE tool

JSON shape:
{
    "toolPayload":"Payload is filePath you need to read
"
}


2- Tool Name: READ_FILE2
	- Tool Description: Goal: Read file
.....
.....

Tool input must be JSON shape when you use READ_FILE2 tool

JSON shape:
{"name": "name of user", "inner": {"innerName": "hidden name of user", "surname": "hidden surname of user", "innerExampleClazz2": {"innerName2": "hidden name2 of user"}}, "age": "age of user", "sum": "sum of something", "test": ["test of something"], "testOfInners": [{"innerName2": "hidden name2 of user"}]}""";
        Assertions.assertEquals(result.trim(), descriptionPrompt.trim());
    }
}
