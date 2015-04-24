package org.jprelude.core.util;

import com.codepoetics.protonpack.Indexed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jprelude.core.util.function.TriFunction;
import org.jprelude.core.util.tuple.Pair;
import org.jprelude.core.util.tuple.Triple;

@FunctionalInterface
public interface Seq<T> {
    Stream<T> stream();
        
        default Seq<T> sequential() {
        return Seq.from(() -> {
            final Stream<T> ret;
            final Stream<T> stream = this.stream();
            
            if (!stream.isParallel()) {
                ret = stream;
            } else {
                ret = stream.sequential();
            }
            
            return ret;
        });
    }
    
    default Seq<T> parallel() {
        return Seq.from(() -> {
            final Stream<T> ret;
            final Stream<T> stream = this.stream();
            
            if (stream.isParallel()) {
                ret = stream;
            } else {
                ret = stream.parallel();
            }
            
            return ret;
        });
    }

    default <R> Seq<R> map(final Function<? super T, ? extends R> f) {
        Objects.requireNonNull(f);

        return Seq.from(() -> Seq.this.stream().map(f));
    }
   
    default <R> Seq<R> map(final BiFunction<? super T, Long, R> f) {
        Objects.requireNonNull(f);
        
        final Seq<Long> nums  = Seq.iterate(0L, n -> n + 1);
        return this.zip(nums, (v, n) -> f.apply(v, n));
    }
    
    default <R> Seq<R> flatMap(final Function<? super T, ? extends Seq<? extends R>> f) {
        Objects.requireNonNull(f);
        
        return Seq.from(() -> Seq.this.stream().flatMap(v -> f.apply(v).stream()));
    }

    default <R> Seq<R> flatMap(final BiFunction<? super T, Long, ? extends Seq<? extends R>> f) {
        Objects.requireNonNull(f);
        
        final Seq<Long> ints  = Seq.iterate(0L, n -> n + 1);
        return this.zip(ints, (v, n) -> f.apply(v, n)).flatMap(v -> v);
    }

    
    default Seq<T> filter(final Predicate<? super T> pred) {
        Objects.requireNonNull(pred);

        return Seq.from(() -> Seq.this.stream().filter(pred));
    }
    
    default Seq<T> filter(final BiPredicate<? super T, Long> pred) {
        Objects.requireNonNull(pred);
        
        final Seq<Long> ints  = Seq.iterate(0L, n -> n + 1);   
        return this.flatMap((v, i) -> pred.test(v, i) ? Seq.of(v) : Seq.empty());
    }
    
    default Seq<T> reject(final Predicate<? super T> pred) {
        Objects.requireNonNull(pred);
        
        return Seq.from(() -> Seq.this.stream().filter(v -> !pred.test(v)));
    }
    
    default Seq<T> reject(final BiPredicate<? super T, Long> pred) {
        Objects.requireNonNull(pred);
        
        final Seq<Long> ints  = Seq.iterate(0L, n -> n + 1);        
        return this.flatMap((v, i) -> !pred.test(v, i) ? Seq.of(v) : Seq.empty());
    }
    
    default Seq<T> rejectNulls() {
        return this.reject(item -> item == null);
    }
    

    
    
    
    default <U, R> Seq<R> zip(final Seq<U> otherSeq, final BiFunction<? super T, ? super U, R> f) {
        Objects.requireNonNull(otherSeq);
        Objects.requireNonNull(f);
        
        return Seq.from(() ->
            com.codepoetics.protonpack.StreamUtils.zip(
                this.stream(), otherSeq.stream(), (x, y) -> f.apply(x, y)));

    }
    
    default <U, R> Seq<R> zip(final Seq<U> otherSeq, final TriFunction<? super T, ? super U, Long, R> f) { 
        Objects.requireNonNull(otherSeq);
        Objects.requireNonNull(f);
        
        return Seq.from(() -> {
            final Stream<Indexed<T>> indexedStream = com.codepoetics.protonpack.StreamUtils.zipWithIndex(this.stream());
            return com.codepoetics.protonpack.StreamUtils.zip(
                indexedStream,
                otherSeq.stream(),
                (indexedValue, otherValue) -> f.apply(indexedValue.getValue(), otherValue, (long) indexedValue.getIndex())
            );
        });
    }

    default Seq<T> take(final long n) {
        return Seq.from(() -> this.stream().limit(n));
    }
    
    default Seq<T> takeWhile(final Predicate<T> pred) {
        return Seq.from(() -> com.codepoetics.protonpack.StreamUtils.takeWhile(this.stream(), pred));
    }
    
