package com.poc.cache;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class ConcurrentCache<T> extends AbstractCache implements Cache<T> {

	private Set<CacheEntry<T>> cache = ConcurrentHashMap.newKeySet();
	// Wanted to use stack, but used deque for using stack.
	// also had to use a blocking data structure.
	private ConcurrentLinkedDeque<CacheEntry<T>> freqAccElms = new ConcurrentLinkedDeque<>();
	Lock lock = new ReentrantLock();
	Condition waiter = lock.newCondition();

	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public ConcurrentCache(long maxAge, TimeUnit unit) {
		super(maxAge, unit);
		scheduleCleanUp();
	}

	private long time() {
		return System.currentTimeMillis();
	}

	@Override
	public boolean add(T obj) {
		CacheEntry<T> elm = new CacheEntry<T>(obj, time());
		// existing elements need to be ignored._
		if (cache.contains(elm))
			return false;
		else {
			lock(elm, () -> {
				cache.add(elm);
				freqAccElms.offerFirst(elm);
			});

			return true;
		}
	}

	private void lock(CacheEntry<T> elm, Runnable r) {
		lock.lock();
		r.run();
		waiter.signalAll();
		lock.unlock();
	}

	@Override
	public boolean remove(T obj) {
		CacheEntry<T> elm = new CacheEntry<T>(obj, time());
		if (cache.contains(elm)) {
			cache.remove(elm);
			// only single occurrence would be there for the element.
			freqAccElms.removeFirstOccurrence(elm);
			return true;
		}
		return false;
	}

	@Override
	public T peek() {
		CacheEntry<T> elm = freqAccElms.peek();
		if (elm == null)
			return null;
		else {
			if (isNodeExpired(elm, this.unit.toMillis(this.maxAge), time()))
				remove(elm.entry);
			return elm.entry;
		}
	}

	@Override
	public T take() {
		CacheEntry<T> elm;
		try {
			elm = freqAccElms.poll();

			if (elm == null) {
				await();
				return take();
			} else {
				if (isNodeExpired(elm, this.unit.toMillis(this.maxAge), time()))
					take();

				T entry = elm.entry;
				cache.remove(elm);
				return entry;
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private void await() throws InterruptedException {
		lock.lock();
		waiter.await();
		lock.unlock();
	}

	private boolean isNodeExpired(CacheEntry<T> entry, long duration, long currentTime) {
		return currentTime > (entry.startTime + duration);
	}

	private void scheduleCleanUp() {
		scheduler.scheduleAtFixedRate(() -> {
			long duration = this.unit.toMillis(this.maxAge);
			long currentTime = time();

			for (Iterator<CacheEntry<T>> iterator = freqAccElms.iterator(); iterator.hasNext();) {
				CacheEntry<T> cacheEntry = iterator.next();
				if (isNodeExpired(cacheEntry, duration, currentTime)) {
					iterator.remove();
					cache.remove(cacheEntry);
				}
			}
		}, 1, 1, TimeUnit.SECONDS);
	}

	@Override
	public String toString() {
		return this.freqAccElms.toString();
	}

}
