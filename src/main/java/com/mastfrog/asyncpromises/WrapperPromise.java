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

/**
 * Combines two promises into one that spans the input type of the first and the
 * output type of the second.
 *
 * @author Tim Boudreau
 */
class WrapperPromise<T, R, S> extends AsyncPromise<T, S> {

    private final AsyncPromise<T, R> first;
    private final AsyncPromise<R, S> second;

    public WrapperPromise(AsyncPromise<T, R> first, AsyncPromise<R, S> second) {
        this.first = first;
        this.second = second;
        second.setParent(first);
    }

    Key<T> key() {
        return first.key();
    }

    @Override
    public AsyncPromise<T, S> start(final T input, final Trigger<S> trigger) {
        first.start(input, new Trigger<R>() {
            @Override
            public void trigger(R obj, Throwable thrown) {
                if (thrown != null) {
                    first.failed(thrown, first.key(), input);
                } else {
                    second.start(obj, trigger);
                }
            }
        });
        return this;
    }

    @Override
    public AsyncPromise<T, S> onFailure(FailureHandler failure) {
        first.onFailure(failure);
        return this;
    }

    @Override
    public <U> AsyncPromise<T, U> then(AsyncPromise<S, U> logic) {
        return new WrapperPromise<>(this, logic);
    }

    @Override
    void setParent(AsyncPromise<?, T> parent) {
        first.setParent(parent);
    }

    @Override
    PromiseContext context() {
        return second.context();
    }

    @Override
    public AsyncPromise<T, S> usingKey(PromiseContext.Key<T> key) {
        first.usingKey(key);
        return this;
    }

    @Override
    boolean hasFailureHandler() {
        return second.hasFailureHandler() || first.hasFailureHandler();
    }

    @Override
    <R> void failed(Throwable thrown, Key<R> key, R input) {
        second.failed(thrown, key, input);
    }
}
