# Nodes

## VarNode
For variable declaration statement
Fields:
- name: Token
- typeName: String
- initializer: Expr

## HtmlNode
For html codes, it will be evaluated as literals
Fields:
- expr: Expr

## BlockNode
List of statements in {}
Fields:
- statements: List<Stmt>

## ExpressionNode
Store the expression
Fields:
- expression: Expr

## IfNode
For `if` statement
Fields:
- condition: Expr
- thenBranch: Stmt
- elseBranch: Stmt

## WhileNode
For `while` statement (and the `for` statement)
Fields:
- condition: Expr
- body: Stmt

## VariableNode
For variable expression
Fields:
- name: Token
- typeName: String

## GetNode
For field access expression
Fields:
- object: Expr
- name: Token

## AssignmentNode
For assignment statement
Fields:
- name: Token
- value: Expr
- operator: Token

## LogicalNode
For logical expressions
Fields:
- left: Expr
- operator: Token
- right: Expr

## BinaryNode
For binary operator expressions
Fields:
- left: Expr
- operator: Token
- right: Expr

## UnaryNode
For unary operator expressions
Fields:
- operator: Token
- right: Expr

## LiteralNode
For literals
Fields:
- value: Object

## GroupingNode
For grouping (like precedence in math expression using parenthesis) 
Fields:
- expression: Expr
