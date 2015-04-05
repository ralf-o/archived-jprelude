package org.jprelude.common.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jprelude.common.function.UnaryConsumer;
import org.jprelude.common.function.BinaryConsumer;
import org.jprelude.common.function.UnaryFunction;
import org.jprelude.common.function.BinaryFunction;
import org.jprelude.common.function.TernaryFunction;
import org.jprelude.common.function.UnaryPredicate;
import org.jprelude.common.function.BinaryPredicate;

@FunctionalInterface
public interface Seq<T> {
    Stream<T> stream();
    
    default <R> Seq<R> map(final UnaryFunction<? super T, ? extends R> f) {
        return Seq.from(() -> StreamUtils.stream(Seq.this.stream()).map(f));
    }
   
    default <R> Seq<R> map(final BinaryFunction<? super T, Integer, R> f) {
        final Seq<Integer> nums  = Seq.iterate(0, n -> n + 1);
        return this.zip(nums, (v, n) -> f.apply(v, n));
    }
    
    default <U, R> Seq<R> zip(final Seq<U> otherSeq, final BinaryFunction<? super T, ? super U, R> f) {
        return this.zip(otherSeq, (x, y, i) -> f.apply(x, y));
//        return Seq.from(() ->
//            com.codepoetics.protonpack.StreamUtils.zip(
//                this.stream(), otherSeq.stream(), (x, y) -> f.apply(x, y)));

    }
    
    default <U, R> Seq<R> zip(final Seq<U> otherSeq, final TernaryFunction<? super T, ? super U, Integer, R> f) { 
//        return Seq.from(() -> {
//            final Stream<Indexed<T>> indexedStream = com.codepoetics.protonpack.StreamUtils.zipWithIndex(this.stream());
//            return com.codepoetics.protonpack.StreamUtils.zip(
//                indexedStream,
//                otherSeq.stream(),
//                (indexedValue, otherValue) -> f.apply(indexedValue.getValue(), otherValue, (int) indexedValue.getIndex())
//            );
//        });

        return (otherSeq == null)
            ? Seq.empty()
            : Seq.from(() -> {
                final Iterator generator = new Generator () {
                    Stream<T> stream1 = null;
                    Stream<U> stream2 = null;
                    Iterator<T> iter1 = null;
                    Iterator<U> iter2 = null;
                    int index = -1;

                    @Override
                    protected void init() {
                        this.stream1 = StreamUtils.sequential(Seq.this.stream());
                        this.iter1 = stream1.iterator();
                        this.stream2 = StreamUtils.sequential(otherSeq.stream());
                        this.iter2 = this.stream2.iterator();
                    }

                    @Override
                    protected void dispose() {
                        if (this.stream1 != null) {
                            this.stream1.close();
                            this.stream1 = null;
                            this.iter1 = null;
                        }

                        if (this.stream2 != null) {
                            this.stream2.close();
                            this.stream2 = null;
                            this.iter2 = null;
                        }

                        this.index = -1;
                    };

                    @Override
                    protected void generate() throws Exception {
                        if (iter1.hasNext() && iter2.hasNext()) {
                            this.yield(f.apply(iter1.next(), iter2.next(), ++this.index));
                        }
                    }
                };

                final Spliterator spliterator =  Spliterators.spliteratorUnknownSize(generator, Spliterator.ORDERED);
                final Stream stream = StreamSupport.stream(spliterator, false);
                return stream;
            });
    }
    
    default <U, R> Seq<R> mapFiltered(final UnaryFunction<T, Optional<R>> f) {
        return this.map(f).filter(o -> o.isPresent()).map(o -> o.get());
    }

    default <U, R> Seq<R> mapFiltered(final BinaryFunction<T, Integer, Optional<R>> f) {
        return this.map((v, i) -> f.apply(v, i)).filter(o -> o.isPresent()).map(o -> o.get());
    }
 
    default <R> Seq<R> flatMap(final UnaryFunction<? super T, ? extends Seq<? extends R>> f) {
        return Seq.from(() ->
            StreamUtils.stream(Seq.this.stream())
            .flatMap(v -> f.apply(v).stream()));
    }
    
    default <R> Seq<R> flatMap(final BinaryFunction<? super T, Integer, ? extends Seq<? extends R>> f) {
        final Seq<Integer> ints  = Seq.iterate(0, n -> n + 1);
        return this.zip(ints, (v, n) -> f.apply(v, n)).flatMap(v -> v);
    }
    
    default Seq<T> filter(final UnaryPredicate<? super T> pred) {
        return Seq.from(() -> StreamUtils.stream(Seq.this.stream()).filter(pred));
    }
    
    default Seq<T> filter(final BinaryPredicate<? super T, Integer> pred) {
        final Seq<Integer> ints  = Seq.iterate(0, n -> n + 1);        
        return this.mapFiltered((v, i) -> pred.test(v, i) ? Optional.of(v) : Optional.empty());
    }
    
    default Seq<T> peek(final UnaryConsumer<? super T> action) {
        final UnaryFunction<? super T, T> f = (v) -> {
            action.accept(v);
            return v;
        };
                
        return Seq.from((() -> StreamUtils.stream(this.stream()).map(f)));
    }
    
    
    default Seq<T> peek(final BinaryConsumer<? super T, Integer> consumer) {
        return this.map((v, idx) -> {consumer.accept(v, idx); return v;});
    }
    
    default Seq<T> take(final int n) {
        return Seq.from(() -> StreamUtils.stream(this.stream()).limit(n));
    }
    
    default Seq<T> skip(final int n) {
        return Seq.from(() -> StreamUtils.stream(this.stream()).skip(n));
    }
    
    default <A, R> R collect(final Collector<? super T, A, R> collector) {
        try (final Stream<T> stream = StreamUtils.stream(this.stream())) {
            return stream.collect(collector);
        }
    }
    
    default Seq<T> force() {
        return Seq.from(this.toList());
    }
    
    default Seq<T> sequential() {
        return Seq.from(() ->  StreamUtils.sequential(this.stream()));
    }
    
    default Seq<T> parallel() {
        return Seq.from(() ->  StreamUtils.parallel(this.stream()));
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
    
    default T reduce(final T start, final BinaryFunction<T, T, T> f) {
        final BinaryOperator<T> operator = (v1, v2) -> f.apply(v1, v2);
        return StreamUtils.stream(this.stream()).reduce(start, operator);
    }
    
    default Object[] toArray() {
        return StreamUtils.stream(this.stream()).toArray();
    }
    
    default List<T> toList() {
        return StreamUtils.stream(this.stream()).collect(Collectors.toList());
    }
    
    default void forEach(final UnaryConsumer<? super T> action) {
        try (final Stream<T> stream = StreamUtils.stream(this.stream())) {
            stream.forEach(action);
        }
    }
    
    default void forEach(final BinaryConsumer<? super T, Integer> action) {
        final Seq<Integer> nums  = Seq.iterate(0, n -> n + 1);
 
        this.zip(nums, (v, n) -> new Object[] {v, n})
            .forEach(pair -> action.accept((T) pair[0], (int) pair[1]));
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
    
    public static <T> Seq<T> iterate(final T seed, final UnaryOperator<T> f) {
        return Seq.from(() -> Stream.iterate(seed, f));
    }

    public static Seq<Integer> range(final int start, final int end) {
        return Seq.from(() -> IntStream.range(start, end).boxed());
    }    
}
