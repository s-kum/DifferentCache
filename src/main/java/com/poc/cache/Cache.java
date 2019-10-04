package com.poc.cache;

public interface Cache<T> {

	/**
	 * add() method must store unique elements only (existing elements must be
	 * ignored). This will return true if the element was successfully added.
	 * 
	 * @param address
	 * @return
	 */

	public boolean add(T obj);

	/**
	 * remove() method will return true if the address was successfully removed
	 * 
	 * @param address
	 * @return
	 */
	public boolean remove(T obj);

	/**
	 * The peek() method will return the most recently added element, null if no
	 * element exists.
	 * 
	 * @return
	 */
	public T peek();

	/**
	 * take() method retrieves and removes the most recently added element from the
	 * cache and waits if necessary until an element becomes available.
	 * 
	 * @return
	 */
	public T take();
	
}
