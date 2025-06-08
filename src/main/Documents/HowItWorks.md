# How It Works

## The `renderTemplate` method

### Method signatures: 
- `public void renderTemplate(String path, IOutput outputMethod)`
- `public void renderTemplate(String path, IOutput outputMethod, Map<String, Object> context)`

### Parameters

`path` is the template path.
`outputMethod` is the method for output, like to standard output or write to file.

### Flow

It first reads the file, and calls `run` to render the template.

The `run` method will then:
1. Use `Lexer` to tokenize the source template
2. Pass the result of Lexer to `Parser`, and generate an **Abstract Syntax Tree**
3. The AST is then pass to `Interpreter`, traverse the AST and evaluate each node
4. Finally, the result (of type `byte[]`) is passed to `outputMethod.write()`, to output the final result
