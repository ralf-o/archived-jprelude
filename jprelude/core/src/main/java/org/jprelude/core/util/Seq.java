package org.jprelude.core.util;

import com.codepoetics.protonpack.Indexed;
import java.util.Arrays;
import java.util.Collection;
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
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jprelude.core.function.TriFunction;
import org.jprelude.core.tuple.Pair;
import org.jprelude.core.tuple.Triple;


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
    
    default <U, R> Seq<R> mapFiltered(final Function<T, Optional<R>> f) {
        return this.map(f).filter(o -> o.isPresent()).map(o -> o.get());
    }

    default <U, R> Seq<R> mapFiltered(final BiFunction<T, Long, Optional<R>> f) {
        return this.map((v, i) -> f.apply(v, i)).filter(o -> o.isPresent()).map(o -> o.get());
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
        return this.mapFiltered((v, i) -> pred.test(v, i) ? Optional.of(v) : Optional.empty());
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
    
    default Object[] toArray() {
        return this.stream().toArray();
    }
    
    default List<T> toList() {
        return this.stream().collect(Collectors.toList());
    }
    
    default void forEach(final Consumer<? super T> consumer) {
        try (final Stream<T> stream = this.stream()) {
            stream.forEach(consumer);
        }
    }
    
    default void forEach(final Observer<? super T>... observers) {
        if (observers != null)  {
            this.forEach(Arrays.asList(observers));
        }
    }
        
    default void forEach(final List<Observer<? super T>> observers) {
        final List<Observer<? super T>> observerList = Seq.from(observers).filter(observer -> observer != null).toList();
    
        if (!observerList.isEmpty()) {
            try {
                this.forEach(item -> observerList.forEach(observer -> observer.onNext(item)));
                observerList.forEach(observer -> observer.onComplete()); // TODO
            } catch (final Throwable throwable) {
                observerList.forEach(observer -> observer.onError(throwable));
            }
        }
    }
    
    
    default void forEach(final BiConsumer<? super T, Long> action) {
        final Seq<Long> nums  = Seq.iterate(0L, n -> n + 1);
 
        this.zip(nums, (v, n) -> new Object[] {v, n})
            .forEach(pair -> action.accept((T) pair[0], (long) pair[1]));
    }
    
    public static <T> Seq<T> from(final Seq<T> seq) {
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