    default Seq<T> takeUntil(final Predicate<T> pred) {
        return Seq.from(() -> com.codepoetics.protonpack.StreamUtils.takeUntil(this.stream(), pred));
    }
    
    

    default Seq<T> skip(final long n) {
        return Seq.from(() -> this.stream()).skip(n);
    }

    default Seq<T> skipWhile(final Predicate<T> pred) {
        Objects.requireNonNull(pred);
        
        return Seq.from(() -> com.codepoetics.protonpack.StreamUtils.skipWhile(this.stream(), pred));
    }
    
    default Seq<T> skipUntil(final Predicate<T> pred) {
        Objects.requireNonNull(pred);
        
        return Seq.from(() -> com.codepoetics.protonpack.StreamUtils.skipUntil(this.stream(), pred));
    }
    
    default Seq<T> distinct() {
        return (() -> this.stream().distinct());
    }
    
    default <R> Seq<T> distinct(final Function<T, R> f) {
        Objects.requireNonNull(f);
        
        return this.map(item -> {
                return new Supplier<T>() {
                    private final R mappedItem = f.apply(item);
                    
                    @Override
                    public T get() {
                        return item;
                    }

                    @Override
                    public boolean equals(final Object obj) {
                        final R other = f.apply(((Supplier<T>) obj).get());

                        return Objects.equals(this.mappedItem, other);
                    }

                    @Override
                    public int hashCode() {
                        return Objects.hashCode(this.mappedItem);
                    }
                };
            })
            .distinct()
            .map(supplier -> supplier.get());
    }
    
    default Seq<T> sorted() {
        return () -> this.stream().sorted();
    }
 
    default Seq<T> sorted(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
    
        return () -> this.stream().sorted(comparator);
    }
    
    default <R extends Comparable<?>> Seq<T> sortedAsc(
            final Function<T, R> f) {
        
        Objects.requireNonNull(f);
        
        return this.sorted((o1, o2) -> {
            final int c;

            if (o1 == null && o2 == null || o1 == o2) {
                c = 0;
            } else if (o1 == null) {
                c = -((Comparable) o2).compareTo(null);
            } else {
                c = ((Comparable) o1).compareTo(o2);
            }
            
            return c;
        });
    }
    
    default <R extends Comparable<?>> Seq<T> sortedDesc(
            final Function<T, R> f) {
        
        Objects.requireNonNull(f);
        
        return this.sorted((o1, o2) -> {
            final int c;

            if (o1 == null && o2 == null || o1 == o2) {
                c = 0;
            } else if (o1 == null) {
                c = -((Comparable) o2).compareTo(null);
            } else {
                c = ((Comparable) o1).compareTo(o2);
            }
            
            return -c;
        });
    }

    default Seq<T> peek(final Consumer<? super T> action) {
        Objects.requireNonNull(action);
        
        final Function<? super T, T> f = (v) -> {
            action.accept(v);
            return v;
        };
                
        return Seq.from((() -> this.stream().map(f)));
    }
    
    default Seq<T> peek(final BiConsumer<? super T, Long> action) {
        Objects.requireNonNull(action);
        
        return this.map((v, idx) -> {action.accept(v, idx); return v;});
    }
    
     default Seq<T> force() {
        // List::size has int as return value, but we want long.
        // So we count the length on our own.
        final List<T> list = new ArrayList();
        
        final long listSize = this.sequential()
                .peek(item -> list.add(item))
                .count();
        
        return new Seq<T>() {
            @Override
            public Stream<T> stream() {
                return list.stream();
            }
            
            @Override
            public long count() {
                return listSize;
            }
            
            @Override
            public Seq<T> force() {
                return this;
            }

            @Override
            public Seq<T> forceOnDemand() {
                return this;
            }
        };
    }

    default Seq<T> forceOnDemand() {
        return new Seq<T>() {
            private List<T> list;
            private long listSize;
            
            @Override
            public Stream<T> stream() {
                this.forceList();
                return this.list.stream();
            }
            
            @Override
            public long count() {
                this.forceList();
                return this.listSize;
            }
            
            @Override
            public Seq<T> forceOnDemand() {
                return this;
            }
            
            private void forceList() {
                if (this.list == null) {
                    this.list = new ArrayList(); 
                            
                    this.listSize = Seq.this.sequential()
                            .peek(item -> this.list.add(item))
                            .count();
                }
            }
        };
    }
    
    default Seq<T> prepend(final T value) {
        return Seq.concat(Seq.of(value), Seq.from(this));
    }
    
    default Seq<T> prependMany(final T... values) {
        return Seq.concat(this, Seq.of(values));
    }
    
