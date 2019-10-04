package com.poc.cache;

import java.util.concurrent.TimeUnit;

import com.poc.cache.vo.Address;

public class AddressCache extends AbstractCache implements Cache<Address> {

	Cache<Address> cache;

	public AddressCache(long maxAge, TimeUnit unit) {
		super(maxAge, unit);
		cache = new SimpleCache<>(maxAge, unit);
	}

	@Override
	public boolean add(Address obj) {
		return cache.add(obj);
	}

	@Override
	public boolean remove(Address obj) {
		return cache.remove(obj);
	}

	@Override
	public Address peek() {
		return cache.peek();
	}

	@Override
	public Address take() {
		return cache.take();
	}
}
