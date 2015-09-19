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
 * Handles the case that a Logic fails, either by throwing an exception
 * or passing a throwable to its trigger's <code>trigger()</code> method.
 *
 * @author Tim Boudreau
 */
public interface FailureHandler {

    /**
     * Called when something fails.
     * 
     * @param <T> The input type of the logic that failed
     * @param key The key, if any
     * @param input The input data at the time of failure
     * @param thrown The exception that was encountered
     * @return true if the promise is a chained one, and earlier elements in
     * the chain may also have failure handlers that should be notified
     */
    <T> boolean onFailure(PromiseContext.Key<T> key, T input, Throwable thrown, PromiseContext context);
    
}
