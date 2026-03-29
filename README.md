# ToolLibrary

A Java library that generates LLM-compatible tool prompts from annotated Java methods. This library enables developers to define tools (functions) using simple annotations and automatically generate structured prompts that LLM models can use to understand and invoke those tools.

## Features

- **Annotation-based Tool Definition**: Define tools using `@Tool` and `@ToolParameter` annotations
- **Automatic JSON Schema Generation**: Automatically generates JSON input schemas for both simple and complex parameter types
- **Support for Complex Types**: Handles nested objects, collections (List, Set, Queue, Deque), and primitive types
- **Type-safe**: Leverages Java's reflection API to ensure type safety
- **LLM-Ready Prompts**: Generates formatted prompts that can be directly used with LLM models

## Requirements

- Java 21 or higher
- Maven 3.x

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.grkn</groupId>
    <artifactId>ToolLibrary</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### 1. Define Your Tool Class

Create a class and annotate it with `@Tool`:

```java
import com.grkn.tool.library.annotation.Tool;
import com.grkn.tool.library.annotation.ToolParameter;

@Tool
public class MyTools {

    @Tool(name = "READ_FILE", description = """
            Reads the content of a file from the file system.
            Returns the file name.
            """)
    public String readFile(@ToolParameter(description = "The file path to read")
                          String filePath) {
        File f = new File(filePath);
        return f.getName();
    }
}
```

### 2. Generate Tool Prompt

Use the `ToolManager` to generate an LLM-ready prompt:

```java
import com.grkn.tool.library.core.Instances;

public class Main {
    public static void main(String[] args) {
        String toolPrompt = Instances.TOOL_MANAGER
            .getToolManager()
            .prepareToolPrompt("com.mypackage.tools");

        System.out.println(toolPrompt);
    }
}
```

### 3. Generated Output

The library generates a structured prompt like:

```
Your goal is to complete the requested task using available tools.

Available Tools
- READ_FILE: Reads the content of a file from the file system.
            Returns the file name.

Tool input must be JSON shape when you use READ_FILE tool

JSON shape:
{
    "filePath":"The file path to read"
}
```

## Advanced Usage

### Complex Parameter Types

For complex objects with nested fields:

```java
@Tool
public class AdvancedTools {

    @Tool(name = "CREATE_USER", description = "Creates a new user in the system")
    public UserResponse createUser(@ToolParameter(description = "User creation request")
                                   UserRequest request) {
        // Implementation
        return new UserResponse();
    }
}

public class UserRequest {
    @ToolParameter(description = "User's full name")
    private String name;

    @ToolParameter(description = "User's age")
    private int age;

    @ToolParameter(description = "User's email address")
    private String email;

    @ToolParameter(description = "User's address details")
    private Address address;

    static class Address {
        @ToolParameter(description = "Street name")
        private String street;

        @ToolParameter(description = "City name")
        private String city;
    }
}
```

The library will automatically generate a nested JSON schema:

```json
{
  "name": "User's full name",
  "age": "User's age",
  "email": "User's email address",
  "address": {
    "street": "Street name",
    "city": "City name"
  }
}
```

### Working with Collections

The library supports collection types:

```java
public class UserRequest {
    @ToolParameter(description = "List of user hobbies")
    private List<String> hobbies;

    @ToolParameter(description = "List of addresses")
    private List<Address> addresses;
}
```

## Annotations Reference

### `@Tool`

Applied to classes and methods to mark them as tools.

**Attributes:**
- `name` (String): The unique identifier for the tool (default: empty string)
- `description` (String): Human-readable description of what the tool does (default: empty string)

**Target:** TYPE, METHOD
**Retention:** RUNTIME

### `@ToolParameter`

Applied to method parameters and fields to describe tool input parameters.

**Attributes:**
- `description` (String, required): Description of the parameter for the LLM

**Target:** PARAMETER, FIELD
**Retention:** RUNTIME

## Rules and Constraints

1. **Single Tool Parameter**: Each `@Tool` annotated method must have exactly one parameter annotated with `@ToolParameter`
2. **Class-level Annotation**: The containing class must be annotated with `@Tool` (can be without attributes)
3. **Package Scanning**: Tools are discovered by scanning packages at runtime
4. **Supported Types**:
   - Primitives: `boolean`, `char`, `byte`, `short`, `int`, `long`, `float`, `double`
   - Wrappers: `Boolean`, `Character`, `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`, `String`
   - Collections: `List`, `Set`, `Queue`, `Deque` (Map is not supported)
   - Custom objects with `@ToolParameter` annotated fields

## Architecture

### Core Components

- **`ToolManager`**: Interface for generating tool prompts
- **`DefaultToolManager`**: Implementation that scans packages and generates prompts
- **`Instances`**: Enum providing singleton access to `ToolManager`
- **`JsonParserTree`**: Internal tree structure for generating nested JSON schemas
- **`ParserTree`**: Base tree implementation

### How It Works

1. **Scanning**: The library scans the specified package for classes annotated with `@Tool`
2. **Method Discovery**: Finds all methods within those classes that have the `@Tool` annotation
3. **Parameter Analysis**: Analyzes method parameters annotated with `@ToolParameter`
4. **Schema Generation**: Generates JSON schemas based on parameter types:
   - Simple types: Creates simple key-value descriptions
   - Complex types: Recursively analyzes fields to create nested structures
   - Collections: Detects generic types and generates appropriate schemas
5. **Prompt Formatting**: Combines all information into an LLM-ready prompt

## Best Practices

1. **Descriptive Names**: Use clear, descriptive names for tools
2. **Detailed Descriptions**: Provide comprehensive descriptions for both tools and parameters
3. **Keep It Simple**: Design tool parameters to be as simple as possible
4. **Package Organization**: Group related tools in the same package
5. **Validation**: Implement proper validation in your tool methods
6. **Error Handling**: Handle exceptions gracefully in tool implementations

## Testing

Run the test suite:

```bash
mvn test
```

The library includes comprehensive tests in `ToolAnnotationTest` that verify:
- Simple parameter handling
- Complex nested object handling
- Collection support
- JSON schema generation

## Example Project Structure

```
src/main/java/
├── com/myproject/
    ├── tools/
    │   ├── FileTools.java
    │   ├── DatabaseTools.java
    │   └── models/
    │       ├── FileRequest.java
    │       └── DatabaseRequest.java
    └── Main.java
```

## Limitations

- Map implementations are not supported
- Requires Java 21+ due to language features used
- Package scanning only works with classes on the classpath
- Circular references in nested objects are not detected

## Contributing

Contributions are welcome! Please ensure:
- All tests pass
- Code follows existing style conventions
- New features include tests
- Documentation is updated

## License

[Specify your license here]

## Support

For issues, questions, or contributions, please visit the project repository.

---

**Author**: grkn
**Version**: 1.0-SNAPSHOT
