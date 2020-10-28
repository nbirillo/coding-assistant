# PsiTreeConverter description

## Table of Contents:

- [Short description](#short-description)
- [GumTree labels](#gumtree-labels)

## Short description

This document contains the test cases description for the converter from 
[PSI](https://jetbrains.org/intellij/sdk/docs/basics/architectural_overview/psi.html) to GumTree 
[tree](https://github.com/GumTreeDiff/gumtree/blob/develop/core/src/main/java/com/github/gumtreediff/tree/ITree.java).

Each node from GumTree tree stores:
- typeLabel - according to `psiTree.node.elementType.toString()`
- label - according to the `PsiElement.label` function
- id - according to the numbering (PostOrder or PreOrder)

We store all nodes from PSI tree, which can we visit by DFS traversal from the root.

## GumTree labels

The rules for generating label by PSI node:
1. If the node is Leaf, we should store the text. For example `PyTargetExpression`, `PyPassStatement`, 
`PyReferenceExpression` and so on
2. Else we should consider several cases. We can not store text in other cases, because it contains text from the 
current vertex and from all children. For example:
```
for i in range(100):
    pass
```
In this case we should consider the type of the current node.

Since all code elements are inherited from the `PyBaseElementImpl<*>`, we need to consider special cases when we want 
to store not a name, because it is `null`, but something else, that characterizes this vertex.

Consider all used cases:

- `PyBinaryExpression` has name `null`, but we need to distinguish between operations depending on the action they 
perform. For example the nodes with expressions `a + b` and `a - b` have the same PSI structure, 
but the operators are different. In this case we store `operator` or `specialMethodName` for the operator if it is exists. 
For example, in the expression `a + b` we have `PyBinaryExpression` with the operator's `specialMethodName` `__add__`, 
but the operator is `Py:PLUS`.

- `PyPrefixExpression` has name `null`, but we also need to distinguish between operations depending on the action 
they perform. An example of this expression is `-1` or `not True`. It does not have the `specialMethodName` field, 
but it has the `operator` field.

- `PyAugAssignmentStatement` supports the operators like `+=`, `-=` and so on. We store `operation.text` which stores 
the action like `+=` or `-=` if it exists.

- `PyFormattedStringElement` supports [f-strings](https://docs.python.org/3/reference/lexical_analysis.html#f-strings).
We did not fins how we can separate text content from variables, and so we store full content like `f"text {1}"` 
We have `todo` about this.

- `PyImportElement` has not an empty name, but it stores the name of the library twice - in this node and in a child. 
So we use empty name for this node and store name only in the child node.

- `PyYieldExpression` can have the prefix `from` if you use a collection: `yield from [1, 2, 3]` and `yield 3`. 
But we can not get this information from the PSI node (only from the text). We need to distinguish these cases in the 
GumTree and so we store the label `from` if it is `yield` for a collection.

In the most of the cases we can use name of the node or empty label. 
For example, almost all elements that inherit from `PyBaseElementImpl<*>` have a defining (or empty) name. 
Consider the following examples:
  - `PyWithItem` or `PyArgumentList` have an empty name, because it has nested items in the tree
  - `PyReferenceExpression` has reference name, for example `with open('file_1', 'r')` has `PyReferenceExpression`
   with the name `open`. We should store it to have a difference between different called functions
We are not interested in the unconsidered vertices, since for them it is usually enough just to know the information 
about their type. In this case we are sure that it is not `PyBaseElementImpl<*>`. 
For example, it is true for the type `FILE`.

### Unsupported cases

**Note:** we don't support the following cases:
- async keyword
- type annotations

It means, that if you use these cases, the converter will work, but the tree can be the same for the following cases:
- `a: int = 1`
- `a: float = 1`
We are going to support these cases in the future.