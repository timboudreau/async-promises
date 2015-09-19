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

/**
 * Logic which is run by a promise.
 *
 * @author Tim Boudreau
 */
public interface Logic<T, R> {

    /**
     * Perform the operation that this promise, or step in a chained promise
     * does.
     * 
     * @param data The input data
     * @param next This object's <code>trigger()</code> method <b>must always</b>
     * be called once this logic has run, to trigger the next one
     * @param context The PromiseContext, which can be used for Logic implementations
     * to communicate with each other
     * @throws Exception If something goes wrong, in which case the owning
     * promise's FailureHandler will be invoked.
     */
    public void run(T data, Trigger<R> next, PromiseContext context) throws Exception;
    
}
