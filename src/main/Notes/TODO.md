# TODO

- [ ] Add comments
- [ ] Add JavaDoc
- [Future] The ++ and -- operator
- [Future] The conditional expression

## Done (from recent to old)

- [x] Add documentation
- [x] Clean up code (logs, unuseful comments)
- [x] Provide `ifDefine()` method to check if a variable is defined (or provide from the user) in the template,
  so the template can have default value for some variables, like `{% if(ifDefine(backgroundColor)) backgroundColor else "#007acc" %}`
- [x] Implement the TODOs in NewGrammar.txt
- [x] Implement the `include "something.template"` statement in code block
- [x] Allow user defined types to pass to the template (Parser should recognize that, and Interpreter should handle that as well)
- [x] Provide different output types, standard output as well as write to file
- [x] Implement variable pass in
- [x] Variables can not be declared multiple times in the same block
- [x] Choose "template" as the extension or use others (like "jam")? (Something like: Write some code in your jam file
  would be cool)
