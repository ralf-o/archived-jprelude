package org.jprelude.common.util;

import org.junit.Test;

public class ObservableTest {
    @Test
    public void testEventStream() {
        final Observable<Integer> str1 = Seq.range(0, 5).toObservable();
        final Observable<Integer> str2 = Seq.range(5, 10).toObservable();
        final Observable<Integer> str3 = Seq.range(10, 15).toObservable();
        
        final Observable<Integer> merged = Observable.concat(str1, str2, str3);
    
        final Observable<Integer> test1 = str1.takeWhile((item, idx) -> {if (idx == 10) throw new RuntimeException("xxx") ;return true;});
        
        
        final Subscription subscription = test1.subscribe(new Observer<Integer>() { 
            @Override
            public void onNext(Integer item) {
                
                System.out.println(item);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(">> onError");
            }

            @Override
            public void onComplete() {
                System.out.println(">> onComplete");
            }

            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println(">> onSubscribe");
            }
            
        });
        
        merged.filter(n -> n % 2 == 0).forEach(System.out::println);
        
        //subscription.request(13);
    
       // test1.forEach(System.out::println, System.out::println);
        
        
    }
}
        
/*        
        Observable<Observable<Integer>> combined = Observable.of(str1, str2, str3);
        Observable.flatten(combined).subscribe(System.out::println);
        //Seq.range(1, 11).toEventStream().map(n -> n * n).subscribe(System.out::println);
    
        Observable.range(1, 100).takeWhile(n -> {if (n == 30) throw new RuntimeException("Juhuu"); return n < 50;}).subscribe(System.out::println);
    }
*/
   
    
