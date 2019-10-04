package com.poc.cache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Test;

import junit.framework.TestCase;

public class SimpleCacheTest2 extends TestCase {

	@Test
	public void testAdds() {
		Cache<String> cache = new ConcurrentCache<>(2, TimeUnit.SECONDS);
		boolean test = cache.add("1") && cache.add("2");
		boolean cond1 = cache.take().equals("2");
		boolean cond2 = cache.take().equals("1");
		assertTrue(cond1 && cond2 && test);
	}

	@Test
	public void testNegativeAdds() {
		Cache<String> cache = new ConcurrentCache<>(2, TimeUnit.SECONDS);
		boolean cond1 = cache.add("1");
		boolean cond2 = cache.add("1");
		assertTrue(cond1 == true);
		assertTrue(cond2 == false);
	}

	@Test
	public void testCacheExpiry() throws InterruptedException {

		Cache<String> cache = new ConcurrentCache<>(2, TimeUnit.SECONDS);
		cache.add("1");
		boolean nodeAvailableEarlier = cache.peek() != null;
		Thread.sleep(TimeUnit.SECONDS.toMillis(4));
		boolean isNodeEvicted = cache.peek() == null;
		System.out.println(
				String.format("isNodeEvicted= %s,  nodeAvailableEarlier = %s", isNodeEvicted, nodeAvailableEarlier));
		assertTrue(isNodeEvicted && nodeAvailableEarlier);
	}

	@Test
	public void testRemove() {
		Cache<String> cache = new ConcurrentCache<>(20, TimeUnit.SECONDS);
		cache.add("1");
		assertTrue(cache.remove("1"));
	}

	@Test
	public void testNegativeRemove() {
		Cache<String> cache = new ConcurrentCache<>(20, TimeUnit.SECONDS);
		assertFalse(cache.remove("1"));
	}

	@Test
	public void testPeek() {
		Cache<String> cache = new ConcurrentCache<>(20, TimeUnit.SECONDS);
		cache.add("1");
		cache.add("2");
		assertTrue(cache.peek().equals("2"));
	}

	@Test
	public void testNegativePeek() {
		Cache<String> cache = new ConcurrentCache<>(20, TimeUnit.SECONDS);
		assertTrue(cache.peek() == null);
	}

	@Test
	public void testTake() throws InterruptedException {
		Cache<String> cache = new ConcurrentCache<>(2, TimeUnit.SECONDS);
		long startTime = System.currentTimeMillis();

		CompletableFuture.runAsync(() -> {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
			}
			cache.add("1");
		});
		cache.take();
		long endTime = System.currentTimeMillis();
		assertTrue(endTime - startTime > TimeUnit.SECONDS.toMillis(1));
	}

	@Test
	public void testConcurrentAdds() {
		Cache<String> cache = new ConcurrentCache<>(20, TimeUnit.SECONDS);
		IntStream.range(1, 50000).parallel().forEach(i -> {
			cache.add(i + "");
			cache.peek();
		});
		assertTrue(cache.peek() != null);
	}
}
