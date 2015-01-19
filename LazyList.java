import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LazyList<T> {
    private T head;
    private Supplier<LazyList<T>> tail;
    private Supplier<LazyList<T>> tail2;

    /**
     * construct a lazy list with only one element
     * @param head
     */
    public LazyList(T head) {
        this(head, null, null);
    }

    /**
     * construct a lazy list with the specified head element and
     * a function to generate the rest, on demand
     * @param head
     * @param tail
     */
    public LazyList(T head, Supplier<LazyList<T>> tail) {
        this(head, tail, null);
    }

    private LazyList(T head, Supplier<LazyList<T>> tail, Supplier<LazyList<T>> tail2) {
        this.head = head;
        this.tail = tail;
        this.tail2 = tail2;
    }

    /**
     * returns a new lazy list that will return the concatenation of the other lazy list on to the end
     * of the current list. The current list, itself, is not modified.
     * @param other
     * @return
     */
    public LazyList<T> concat(Supplier<LazyList<T>> other) {
        if(empty()) {
            if (other != null)
                return other.get();
            else return this;
        }
        if(tail == null)
            return new LazyList<T>(head, other);
        if(tail2 == null)
            return new LazyList<T>(head, tail, other);

        return new LazyList<>(head, tail, ()->tail2.get().concat(other));
    }

    public boolean empty() {
        return head == null && tail == null && tail2 == null;
    }


    class ListIterator<T> implements Spliterator<T> {
        private LazyList<T> state;

        public ListIterator(LazyList<T> state) {
            this.state = state;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if(state == null || state.head == null)
                return false;

            T retval = state.head;

            if(state.tail != null) {
                if(state.tail2 != null) {
//                    System.out.println("concat: tail + tail2");
                    state = state.tail.get().concat(state.tail2);
                } else {
//                    System.out.println("tail only");
                    state = state.tail.get();
                }
            } else {
//                System.out.println("head:" + state.head + ", tail: " + state.tail + ", tail2: " + state.tail2);
                state = null;             // nothing else to do
            }

//            System.out.println("Accept: " + retval.toString());
            action.accept(retval);
            return true;
        }

        @Override
        public Spliterator<T> trySplit() {
            if(state.tail2 != null) {
//                System.out.println("Split!");
                Spliterator<T> retval = new ListIterator(state.tail2.get());
                state = new LazyList<T>(state.head, state.tail);

                return retval;
            }
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE; // too big, don't know, don't care
        }

        @Override
        public int characteristics() {
            return IMMUTABLE; // unclear how/if this is used, but at least it's true
        }
    }


    public Spliterator<T> spliterator() {
        return new ListIterator<T>(this);
    }

    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public Stream<T> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }
}