    default Seq<T> append(final T value) {
        return Seq.concat(Seq.from(this), Seq.of(value));
    }
    
    default Seq<T> appendMany(final T... values) {
        return Seq.concat(Seq.of(values), this);
    }
    
    default T head() {
        final Object[] arr = this.take(1).toArray();
        
        if (arr.length == 0) {
            throw new NoSuchElementException();
        }
        
        return (T) arr[0];
    }
    
    default T headOrNull() {
        return this.headOrDefault(null);
    }
    
    default T headOrDefault(final T defaultValue) {
        final Object[] arr = this.take(1).toArray();
        
        return (arr.length == 0 ? defaultValue : (T) arr[0]); 
    }
    
    default Seq<T> tail() {
        return this.skip(1);
    }
    
    default T reduce(final T identity, final BiFunction<T, T, T> accumulator) {
        Objects.requireNonNull(accumulator);
        final BinaryOperator<T> operator = (v1, v2) -> accumulator.apply(v1, v2);
        return this.stream().reduce(identity, operator);
    }
    
    default Optional<T> reduce(final BiFunction<T, T, T> accumulator) {
        Objects.requireNonNull(accumulator);
        
        final BinaryOperator<T> operator = (v1, v2) -> accumulator.apply(v1, v2);
        return this.stream().reduce(operator);
    }

    default <U> U reduce(
            final U identity,
            final BiFunction<U, ? super T, U> accumulator,
            final BinaryOperator<U> combiner) {
        
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        
        final BinaryOperator<U> operator = (v1, v2) -> combiner.apply(v1, v2);
        return this.stream().reduce(identity, accumulator, operator);
    }

    default <R> R collect(
            final Supplier<R> supplier,
            final BiConsumer<R, ? super T> accumulator,
            final BiConsumer<R, R> combiner) {
        
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);

