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
 * Implementation of a single promise.
 *
 * @author Tim Boudreau
 */
final class PromiseImpl<T, R> extends AsyncPromise<T, R> {

    private PromiseContext context;
    private AsyncPromise<?, T> parent;
    private FailureHandler onFailure;
    private final LogicWrapper<R> logic;
    private Key<T> key;

    PromiseImpl(final SimpleLogic<T, R> logic) {
        this(new Logic<T,R>(){

            @Override
            public void run(T data, Trigger<R> next, PromiseContext context) throws Exception {
                logic.run(data, next);
            }
        });
    }

    PromiseImpl(Logic<T, R> logic) {
        this.logic = new LogicWrapper<>(logic);
    }

    @Override
    public PromiseImpl<T, R> usingKey(PromiseContext.Key<T> key) {
        this.key = key;
        return this;
    }

    @Override
    public <S> AsyncPromise<T, S> then(AsyncPromise<R, S> logic) {
        WrapperPromise<T, R, S> p = new WrapperPromise<>(this, logic);
        return p;
    }

    @Override
    public PromiseImpl<T, R> start(T input, Trigger<R> trigger) {
        if (input != null && key != null) {
            context().put(key, input);
        }
        try {
            logic.run(input, trigger, context());
        } catch (Exception ex) {
            failed(ex, key, input);
        }
        return this;
    }

    @Override
    Key<T> key() {
        return key;
    }

    @Override
    <R> void failed(Throwable thrown, Key<R> key, R input) {
        boolean propagate = true;
        if (hasFailureHandler()) {
            propagate = onFailure.onFailure(key, input, thrown, context());
        }
        if (propagate && parent != null) {
            parent.failed(thrown, key, input);
        }
    }

    @Override
    public AsyncPromise<T, R> onFailure(FailureHandler failure) {
        if (parent != null && !parent.hasFailureHandler()) {
            parent.onFailure(failure);
        } else {
            this.onFailure = failure;
        }
        return this;
    }

    @Override
    void setParent(AsyncPromise<?, T> parent) {
        if (this.parent != null) {
            throw new IllegalStateException("This promise is already part of a chain");
        }
        this.parent = parent;
    }

    @Override
    PromiseContext context() {
        if (parent != null) {
            return parent.context();
        }
        if (context == null) {
            context = new PromiseContext();
        }
        return context;
    }

    @Override
    boolean hasFailureHandler() {
        return this.onFailure != null;
    }

    private final class LogicWrapper<R> implements Logic<T, R>, Trigger<R> {

        private final Logic<T, R> real;
        private Trigger<R> next;
        private T data;

        public LogicWrapper(Logic<T, R> real) {
            this.real = real;
        }

        @Override
        public void run(T data, Trigger<R> next, PromiseContext context) throws Exception {
            this.next = next;
            this.data = data;
            real.run(data, this, context);
        }

        @Override
        public void trigger(R obj, Throwable thrown) {
            if (thrown != null) {
                failed(thrown, key(), data);
                return;
            }
            next.trigger(obj, thrown);
            this.next = null;
            this.data = null;
        }
    }
}
