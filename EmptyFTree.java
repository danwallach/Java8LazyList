import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// note the kludgy warning suppression because Java's type system isn't clever enough
public class EmptyFTree<T extends Comparable<T>> implements ITree<T> {
    @SuppressWarnings("unchecked")
    private static final ITree EMPTY_TREE = new EmptyFTree<>();

    @SuppressWarnings("unchecked")
    public static final <T extends Comparable<T>> ITree<T> emptyTree() {
        return (ITree<T>) EMPTY_TREE;
    }

    public ITree<T> insert(T newbie) {
        return new FTree<T>(newbie, emptyTree(), emptyTree());
    }

    public boolean contains(T data) {
        return false;
    }

    public void inorder(Consumer<T> consumer) {}

    @Override
    public Stream<T> eagerStream() {
        return StreamSupport.stream(emptySpliterator, false);
    }

    @Override
    public Stream<T> stackStream() {
        return StreamSupport.stream(emptySpliterator, false);
    }

    public EmptyFTree() { }

    @SuppressWarnings("unchecked")
    private static Spliterator emptySpliterator = new Spliterator() {
        @Override
        @SuppressWarnings("unchecked")
        public boolean tryAdvance(Consumer action) {
            return false;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Spliterator trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return 0;
        }

        @Override
        public int characteristics() {
            return 0;
        }
    };

    public LazyList<T> lazyList() {
        return new LazyList<>(null, null);
    }

    public boolean empty() {
        return true;
    }

    public ITree<T> getLeft() { return emptyTree(); }

    public ITree<T> getRight() { return emptyTree(); }

    public T getData() { throw new RuntimeException("getData() on emptyTree"); }

    public String toSexp() { return ""; }
}