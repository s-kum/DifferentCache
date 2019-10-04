package com.poc.cache;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class CacheEntry<T> {
	T entry;
	long startTime;

	@Override
	public int hashCode() {
		return this.entry.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!obj.getClass().equals(CacheEntry.class))
			return false;
		CacheEntry<T> otherObj = (CacheEntry) obj;
		return this.entry.equals(otherObj.entry);
	}

}

public class SimpleCache<T> extends AbstractCache implements Cache<T> {

	private Set<CacheEntry<T>> cache = new HashSet<>();
	// Wanted to use stack, but used deque for using stack.
	// also had to use a blocking data structure.
	private LinkedBlockingDeque<CacheEntry<T>> freqAccElms = new LinkedBlockingDeque<>();

	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public SimpleCache(long maxAge, TimeUnit unit) {
		super(maxAge, unit);
		scheduleCleanUp();
	}

	private long time() {
		return System.currentTimeMillis();
	}

	@Override
	public synchronized boolean add(T obj) {
		CacheEntry<T> elm = new CacheEntry<T>(obj, time());
		// existing elements need to be ignored._
		if (cache.contains(elm))
			return false;
		else {
			cache.add(elm);
			freqAccElms.offerFirst(elm);
			return true;
		}
	}

	@Override
	public synchronized boolean remove(T obj) {
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
			elm = freqAccElms.take();

			if (elm == null)
				return null;
			else {

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
