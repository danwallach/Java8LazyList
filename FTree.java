import java.util.Spliterator;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


class FTree<T extends Comparable<T>> implements ITree<T> {
    private T data;
    private ITree<T> left, right;

    static int visitCounter = 0;

    /**
     * constructor for internal use only; use the singleton empty node (EmptyFTree) to start building a tree
     */
    FTree(T data, ITree<T> left, ITree<T> right) {
        this.data = data;
        this.left = left;
        this.right= right;
    }

    public boolean empty() {
        return false;
    }

    public String toSexp() {
        return "(" + left.toSexp() + " | " + data.toString() + " | " + right.toSexp() + ")";
    }

    public ITree<T> getLeft() { return left; }
    public ITree<T> getRight() { return right; }
    public T getData() { return data; }

    public boolean contains(T data) {
        int comparison =  data.compareTo(this.data);
        if(comparison < 0)
            return left.contains(data);
        else if (comparison > 0)
            return right.contains(data);
        else
            return true;
    }

    public FTree<T> insert(T newbie) {
        if(newbie.compareTo(data) < 0)
            return new FTree<T>(data, left.insert(newbie), right);
        else
            return new FTree<T>(data, left, right.insert(newbie));
    }

    public void inorder(Consumer<T> consumer) {
        visitCounter++;
        left.inorder(consumer);
        consumer.accept(data);
        right.inorder(consumer);
    }

    /**
     * This method returns a stream from the tree, but it will traverse the whole tree first, effectively making
     * a complete copy, no matter how few elements you ultimately want to read from it.
     */
    public Stream<T> eagerStream() {
        visitCounter++;
        return Stream.concat(left.eagerStream(),
                             Stream.concat(Stream.of(data),
                                           right.eagerStream() ));
    }

    /**
     * This method returns a lazy list derived from the tree that will only read elements on demand, making it
     * much more efficient.
     */
    public LazyList<T> lazyList() {
        visitCounter++;
        return left.lazyList()
                   .concat(() -> new LazyList<T>(data, () -> right.lazyList()));
    }

    /**
     * This method uses an internal Stack to have the same effect as the lazyList
     */
    public Stream<T> stackStream() {
        return StreamSupport.stream(new StackHelper<T>(this), false);
    }


    class SeenTree<T extends Comparable<T>> {
        private boolean leftSeen = false;
        private ITree<T> tree;

        public SeenTree(ITree<T> tree) {
            this.tree = tree;
        }

        public boolean getLeftSeen() { return leftSeen; }
        public void setLeftSeen() { leftSeen = true; }

        public ITree<T> getTree() { return tree; }
    }

    class StackHelper<T extends Comparable<T>> implements Spliterator<T> {
        private Stack<SeenTree<T>> stack = new Stack<>();
        public StackHelper(ITree<T> tree) {
            if(!tree.empty())
                stack.push(new SeenTree(tree));
        }

        public boolean tryAdvance(Consumer<? super T> action) {
            SeenTree<T> stree;
            do {
                visitCounter++; // lexical scoping FTW
                stree = stack.pop();
                ITree<T> tree = stree.getTree();

                if(tree.empty()) continue; // we hit an empty leaf; move on.

                if(stree.getLeftSeen()) {
                    action.accept(tree.getData());
                    stack.push(new SeenTree(tree.getRight()));
                    return true;
                } else {
                    // left hasn't been seen yet, so push the current
                    // tree back on, mark it seen, and then push the left
                    // subtree
                    stree.setLeftSeen();
                    stack.push(stree);
                    stack.push(new SeenTree<T>(tree.getLeft()));
                }
            } while(!stack.empty());

            // if we get here, that means the loop finished without finding anything
            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return 0;
        }

        @Override
        public int characteristics() {
            return IMMUTABLE;
        }
    }

    public static int getVisitCounter() {
        return visitCounter;
    }

    public static void resetVisitCounter() {
        visitCounter = 0;
    }
}