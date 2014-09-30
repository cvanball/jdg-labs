package com.acme.todo;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.infinispan.Cache;

import com.acme.todo.model.Task;
import com.acme.todo.model.User;

/**
 * This class is used to query, insert or update Task object.
 * 
 * @author tqvarnst
 * 
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TaskService {

	@PersistenceContext
	EntityManager em;

	@Inject
	@DefaultUser
	User currentUser;

	@Inject
	UserService userService;

	@Inject
	Cache<String, User> cache;

	Logger log = Logger.getLogger(this.getClass().getName());

	/**
	 * This methods should return all cache entries, currently contains mockup
	 * code.
	 * 
	 * @return
	 * 
	 *         DONE: Replace implementation with Cache.values()
	 */
	public Collection<Task> findAll() {
		User user = cache.get(currentUser.getUsername());
		if(user==null) {
			user = em.find(User.class, currentUser.getUsername());
			if(user!=null) {
				cache.put(currentUser.getUsername(), user);
			} else {
				throw new RuntimeException("Failed to find user with username " + currentUser.getUsername());
			}
		}
		List<Task> tasks = user.getDefaultTaskGroup().getGroupTasks();
		
		Collections.sort(tasks, new Comparator<Task>() {
			@Override
			public int compare(Task o1, Task o2) {
				if (o1.isDone() == o2.isDone()) {
					return o2.getCreatedOn().compareTo(o1.getCreatedOn());
				} else if (o1.isDone()) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		return tasks;
	}

	/**
	 * This method persists a new Task instance
	 * 
	 * @param task
	 * 
	 *            DONE: Add implementation to also update the Cache with the new
	 *            object
	 */
	public void insert(Task task) {
		if(task.getCreatedOn()==null) {
			task.setCreatedOn(new Date());
		}
		if(task.getGroup()==null) {
			task.setGroup(currentUser.getDefaultTaskGroup());
		}
		em.persist(task);
		
		User user = cache.get(currentUser.getUsername());
		user.getDefaultTaskGroup().getGroupTasks().add(task);
		cache.replace(user.getUsername(), user);
	}

	/**
	 * This method persists an existing Task instance
	 * 
	 * @param task
	 * 
	 *            DONE: Add implementation to also update the Object in the
	 *            Cache
	 */
	public void update(Task task) {
		task.setGroup(currentUser.getDefaultTaskGroup());
		Task mergedTask = em.merge(task);
		em.detach(mergedTask);

		User user = cache.get(currentUser.getUsername());
		List<Task> groupTasks = user.getDefaultTaskGroup().getGroupTasks();
		int index = groupTasks.indexOf(mergedTask);
		if(index==-1)
			throw new RuntimeException("Cannot find the task to update in existing groupTasks");
		groupTasks.set(index, mergedTask);
		
//		List<Task> groupTasks = currentUser.getDefaultTaskGroup().getGroupTasks();
//		int index = groupTasks.indexOf(t2);
//		if(index==-1)
//			throw new RuntimeException("Cannot find the task to update in existing groupTasks");
//		groupTasks.set(index, t2);
//		cache.replace(currentUser.getUsername(), currentUser);
	}
//
	/**
	 * This method deletes an Task from the persistence store
	 * 
	 * @param task
	 * 
	 *            DONE: Add implementation to also delete the object from the
	 *            Cache
	 */
	public void delete(Task task) {
		User user = cache.get(currentUser.getUsername());
		List<Task> groupTasks = user.getDefaultTaskGroup().getGroupTasks();
		int index = groupTasks.indexOf(task);
		if(index==-1)
			throw new RuntimeException("Cannot find the task to delete in existing groupTasks");
		groupTasks.remove(index);
		cache.replace(user.getUsername(), user);
//		currentUser.getDefaultTaskGroup().getGroupTasks().remove(task);
		em.remove(em.find(Task.class, task.getId()));
//		cache.replace(currentUser.getUsername(), currentUser);
	}
	
	/**
	 * This method deletes an Task from the persistence store
	 * 
	 * @param task
	 * 
	 *            DONE: Add implementation to also delete the object from the
	 *            Cache
	 */
	public void delete(Long taskId) {
		Task t0 = new Task();
		t0.setId(taskId);
		delete(t0);
	}

	/**
	 * This method is called after construction of this SLSB.
	 * 
	 * DONE: Replace implementation to read existing Tasks from the database and
	 * add them to the cache
	 */
	@PostConstruct
	public void startup() {
	}

}
