import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ITree<T extends Comparable<T>> {
    ITree<T> insert(T data);
    boolean contains(T data);

    /**
     * visitor pattern: consume the elements of the tree in sorted order
     * @param consumer
     */
    void inorder(Consumer<T> consumer);

    Stream<T> eagerStream();

    default Stream<T> lazyStream() {
        return lazyList().stream();
    }

    default Stream<T> lazyParallelStream() {
        return lazyList().parallelStream();
    }

    Stream<T> stackStream();

    LazyList<T> lazyList();

    public boolean empty();

    public ITree<T> getLeft();

    public ITree<T> getRight();

    public T getData();

    String toSexp();
}