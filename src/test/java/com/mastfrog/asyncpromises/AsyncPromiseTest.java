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

import com.mastfrog.asyncpromises.PromiseContext.Key;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
public class AsyncPromiseTest {

    @Test
    public void testCreate_Logic() {
        StringLogic a = new StringLogic("a");
        StringLogic b = new StringLogic("b");
        StringLogic c = new StringLogic("c");
        StringLogic d = new StringLogic("d");
        StringLogic e = new StringLogic("e");

        AsyncPromise<String, String> p = AsyncPromise.create(a).then(b).then(c).then(d).then(e);
        p.start("x", new Trigger<String>() {

            @Override
            public void trigger(String obj, Throwable thrown) {
                assertNull(thrown);
                assertEquals("xabcde", obj);
            }
        });
        final Throwable[] thrown = new Throwable[1];
        FailureHandler h = new FailureHandler() {

            @Override
            public <T> boolean onFailure(Key<T> key, T input, Throwable th, PromiseContext ctx) {
                System.out.println("OnFailure Key " + key + " in " + input);
                assertEquals("xabcde", input);
//                assertSame(KEYF, key);
                thrown[0] = th;
                return true;
            }

        };
        AsyncPromise<String,String> base = AsyncPromise.create(a).then(b);
        p = base.then(c).onFailure(h).then(d).then(e).usingKey(KEYF).then(new Failer());
        final boolean[] called = new boolean[1];
        p.start("x", new Trigger<String>() {

            @Override
            public void trigger(String obj, Throwable thrown) {
                called[0] = true;
                assertNotNull(obj, thrown);
                assertNull(obj);
            }
        });
        assertNotNull(thrown[0]);
        assertFalse(called[0]);
    }

    static class Failer implements SimpleLogic<String, String> {

        @Override
        public void run(String data, Trigger<String> next) throws Exception {
            next.trigger(null, new IllegalArgumentException("bad"));
        }
    }

    private static final Key<String> KEYF = PromiseContext.newKey(String.class);
    private static final Key<Integer> KEY = PromiseContext.newKey(Integer.class);

    private static class StringLogic implements Logic<String, String> {

        private final String append;

        public StringLogic(String append) {
            this.append = append;
        }

        @Override
        public void run(String data, Trigger<String> next, PromiseContext context) throws Exception {
            System.out.println(append + " -> " + data);
            if ("x".equals(data)) {
                context.put(KEY, 42);
            } else {
                assertEquals("In " + data + " with " + append, Integer.valueOf(42), context.get(KEY));
            }
            next.trigger(data + append, null);
        }

    }
}
