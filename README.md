Asynchronous Promises
=====================

This is a simple library for implementing the "promise" idiom in Java for the case that work is run asynchronously.

I.e.

		AsyncPromise.create((Foo data, Trigger<Bar> trigger) -> {
			someLib.asyncCreateTheBar((Bar bar) -> { trigger(bar, null) });
		});

The specific use case it was initially built for was putting a more intuitive API over MongoDB's async Java driver,
but this library has no dependencies and is useful for any similar use case.

The usually aspects of promises apply:

 * The work will be run sequentially
 * They can be chained together - AsyncPromise<A,B> + AsyncPromise<B,C> gets you an AyncPromise<A,C>
 * A PromiseContext allows data to be passed between promises
 * Failure handlers callbacks can be attached to individual promises, and failures propagate backward to the nearest promise in the chain that has one, optionally continuing back if there are multiple handlers
 * Promises of heterogenous types can be chained together




