/*
 * Copyright (C) 2014 Dan Wallach <dwallach@rice.edu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
