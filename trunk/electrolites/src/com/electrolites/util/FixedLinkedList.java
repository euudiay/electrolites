package com.electrolites.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class FixedLinkedList<T> {

	public LinkedList<T> list;
	public int capacity;
	
	public FixedLinkedList(int capacity) {
		list = new LinkedList<T>();
		this.capacity = capacity;
	}
	
	public void addAll(Collection<T> newList) {
		int newTs = newList.size();
		
		// If no width's specified, just add them all
		if (capacity < 0)
			list.addAll(newList);
		else {
			// Remove from head those that doesn't fit in
			if (list.size() + newTs >= capacity) {
				int toRemove = ((list.size() + newTs) - capacity);
				for (int i = 0; i < toRemove; i++) {
					if (list.isEmpty())
						break;
					try {
						T p = list.removeFirst();
						p = null;
					} catch (NoSuchElementException e) {
						e.printStackTrace();
					}
				}
			}
			// Add new ones at end
			list.addAll(list);
		}
	}
	
	public void add(T elem) {
		if (capacity > 0) {
			if (list.size() + 1 >= capacity)
				list.remove();
		}
		list.add(elem);
	}

	public T get() {
		return list.remove();
	}
	
	/** Returns a shallow bunch of elements **/ 
	public Collection<T> getBunch(int ammount) {
		Collection<T> bunch = new LinkedList<T>();

		int toReturn = Math.min(ammount, list.size());
		for (int i = 0; i < toReturn; i++)
			bunch.add(list.remove());
		
		return bunch;
	}
	
	public int size() {
		return list.size();
	}
}
