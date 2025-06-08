# Introduction

Jam is a html template engine that you can write Java code within the template.

The Jam engine provides:
- A subset of Java language (see [Syntax](./Syntax.md))
- Variable pass-in (environment)
- Built-in function to check variable existence
- Include other template or html file

## Template Example

9x9.jam

```html
<!DOCTYPE html>
<html>
    <head>
        <title>{% if(ifDefine(title)) title else "Multiplication Table" %}</title>
    </head>
    <body>
        <table style="width:100%">
            <tr>
            {% for (int i = 1; i < 10; i+=1) { %}
                <th>{% i %}</th>
            {% } %}
            </tr>
            {% for (int i = 1; i < 10; i+=1) { %}
                <tr>
                    <td>{% i %}</td>
                    {% for (int j = 1; j < 10; j+=1) { %}
                        <td>{% i*j %}</td>
                    {% } %}
                </tr>
            {% } %}
        </table>
    </body>
    {% include "footer.jam" %}
</html>
```

## Usage

Prepare the template file (It is recommended to name it with extension `.jam`)

You could include Jam as an external package to render the template, or execute Jam in the terminal directly.

If you run Jam directly, you can:
- Pass the template path as the first argument, Jam will evaluate it and output to standard output, or
- Run without any argument, Jam will start the interactive shell to let you play with.
