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
 * An asynchronous promise.  Unlike traditional promises that can be chained
 * together, these promises expect to that the logic they will trigger runs
 * asynchronously, and that continuing the chain of promises will occur some
 * time in the future.
 * <p>
 * Promises can be chained together.
 * </p>
 * <p>Asynchronous promises have a PromiseContext which can be used to pass
 * additional objects along the chain of promises.
 * </p>
 *
 * @author Tim Boudreau
 */
public abstract class AsyncPromise<T, R> {

    public static <T, R> AsyncPromise<T, R> create(Logic<T, R> toRun) {
        return new PromiseImpl<>(toRun);
    }

    public static <T, R> AsyncPromise<T, R> create(SimpleLogic<T, R> toRun) {
        return new PromiseImpl<>(toRun);
    }
    
    abstract Key<T> key();

    /**
     * Run the promise, invoking the passed trigger with a result when the work
     * has completed.
     * @param input The input data
     * @param onDone The trigger
     * @return this
     */
    public abstract AsyncPromise<T, R> start(T input, Trigger<R> onDone);

    /**
     * Run the promise, using a no-op trigger, for promises that do not
     * need to pass back a result.
     * @param input The input
     * @return this
     */
    public final AsyncPromise<T, R> start(final T input) {
        AsyncPromise.this.start(input, new Trigger<R>() {
            @Override
            public void trigger(R obj, Throwable thrown) {
                if (thrown != null) {
                    failed(thrown, key(), input);
                }
            }
        });
        return this;
    }

    /**
     * Use this key to attach the input to the promise context.  If set,
     * the input parameter will be automatically added to the context before
     * subsequent promises are called.
     * 
     * @param key They key
     * @return this
     */
    public abstract AsyncPromise<T, R> usingKey(PromiseContext.Key<T> key);

    /**
     * Run an additional promise with new input data when this one 
     * completes.
     * 
     * @param <Q> The input type to the next promise
     * @param <V> The output type of the next promise
     * @param q The input to the next promise
     * @param p The next promise
     * @return A promise that merges this and the passed one
     */
    public final <Q, V> AsyncPromise<T, V> then(final Q q, final AsyncPromise<Q, V> p) {
        AsyncPromise<R, Q> interim = new PromiseImpl<>(new Logic<R, Q>() {
            @Override
            public void run(R data, Trigger<Q> next, PromiseContext context) throws Exception {
                next.trigger(q, null);
            }
        });
        return this.then(interim).then(p);
    }

    /**
     * Run an additional promise with new input data when this one 
     * completes.
     * 
     * @param <Q> The input type to the next promise
     * @param <V> The output type of the next promise
     * @param q The input to the next promise
     * @param logic p The next unit of logic to run
     * @return A promise that merges this and the passed one
     */
    public final <Q, V> AsyncPromise<T, V> then(final Q q, final Logic<Q, V> logic) {
        AsyncPromise<Q, V> promise = new PromiseImpl<>(logic);
        return then(q, promise);
    }

    /**
     * Chain another promise to be run against the output of this one.
     * 
     * @param <S> The type of the next promise's output
     * @param logic The next promise
     * @return A promise which combine this and the next one
     */
    public abstract <S> AsyncPromise<T, S> then(AsyncPromise<R, S> logic);
    
    /**
     * Chain another promise to be run against the output of this one.
     * 
     * @param <S> The type of the next promise's output
     * @param next The logic to run against this promise's result
     * @return A promise which combine this and the next one
     */
    public final <S> AsyncPromise<T, S> then(final SimpleLogic<R, S> next) {
        return then(new Logic<R,S>(){

            @Override
            public void run(R data, Trigger<S> trigger, PromiseContext context) throws Exception {
                next.run(data, trigger);
            }
        });
    }

    /**
     * Chain another promise to be run against the output of this one.
     * 
     * @param <S> The type of the next promise's output
     * @param next The logic to run against this promise's result
     * @return A promise which combine this and the next one
     */
    public final <S> AsyncPromise<T, S> then(Logic<R, S> next) {
        AsyncPromise<R, S> p = new PromiseImpl<>(next);
        return then(p);
    }

    abstract void setParent(AsyncPromise<?, T> parent);

    abstract <R> void failed(Throwable thrown, PromiseContext.Key<R> key, R input);
    
    /**
     * Attach a failure handler to handle asynchronous failures.
     * The handler will be notified if promise execution is aborted
     * because of an error, and may run cleanup logic or whatever
     * is necessary.
     * <p>
     * If multiple faiure handlers are added at different points in
     * assembling a chain of AsyncPromises, the one nearest to the
     * failure will be called first;  if it returns true, earlier
     * failure handlers will also be notified - the failure notification
     * propagates backward toward the beginning of the chain.
     * </p>
     * 
     * @param failure The handler
     * @return this
     */
    public abstract AsyncPromise<T, R> onFailure(FailureHandler failure);

    abstract boolean hasFailureHandler();

    abstract PromiseContext context();
}
