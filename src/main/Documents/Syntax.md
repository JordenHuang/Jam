# Syntax

Syntax of the Jam template

## Java Code Block

Java code could only be written in the block enclosed with `{%` and `%}`,
as well as the template specific syntax like `include`.

Otherwise, it would be treated as normal html code. 

## Keywords

- int     
- double
- char
- boolean
- Integer
- Double
- Character
- Boolean
- String
- null
- true
- false
- if
- else
- for
- while
- return
- include

## Types

Supported Java primitive types:
- int / Integer
- double / Double
- char / Character
- String
- boolean / Boolean

Above types could be declared and initialized in template.

User defined types (classes) could be passed-in, but can not be declared and initialized.

## Operators

`+`, `-`, `*`, `/`, `%`, `.`, `?`,
`!`, `!=`, `=`, `==`, `>`, `>=`, `<`, `<=`,
`+=`, `-=`, `*=`, `/=`,
`&&`, `||`

## Value Evaluation

Use only the variable name in the Java code block.

e.g.
```html
{% int n = 0; %}
{% n %}
```

Strings are also supported.
```html
{% "This is a string" %}
```

Or if user pass-in a variable of type `Student`.
e.g.
```html
{% student.id %}
{% student.name %}
```

## Field access

Access fields of a variable.

e.g.
```html
{% array.length %}
{% weather.temperature %}
```

## Arrays

Array access are supported, but array declaration are NOT.

e.g.
```html
{% for (int i = 0; i < array.length; i+=1) { %}
    {% i %}
{% } %}
```

## Conditions

`if ... else ...` statements are supported.

## Flows

`while` and c-styled `for` loops are supported.

## Comments

Single line comment are supported.

**Limitation**

The ended code block delimiter needs to be in different line.

e.g.
```html
{%
// This is a line comment
%}
```

## Built-in Function `ifDefine`

Use to check if a variable is in environment or not.

e.g.
```html
{% if (ifDefine(fontSize)) { %}
    {% 20 %}px
{% } else { %}
    {% fontSize %}px
{% } %}
```

## Include

Include other template file or html file.

e.g.
```html
{% include "body.jam" %}
```

```html
{% include "footer.html" %}
```

<hr>

## NOT Yet Supported

- Array declaration
- `++` and `--` operator
- conditional expression, ` ? : `
