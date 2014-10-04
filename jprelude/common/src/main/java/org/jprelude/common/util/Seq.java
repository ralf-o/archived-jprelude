package org.jprelude.common.util;

import com.codepoetics.protonpack.Indexed;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jprelude.common.function.Consumer1;
import org.jprelude.common.function.Consumer2;
import org.jprelude.common.function.Function1;
import org.jprelude.common.function.Function2;
import org.jprelude.common.function.Function3;
import org.jprelude.common.function.Predicate1;
import org.jprelude.common.function.Predicate2;

public interface Seq<T> {
    Stream<T> stream();
    
    default <R> Seq<R> map(final Function1<? super T, ? extends R> f) {
        return Seq.buildBy(() -> StreamUtils.stream(Seq.this.stream()).map(f));
    }
   
    default <R> Seq<R> map(final Function2<? super T, Integer, R> f) {
        final Seq<Integer> nums  = Seq.iterate(0, n -> n + 1);
        return this.zip(nums, (v, n) -> f.apply(v, n));
    }
    
    
    default <U, R> Seq<R> zip(final Seq<U> otherSeq, final Function2<? super T, ? super U, R> f) {
        return Seq.buildBy(() ->
            com.codepoetics.protonpack.StreamUtils.zip(
                this.stream(), otherSeq.stream(), (x, y) -> f.apply(x, y)));
    }
    
    default <U, R> Seq<R> zip(final Seq<U> otherSeq, final Function3<? super T, ? super U, Integer, R> f) { 
        return Seq.buildBy(() -> {
            final Stream<Indexed<T>> indexedStream = com.codepoetics.protonpack.StreamUtils.zipWithIndex(this.stream());
            return com.codepoetics.protonpack.StreamUtils.zip(
                indexedStream,
                otherSeq.stream(),
                (indexedValue, otherValue) -> f.apply(indexedValue.getValue(), otherValue, (int) indexedValue.getIndex())
            );
        });
    }
    
    default <U, R> Seq<R> mapFiltered(final Function1<T, Optional<R>> f) {
        return this.map(f).filter(o -> o.isPresent()).map(o -> o.get());
    }

    default <U, R> Seq<R> mapFiltered(final Function2<T, Integer, Optional<R>> f) {
        return this.map((v, i) -> f.apply(v, i)).filter(o -> o.isPresent()).map(o -> o.get());
    }
 
    default <R> Seq<R> flatMap(final Function1<? super T, ? extends Seq<? extends R>> f) {
        return Seq.buildBy(() ->
            StreamUtils.stream(Seq.this.stream())
            .flatMap(v -> f.apply(v).stream()));
    }
    
    default <R> Seq<R> flatMap(final Function2<? super T, Integer, ? extends Seq<? extends R>> f) {
        final Seq<Integer> ints  = Seq.iterate(0, n -> n + 1);
        return this.zip(ints, (v, n) -> f.apply(v, n)).flatMap(v -> v);
    }
    
    default Seq<T> filter(final Predicate1<? super T> pred) {
        return Seq.buildBy(() -> StreamUtils.stream(Seq.this.stream()).filter(pred));
    }
    
    default Seq<T> filter(final Predicate2<? super T, Integer> pred) {
        final Seq<Integer> ints  = Seq.iterate(0, n -> n + 1);        
        return this.mapFiltered((v, i) -> pred.test(v, i) ? Optional.of(v) : Optional.empty());
    }
    
    default Seq<T> peek(final Consumer1<? super T> action) {
        final Function1<? super T, T> f = (v) -> {
            action.accept(v);
            return v;
        };
                
        return Seq.buildBy((() -> StreamUtils.stream(this.stream()).map(f)));
    }
    
    
    default Seq<T> peek(final Consumer2<? super T, Integer> consumer) {
        return this.map((v, idx) -> {consumer.accept(v, idx); return v;});
    }
    
    default Seq<T> take(final int n) {
        return Seq.buildBy(() -> StreamUtils.stream(this.stream()).limit(n));
    }
    
    default Seq<T> skip(final int n) {
        return Seq.buildBy(() -> StreamUtils.stream(this.stream()).skip(n));
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
    
    default T reduce(final T start, final Function2<T, T, T> f) {
        final BinaryOperator<T> operator = (v1, v2) -> f.apply(v1, v2);
        return StreamUtils.stream(this.stream()).reduce(start, operator);
    }
    
    default T reduce(final T start, final Function3<T, T, Integer, T> f) {
        final int[] idx = { 0 };
        final BinaryOperator<T> operator = (v1, v2) -> f.apply(v1, v2, idx[0]++);
        return StreamUtils.stream(this.stream()).reduce(start, operator);
    }
    
    default Object[] toArray() {
        return StreamUtils.stream(this.stream()).toArray();
    }
    
    default List<T> toList() {
        return StreamUtils.stream(this.stream()).collect(Collectors.toList());
    }
    
    default void forEach(final Consumer1<? super T> action) {
        try (final Stream<T> stream = StreamUtils.stream(this.stream())) {
            stream.forEach(action);
        }
    }
    
    default void forEach(final Consumer2<? super T, Integer> action) {
        final int[] idx = { 0 };

        this.forEach(v -> action.accept(v, idx[0]++));
    }
    
    public static <T> Seq<T> from(final Iterable<T> iterable) {
        final Seq<T> ret;
        
        if (iterable == null) {
            ret = Seq.empty();
        } else {
            ret = Seq.buildBy(() -> StreamSupport.stream(iterable.spliterator(), false));
        }
        
        return ret;
    }
    
    public static <T> Seq<T> buildBy(final Supplier<Stream<T>> streamSupplier) {
        if (streamSupplier == null) {
            throw new IllegalArgumentException("Argument 'streamSupplier' must not be null");
        }

        return () -> StreamUtils.stream(streamSupplier.get());
    }

    public static <T> Seq<T> from(final T[] values) {
        return Seq.buildBy(() -> Arrays.stream(values));
    }

    public static Seq<Integer> from(final int[] values) {
        return Seq.buildBy(() -> IntStream.of(values).boxed());
    }

    public static Seq<Long> from(final long[] values) {
        return Seq.buildBy(() -> LongStream.of(values).boxed());
    }

    public static Seq<Double> from(final double[] values) {
        return Seq.buildBy(() -> DoubleStream.of(values).boxed());
    }

    public static <T> Seq<T> of(final T... values) {
        return Seq.from(values);
    }

    public static Seq<Integer> of(final int... values) {
        return Seq.from(values);
    }

    public static Seq<Long> of(final long... values) {
        return Seq.from(values);
    }

    public static Seq<Double> of(final double... values) {
        return Seq.from(values);
    }
    
    public static <T> Seq<T> empty() {
        return Seq.buildBy(() -> Stream.empty());
    }
    
    public static <T> Seq<T> iterate(final T seed, final UnaryOperator<T> f) {
        return Seq.buildBy(() -> Stream.iterate(seed, f));
    }

    public static Seq<Integer> range(final int start, final int end) {
        return Seq.buildBy(() -> IntStream.range(start, end).boxed());
    }    
}
