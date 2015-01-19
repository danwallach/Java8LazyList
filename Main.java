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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Main {
    final static int MAX_INT = 1000000;
    final static int NUM_INSERT = 1000000;
    final static int NUM_RUNS = 200;
    final static int FIRST_N = 10000;
    final static int NUM_THREADS = 4;

    static ITree<Integer> tree;

    static HashMap<String,Long> sumTimes = new HashMap<>();
    static HashMap<String,Long> numRuns = new HashMap<>();


    public static void exercise(String name, Supplier<Integer> summer, boolean printStuff) {
        long startTime, endTime;
        int sum;

        if(printStuff) System.out.println("============================");
        FTree.resetVisitCounter();
        if(printStuff) System.out.println("Exercising " + name + ":");
        startTime = System.nanoTime();
        sum = summer.get();
        endTime = System.nanoTime();
        if(printStuff) System.out.println("Visit counter: " + FTree.getVisitCounter());
        if(printStuff) System.out.println("Running time: " + (endTime - startTime)/1000000.0 + "ms");
        if(printStuff) System.out.println("Sum: "+ sum);

        if(!sumTimes.containsKey(name)) {
            sumTimes.put(name, 0L);
            numRuns.put(name, 0L);
        }

        sumTimes.put(name, sumTimes.get(name) + endTime - startTime);
        numRuns.put(name, numRuns.get(name) + 1);
    }

    public static void exercise(String name, Stream<Integer> stream, boolean printStuff) {
        exercise(name, ()->stream.reduce(0, (x,y) -> (x+y)), printStuff);
    }

    static class Accumulator {
        private int acc;

        public Accumulator() {
            acc = 0;
        }

        public void add(int t) {
            acc = acc + t;
        }

        public int get() {
            return acc;
        }
    }

    public static void main(String[] args) {
        Random random = new Random();
        int i;

        System.out.println("Initializing tree...");

        tree = EmptyFTree.emptyTree();
        for(i=0; i<NUM_INSERT; i++) {
            tree = tree.insert(random.nextInt(MAX_INT));
        }

//        System.out.println(tree.toSexp());

        exercise("Eager (cold)", tree.eagerStream(), true);
        exercise("Stack (cold)", tree.stackStream(), true);
        exercise("Visitor (cold)", ()-> {
            Accumulator ac = new Accumulator();
            tree.inorder((x)->ac.add(x));
            return ac.get();
        }, true);

        for(i=0; i<NUM_RUNS; i++)  exercise("Visitor (hot)", ()-> {
                    Accumulator ac = new Accumulator();
                    tree.inorder((x)->ac.add(x));
                    return ac.get();
                }, false);

        LazyList<Integer> list = tree.lazyList();
        exercise("Lazy (cold)", list.stream(), true);
        for(i=0; i<NUM_RUNS; i++) exercise("Lazy (hot)", list.stream(), false);

        ForkJoinPool pool = new ForkJoinPool(NUM_THREADS);

        // exercise("Lazy/Parallel (cold)", list.parallelStream(), true);
        // for(i=0; i<NUM_RUNS; i++) exercise("Lazy/Parallel (hot)", list.parallelStream(), false);
        exercise("Lazy/Parallel (cold)", ()->{
            try {
                return pool.submit(() -> list.parallelStream().reduce(0, (x, y) -> (x + y))).get();
            } catch (Throwable throwable) {
                System.out.println("kaboom? " + throwable.toString());
                return 0;
            }
        }, true);
        for(i=0; i<NUM_RUNS; i++) exercise("Lazy/Parallel (hot)", ()->{
                try {
                    return pool.submit(() -> list.parallelStream().reduce(0, (x, y) -> (x + y))).get();
                } catch (Throwable throwable) {
                    System.out.println("kaboom? " + throwable.toString());
                    return 0;
                }
            }, false);

        System.out.println("============================");
        sumTimes.keySet().stream().sorted().forEach((name) -> System.out.println("Avg " + name + ": " + (sumTimes.get(name) / numRuns.get(name)) / 1000000.0 + "ms"));
    }
}
