package com.acme.todo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.acme.todo.model.Task;
import com.acme.todo.model.TaskGroup;
import com.acme.todo.model.User;

@RunWith(Arquillian.class)
public class TaskServiceTest {
	
	Logger log = Logger.getLogger(this.getClass().getName());

	@Inject
	private TaskService taskservice;
	
	@Inject
	@DefaultUser
	User defaultUser;
	
	@PersistenceContext
    EntityManager em;
	
//	@Inject
//	Cache<String, User> cache;
	
	@Inject
	TaskService taskService;
	
	
	@Deployment
	public static WebArchive createDeployment() {

		return ShrinkWrap
				.create(WebArchive.class, "todo-test.war")
				.addClass(Config.class)
				.addClass(Task.class)
				.addClass(TaskGroup.class)
				.addClass(TaskService.class)
				.addClass(User.class)
				.addClass(DefaultUser.class)
				.addClass(UserService.class)
				//.addAsResource("import.sql")
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
			    .addAsWebInfResource(new File("src/main/webapp/WEB-INF/jboss-deployment-structure.xml"))
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Test
	@InSequence(1)
	public void should_be_deployed() {
		Assert.assertNotNull(taskservice);
	}

	@Test
	@InSequence(2)
	public void testRetrivingTasks() {
		Collection<Task> tasks = taskservice.findAll();
		Assert.assertNotNull(tasks);
	}

	@Test
	@InSequence(3)
	public void testInsertTask() {
		int orgsize = taskservice.findAll().size();
		Task task = new Task();
		task.setTitle("This is a test task");
		task.setCreatedOn(new Date());
		
		taskservice.insert(task);
		Assert.assertEquals(orgsize+1, taskservice.findAll().size());
		
		Task task2 = new Task();
		task2.setTitle("This is another test task");
		task2.setCreatedOn(new Date());
		
		taskservice.insert(task2);
		Assert.assertEquals(orgsize+2, taskservice.findAll().size());
		
		taskservice.delete(task);
		taskservice.delete(task2);
		Assert.assertEquals(orgsize, taskservice.findAll().size());
	}

	@Test
	@InSequence(4)
	public void testUpdateTask() {
		int orgsize = taskservice.findAll().size();
		Task task = new Task();
		task.setTitle("This is the second test task");
		task.setCreatedOn(new Date());
		taskservice.insert(task);
		Assert.assertEquals(orgsize+1, taskservice.findAll().size());

		log.info("###### Inserted task with id " + task.getId());
		task.setDone(true);
		Date taskUpdatedDate = new Date();
		task.setCompletedOn(taskUpdatedDate);
		taskservice.update(task);	
		
		for (Task listTask : taskservice.findAll()) {
			if("This is the second test task".equals(listTask.getTitle())) {
				Assert.assertEquals(true,listTask.isDone());
				Assert.assertNotNull(listTask.getCompletedOn());
			}
		}
		taskservice.delete(task);
		Assert.assertEquals(orgsize, taskservice.findAll().size());
	}
	
	@Test
	@InSequence(5)
	public void testReadPerformance() {
		List<Task> taskList = new ArrayList<Task>();
		// Create 500 tasks
		for (int i = 0; i < 50; i++) {
			taskList.add(generateTestTasks("Test data are some data may be used in a confirmatory way, typically to verify... " + i,true));		
		}
		
		defaultUser.getDefaultTaskGroup().setGroupTasks(taskList);
		
		//Random r = new Random(System.currentTimeMillis());
		long startTime = System.currentTimeMillis();
	
		//Execute 1000 reads
		for (int i = 0; i < 1000; i++) {
			taskList = defaultUser.getDefaultTaskGroup().getGroupTasks();
			log.info("Retrived task list of size " + taskList.size());
			
		}
		long stopTime = System.currentTimeMillis();
		
		log.info("#### Executeing 1000 reads took " + (stopTime-startTime) + " ms");
		
		Assert.assertTrue((stopTime-startTime)<400);
	}
	
	private Task generateTestTasks(String title, boolean done) {
		Task task = new Task();
		task.setTitle(title);
		if(done) {
			task.setCompletedOn(new Date());
			task.setDone(true);
		}
		return task;
	}

}
