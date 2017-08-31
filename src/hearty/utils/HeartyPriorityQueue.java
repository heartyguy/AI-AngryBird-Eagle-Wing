/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2015, Team DataLab Birds: 
 ** Karel Rymes, Radim Spetlik, Tomas Borovicka
 ** Team HeartyTian: Tian Jian Wang, Tomas Rehorek
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package hearty.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 *
 * @author Tomas Rehorek
 * 
 *         Encapsulates java.util.PriorityQueue so that no Comparator is needed.
 */
public class HeartyPriorityQueue<T> {

	private PriorityQueue<T> internalQueue;
	private Map<T, Double> priorities;

	public HeartyPriorityQueue() {
		priorities = new HashMap<T, Double>();

		internalQueue = new PriorityQueue<T>(100, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return (int) Math.signum(
						HeartyPriorityQueue.this.priorities.get(o1) - HeartyPriorityQueue.this.priorities.get(o2));
			}
		});
	}

	/**
	 * Add an element to the queue, assigning given priority
	 * 
	 * @param item
	 *            The element to be added
	 * @param priority
	 *            Priority of the element (lower number means higher priority)
	 */
	public void enqueue(T item, double priority) {
		this.priorities.put(item, priority);
		this.internalQueue.add(item);
	}

	/**
	 * Gets and removes (polls) the first element from the queue.
	 * 
	 * @return The element of highest priority
	 */
	public T dequeue() {
		this.priorities.remove(this.internalQueue.peek());
		return this.internalQueue.poll();
	}

	/**
	 * Updates the priority of given element currently present in the queue.
	 * 
	 * @param item
	 *            The item priority of which shall be updated
	 * @param priority
	 *            The new priority (lower number means higher priority)
	 */
	public void updateKey(T item, double priority) {
		this.priorities.put(item, priority);
		this.internalQueue.remove(item);
		this.internalQueue.add(item);
	}

	/**
	 * Test whether there are any element in the queue.
	 * 
	 * @return True if no elements are present.
	 */
	public boolean isEmpty() {
		return this.internalQueue.isEmpty();
	}

	/**
	 * Test whether given element is currently present in the queue.
	 * 
	 * @param item
	 *            The element to be tested for presence.
	 * @return True if the element is present.
	 */
	public boolean contains(T item) {
		return this.internalQueue.contains(item);
	}
}
