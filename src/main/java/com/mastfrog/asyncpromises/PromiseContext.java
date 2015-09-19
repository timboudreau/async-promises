/*
 * The MIT License
 *
 * Copyright 2015 Tim Boudreau.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mastfrog.asyncpromises;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Allows individual Logics to pass data to other Logics later in the sequence,
 * in the case of compound promises.
 *
 * @author Tim Boudreau
 */
public class PromiseContext {

    private final Map<Key<?>, Object> map = new IdentityHashMap<>();

    public <T> PromiseContext put(Key<T> key, T obj) {
        map.put(key, key.cast(obj));
        return this;
    }

    public <T> T get(Key<T> key) {
        Object o = map.get(key);
        return key.cast(o);
    }

    /**
     * Create a key that can be used to store and retrieve context contents. The
     * equals contract is identity, so multiple keys with the same type may be
     * used - the reference to the key you want to use must be known to all the
     * logic instances that want to use it.
     *
     * @param <T> The type of object.
     * @param key The key type
     * @return A key
     */
    public static <T> Key<T> newKey(Class<T> key) {
        return new Key<>(key);
    }

    public static final class Key<T> {

        private final Class<T> type;

        Key(Class<T> type) {
            this.type = type;
        }

        public T cast(Object o) {
            return type.cast(o);
        }

        public Class<T> type() {
            return type;
        }
    }
}
