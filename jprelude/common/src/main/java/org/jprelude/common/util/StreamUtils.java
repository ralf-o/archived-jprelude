/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jprelude.common.util;

import java.util.stream.Stream;

public class StreamUtils {
    public static <T> Stream<T> stream(Stream<T> stream) {
        return (stream == null ? Stream.empty() : stream);
    }
    
    public static <T> Stream<T> sequential(Stream<T> stream) {
        final Stream<T> ret;
        
        if (stream == null) {
            ret = Stream.empty();
        } else {
            if (stream.isParallel()) {
                ret = stream.sequential();
            } else {
                ret = stream;
            }
        }
        
        return ret;
    }

    public static <T> Stream<T> parallel(final Stream<T> stream) {
        final Stream<T> ret;
        
        if (stream == null) {
            ret = Stream.<T>empty().parallel();
        } else {
            if (stream.isParallel()) {
                ret = stream;
            } else {
                ret = stream.parallel();
            }
        }
        
        return ret;
    }
}