        return this.stream().collect(supplier, accumulator, combiner);
    }
    
    default <A, R> R collect(final Collector<? super T, A, R> collector) {
        Objects.requireNonNull(collector);

        return this.stream().collect(collector);
    }


    default Optional<T> min(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        
        return this.stream().min(comparator);
    }

    default Optional<T> max(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        
        return this.stream().max(comparator);
    }

    default long count() {
        return this.stream().count();
    }


    default boolean anyMatch(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        
        return this.stream().anyMatch(predicate);
    }

    default boolean allMatch(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        
        return this.stream().allMatch(predicate);
        
    }

    default boolean noneMatch(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        
        return this.stream().noneMatch(predicate);
    }

    default Optional<T> findFirst() {
        return this.stream().findFirst();
    }

    default Optional<T> findAny() {
        return this.stream().findAny();
    }
    
    default void forEach(final Consumer<? super T> action) {
        Objects.requireNonNull(action);
        
        this.stream().forEach(action);
    }    
    
    default void forEach(final BiConsumer<? super T, Long> action) {
        Objects.requireNonNull(action);
        
        final Seq<Long> nums  = Seq.iterate(0L, n -> n + 1);
 
        this.zip(nums, (v, n) -> new Object[] {v, n})
            .forEach(pair -> action.accept((T) pair[0], (long) pair[1]));
    }

    default void forEachOrdered(final Consumer<? super T> action) {
        Objects.requireNonNull(action);
        
        this.stream().forEachOrdered(action);
    }
    
    default Object[] toArray() {
        return this.stream().toArray();
    }
    
    default <T> T[] toArray(final IntFunction<T[]> generator) {
        Objects.requireNonNull(generator);
        
        return this.stream().toArray(generator);
    }
    
    default String[] toStringArray() {
        return (String[]) this.stream()
                .map(v -> Objects.toString(v, null))
                .toArray();
    }
    
    default List<T> toList() {
        return this.stream().collect(Collectors.toList());
    }
    
    default List<String> toStringList() {
        return (List<String>)this.stream()
                .map(v -> v == null ? null : v.toString())
                .collect(Collectors.toList());
    }
    

    
    
    
    
    
    // Static factories

    static <T> Seq<T> paralell(final Seq<T> seq) {
        return Seq.from(seq).parallel();
    }
    
    static <T> Seq<T> sequential(final Seq<T> seq) {
        return Seq.from(seq).sequential();
    }
    
    
    static <T> Seq<T> empty() {
        return Seq.from(() -> Stream.empty());
    }
    
    static<T> Seq<T> of(final T t) {
        return Seq.from(() -> Stream.of(t));
    }

    @SafeVarargs
    @SuppressWarnings("varargs") // Creating a sequence from an array is safe
    static<T> Seq<T> of(final T... values) {
        return Seq.from(() -> Stream.of(values));
    }

    
        static <T> Seq<T> from(final Seq<T> seq) {
        return seq == null
                ? Seq.empty()
                : seq;
    }
    
    static <T> Seq<T> from(final Iterable<T> iterable) {
        final Seq<T> ret;
        
        if (iterable == null) {
            ret = Seq.empty();
        } else {
            ret = Seq.from(() -> StreamSupport.stream(iterable.spliterator(), false));
        }
        
        return ret;
    }
    
    static <T> Seq<T> from(final T[] values) {
        return Seq.from(() -> Arrays.stream(values));
    }

    static Seq<Integer> from(final int[] values) {
        return Seq.from(() -> IntStream.of(values).boxed());
    }

    static Seq<Long> from(final long[] values) {
        return Seq.from(() -> LongStream.of(values).boxed());
    }

    static Seq<Double> from(final double[] values) {
        return Seq.from(() -> DoubleStream.of(values).boxed());
    }
    
    static <T> Seq<T> from(final BiFunction<Long, Long, Collection<T>> blockReader, final long blockSize) {
        return Seq.from(blockReader, blockSize, 0);
    }
       
    
    static <T> Seq<T> from(final BiFunction<Long, Long, Collection<T>> blockReader, final long blockSize, long start) {
        if (blockSize <= 0) {
            throw new IllegalArgumentException("Second argument must be a positive integer number");
        }
        
        return Seq.iterate(0L, n -> n + blockSize)
                .map(n -> blockReader.apply(n, blockSize))
                .flatMap(coll -> (coll  == null ? Seq.of((Collection<T>) null) : (coll.size() == blockSize ? Seq.of(coll) : Seq.of(coll, null))))                 
                .takeWhile(coll -> coll != null)
                .flatMap(coll -> Seq.from(coll));
    }
    
    static <T> Seq<T> iterate(final T seed, final Function<T, T> f) {
        Objects.requireNonNull(f);
 
        return Seq.from(() -> Stream.iterate(seed, v -> f.apply(v)));
    }
    
    static <T> Seq<T> iterate(final T seed1, final T seed2, final BiFunction<T, T, T> f) {
        Objects.requireNonNull(f);

        final Pair<T, T> seedPair = Pair.of(seed1, seed2);
        
        return Seq.iterate(seedPair, pair -> Pair.of(pair.getSecond(), f.apply(pair.getFirst(), pair.getSecond())))
                .map(pair -> pair.getFirst());
    }
    
    static <T> Seq<T> iterate(final T seed1, final T seed2, final T seed3, final TriFunction<T, T, T, T> f) {
        Objects.requireNonNull(f);
        
        final Triple<T, T, T> seedTriple = Triple.of(seed1, seed2, seed3);
      
        return Seq.iterate(seedTriple, triple -> Triple.of(triple.getFirst(), triple.getSecond(), f.apply(triple.getFirst(), triple.getSecond(), triple.getThird())))
                .map(Triple::getFirst);
    }


    static<T> Seq<T> generate(final Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        
        return Seq.from(() -> Stream.generate(supplier));
    }

    static <T> Seq<T> concat(
            final Seq<? extends T> seq1,
            final Seq<? extends T> seq2) {

        Objects.requireNonNull(seq1);
        Objects.requireNonNull(seq2);
        
        final Seq<T> ret;
        final Stream<? extends T> stream1 = seq1.stream();
        final Stream<? extends T> stream2;
        
        try {
            stream2 = seq2.stream();
        } catch (final RuntimeException e1) {
            try {
                stream1.close();
            } catch (final RuntimeException e2) {
                e1.addSuppressed(e2);
            }
            
            throw e1;
        }
        
        return Seq.from(() -> Stream.concat(stream1, stream2));
    }
           
    static <T> Seq<T> concat(final Seq<Seq<T>> seqs) {
        Objects.requireNonNull(seqs);
        
        final Seq<T> ret;
        
        ret = seqs.flatMap(seq ->
            seq != null ? seq : Seq.empty()
        );
        
        return ret;
    }

    static <T> Seq<T> concat(final Iterable<Seq<T>> seqs) {
        Objects.requireNonNull(seqs);
        
        return Seq.concat(Seq.from(seqs));
    }

    static <T> Seq<T> concat(final Seq<T>... seqs) {
        return Seq.concat(Seq.from(seqs));
    }
    
    static Seq<Integer> range(final int start, final int end) {
        return Seq.from(() -> IntStream.range(start, end).boxed());
    }
    
    static Seq<Long> range(final long start, final long end) {
        return Seq.from(() -> LongStream.range(start, end).boxed());
    }
}
