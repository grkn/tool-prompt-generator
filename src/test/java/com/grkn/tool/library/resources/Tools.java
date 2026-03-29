package com.grkn.tool.library.resources;

import com.grkn.tool.library.annotation.Tool;
import com.grkn.tool.library.annotation.ToolParameter;

import java.io.File;

@Tool
public class Tools {

    @Tool(name = "READ_FILE", description = """
            Goal: Read file
            .....
            .....
            """)
    public String execute(@ToolParameter(description = """
            Payload is filePath you need to read
            """) String toolPayload) {
        File f = new File(toolPayload);


        return f.getName();
    }

    @Tool(name = "READ_FILE2", description = """
            Goal: Read file
            .....
            .....
            """)
    public BaseToolResponse execute2(@ToolParameter(description = """
            Payload is filePath you need to read
            """) BaseToolRequest request) {
        return new BaseToolResponse();
    }
}
