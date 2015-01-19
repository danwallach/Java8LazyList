Simple Java8 example code that demonstrates the use of a lazy list to interate
over a functional tree. 

FTree.java -- a simple, parametric functional tree
  -- In here, you'll also see example code that builds an "eager" stream
     as well as using a Stack to maintain state while iterating over a
     tree. These are included for performance comparison purposes, and
     aren't something you should ever seriously use. On the other hand,
     the visitor pattern (i.e., the inorder() method) runs faster than
     any stream variant.

EmptyFTree.java -- an "empty" leaf node that we use instead of null
ITree.java -- the interface that a tree node has to implement
LazyList.java -- functional lazy lists, including support for parallel streams
Main.java -- test code that can drive all of the above and show you performance comparisons
