package org.jprelude.core.util;

import com.codepoetics.protonpack.Indexed;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
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
    
    default <R> Seq<R> map(final Function<? super T, ? extends R> f) {
        return Seq.from(() -> Seq.this.stream().map(f));
    }
   
    default <R> Seq<R> map(final BiFunction<? super T, Long, R> f) {
        final Seq<Long> nums  = Seq.iterate(0L, n -> n + 1);
        return this.zip(nums, (v, n) -> f.apply(v, n));
    }
    
    default <U, R> Seq<R> zip(final Seq<U> otherSeq, final BiFunction<? super T, ? super U, R> f) {
        return Seq.from(() ->
            com.codepoetics.protonpack.StreamUtils.zip(
                this.stream(), otherSeq.stream(), (x, y) -> f.apply(x, y)));

    }
    
    default <U, R> Seq<R> zip(final Seq<U> otherSeq, final TriFunction<? super T, ? super U, Long, R> f) { 
        return Seq.from(() -> {
            final Stream<Indexed<T>> indexedStream = com.codepoetics.protonpack.StreamUtils.zipWithIndex(this.stream());
            return com.codepoetics.protonpack.StreamUtils.zip(
                indexedStream,
                otherSeq.stream(),
                (indexedValue, otherValue) -> f.apply(indexedValue.getValue(), otherValue, (long) indexedValue.getIndex())
            );
        });
    }

    default <R> Seq<R> flatMap(final Function<? super T, ? extends Seq<? extends R>> f) {
        return Seq.from(() -> Seq.this.stream().flatMap(v -> f.apply(v).stream()));
    }
   
    default <T> Seq<T> flatten(final Seq<Seq<T>> seqs) {
        return Seq.from(seqs).flatMap(Function.identity());
    }
    
    default <R> Seq<R> flatMap(final BiFunction<? super T, Long, ? extends Seq<? extends R>> f) {
        final Seq<Long> ints  = Seq.iterate(0L, n -> n + 1);
        return this.zip(ints, (v, n) -> f.apply(v, n)).flatMap(v -> v);
    }
    
    default Seq<T> filter(final Predicate<? super T> pred) {
        return Seq.from(() -> Seq.this.stream().filter(pred));
    }
    
    default Seq<T> filter(final BiPredicate<? super T, Long> pred) {
        final Seq<Long> ints  = Seq.iterate(0L, n -> n + 1);   
        return this.flatMap((v, i) -> pred.test(v, i) ? Seq.of(v) : Seq.empty());
    }
    
    default Seq<T> reject(final Predicate<? super T> pred) {
        return Seq.from(() -> Seq.this.stream().filter(v -> !pred.test(v)));
    }
    
    default Seq<T> reject(final BiPredicate<? super T, Long> pred) {
        final Seq<Long> ints  = Seq.iterate(0L, n -> n + 1);        
        return this.flatMap((v, i) -> !pred.test(v, i) ? Seq.of(v) : Seq.empty());
    }
    
    default Seq<T> rejectNulls() {
        return this.reject(item -> item == null);
    }
    
    default Seq<T> peek(final Consumer<? super T> action) {
        final Function<? super T, T> f = (v) -> {
            action.accept(v);
            return v;
        };
                
        return Seq.from((() -> this.stream().map(f)));
    }
    
    default Seq<T> peek(final BiConsumer<? super T, Long> consumer) {
        return this.map((v, idx) -> {consumer.accept(v, idx); return v;});
    }
    
    default Seq<T> take(final int n) {
        return Seq.from(() -> this.stream().limit(n));
    }
    
    default Seq<T> takeWhile(final Predicate<T> pred) {
        return Seq.from(() -> com.codepoetics.protonpack.StreamUtils.takeWhile(this.stream(), pred));
    }
    
    default Seq<T> takeUntil(final Predicate<T> pred) {
        return Seq.from(() -> com.codepoetics.protonpack.StreamUtils.takeUntil(this.stream(), pred));
    }

    default Seq<T> skip(final int n) {
        return Seq.from(() -> this.stream()).skip(n);
    }

    default Seq<T> skipWhile(final Predicate<T> pred) {
        return Seq.from(() -> com.codepoetics.protonpack.StreamUtils.skipWhile(this.stream(), pred));
    }
    
    default Seq<T> skipUntil(final Predicate<T> pred) {
        return Seq.from(() -> com.codepoetics.protonpack.StreamUtils.skipUntil(this.stream(), pred));
    }
    
    default Seq<T> sorted() {
        return () -> this.stream().sorted();
    }
 
    default Seq<T> sorted(final Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return () -> this.stream().sorted(comparator);
    }
    
    default <R extends Comparable<?>> Seq<T> sorted(
            final Function<T, R> f,
            final SortDirection sortDirection) {
        
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
            
            return (sortDirection == SortDirection.ASCENDING  ? c : -c);
        });
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
    
    default <A, R> R collect(final Collector<? super T, A, R> collector) {
        try (final Stream<T> stream = this.stream()) {
            return stream.collect(collector);
        }
    }
    
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
    
    static <T> Seq<T> concat(final Seq<Seq<T>> seqs) {
        final Seq<T> ret;
        
        if (seqs == null) {
            ret = Seq.empty();
        } else {
            ret = seqs.flatMap(seq ->
                seq != null ? seq : Seq.empty()
            );
        }
        
        return ret;
    }

    static <T> Seq<T> concat(final Seq<T>... seqs) {
        return Seq.concat(Seq.from(seqs));
    }
    
    static <T> Seq<T> concat(final Iterable<Seq<T>> seqs) {
        return Seq.concat(Seq.from(seqs));
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
    
    default T reduce(final T start, final BiFunction<T, T, T> f) {
        final BinaryOperator<T> operator = (v1, v2) -> f.apply(v1, v2);
        return this.stream().reduce(start, operator);
    }
    
    default long length() {
        return this.stream().collect(Collectors.counting());
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
    
    default Seq<T> force() {
        // List::size has int as return value, but we want long.
        // So we count the length on our own.
        final Mutable<Long> length = Mutable.of(0L);
        
        final List<T> list = this
                .sequential()
                .peek((item, idx) -> length.set(idx + 1))
                .toList();
        
        return new Seq<T>() {
            @Override
            public Stream<T> stream() {
                return list.stream();
            }
            
            @Override
            public long length() {
                return length.get();
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
            public long length() {
                this.forceList();
                return this.listSize;
            }
            
            @Override
            public Seq<T> forceOnDemand() {
                return this;
            }
            
            private void forceList() {
                final Mutable<Long> counter = Mutable.of(0L);
                
                if (this.list == null) {
                    this.list = Seq.this
                            .peek((item, idx) -> counter.set(idx))
                            .toList();
                    
                    this.listSize = counter.get() + 1;
                }
            }
        };
    }

    default <T> Object[] toArray() {
        return this.stream().toArray();
    }
    
    default <T> T[] toArray(IntFunction<T[]> generator) {
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
    
    default void forEach(final Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer);
        
        try (final Stream<T> stream = this.stream()) {
            stream.forEach(consumer);
        }
    }    
    
    default void forEach(final BiConsumer<? super T, Long> action) {
        final Seq<Long> nums  = Seq.iterate(0L, n -> n + 1);
 
        this.zip(nums, (v, n) -> new Object[] {v, n})
            .forEach(pair -> action.accept((T) pair[0], (long) pair[1]));
    }
    
    static <T> Seq<T> from(final Seq<T> seq) {
        return seq == null
                ? Seq.empty()
                : seq;
    }
    
    public static <T> Seq<T> from(final Iterable<T> iterable) {
        final Seq<T> ret;
        
        if (iterable == null) {
            ret = Seq.empty();
        } else {
            ret = Seq.from(() -> StreamSupport.stream(iterable.spliterator(), false));
        }
        
        return ret;
    }
    
    public static <T> Seq<T> from(final T[] values) {
        return Seq.from(() -> Arrays.stream(values));
    }

    public static Seq<Integer> from(final int[] values) {
        return Seq.from(() -> IntStream.of(values).boxed());
    }

    public static Seq<Long> from(final long[] values) {
        return Seq.from(() -> LongStream.of(values).boxed());
    }

    public static Seq<Double> from(final double[] values) {
        return Seq.from(() -> DoubleStream.of(values).boxed());
    }
    
    public static <T> Seq<T> from(final BiFunction<Long, Long, Collection<T>> blockReader, final long blockSize) {
        return Seq.from(blockReader, blockSize, 0);
    }
       
    
    public static <T> Seq<T> from(final BiFunction<Long, Long, Collection<T>> blockReader, final long blockSize, long start) {
        if (blockSize <= 0) {
            throw new IllegalArgumentException("Second argument must be a positive integer number");
        }
        
        return Seq.iterate(0L, n -> n + blockSize)
                .map(n -> blockReader.apply(n, blockSize))
                .flatMap(coll -> (coll  == null ? Seq.of((Collection<T>) null) : (coll.size() == blockSize ? Seq.of(coll) : Seq.of(coll, null))))                 
                .takeWhile(coll -> coll != null)
                .flatMap(coll -> Seq.from(coll));
    }
    
    public static <T> Seq<T> of(final T value) {
        return Seq.from(Arrays.asList(value));
    }
    
    // TODO: rename to ofItems?
    public static <T> Seq<T> of(final T... values) {
        return Seq.from(values);
    }

    public static <T> Seq<T> paralell(final Seq<T> seq) {
        return Seq.from(seq).parallel();
    }
    
    public static <T> Seq<T> sequential(final Seq<T> seq) {
        return Seq.from(seq).sequential();
    }
    
    public static <T> Seq<T> empty() {
        return Seq.from(() -> Stream.empty());
    }
    
    public static <T> Seq<T> iterate(final T seed, final Function<T, T> f) {
        Objects.requireNonNull(f);
        return Seq.from(() -> Stream.iterate(seed, v -> f.apply(v)));
    }
    
    public static <T> Seq<T> iterate(final T seed1, final T seed2, final BiFunction<T, T, T> f) {
        Objects.requireNonNull(f);
        final Pair<T, T> seedPair = Pair.of(seed1, seed2);
        
        return Seq.iterate(seedPair, pair -> Pair.of(pair.getSecond(), f.apply(pair.getFirst(), pair.getSecond())))
                .map(pair -> pair.getFirst());
    }
    
    public static <T> Seq<T> iterate(final T seed1, final T seed2, final T seed3, final TriFunction<T, T, T, T> f) {
        final Triple<T, T, T> seedTriple = Triple.of(seed1, seed2, seed3);
      
        return Seq.iterate(seedTriple, triple -> Triple.of(triple.getFirst(), triple.getSecond(), f.apply(triple.getFirst(), triple.getSecond(), triple.getThird())))
                .map(Triple::getFirst);
    }

    public static Seq<Integer> range(final int start, final int end) {
        return Seq.from(() -> IntStream.range(start, end).boxed());
    }
    
    public static Seq<Long> range(final long start, final long end) {
        return Seq.from(() -> LongStream.range(start, end).boxed());
    }
}
