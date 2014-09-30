package com.acme.todo.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

@Entity
public class TaskGroup {
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 4120823854642318674L;
	
	@Id
	private String name;
	
	private String publicName;
//	
//	@OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.MERGE)
//	@JoinColumn(name = "GROUPID_FK")
	@OneToMany(cascade = CascadeType.MERGE, mappedBy = "group", orphanRemoval = true)
	private List<Task> groupTasks = new ArrayList<Task>();
	
	public TaskGroup() {
		super();
	}
	
	public TaskGroup(String name) {
		this(name,name);
	}
	
	public TaskGroup(String name, String publicName) {
		super();
		this.name = name;
		this.publicName = publicName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<Task> getGroupTasks() {
		return groupTasks;
	}

	public void setGroupTasks(List<Task> groupTasks) {
		this.groupTasks = groupTasks;
	}

	public String getPublicName() {
		return publicName;
	}

	public void setPublicName(String publicName) {
		this.publicName = publicName;
	}
	
	
}
