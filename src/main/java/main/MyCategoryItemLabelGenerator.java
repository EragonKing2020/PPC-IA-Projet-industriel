package main;

import java.time.Duration;
import java.time.ZoneId;

import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;

import data.Activity;
import data.Station;
import data.Worker;
import data.Workshop;

public class MyCategoryItemLabelGenerator implements CategoryItemLabelGenerator{
	private int index;
	private Workshop workshop;
	private Station station;
	private Worker worker;
	private String label;

	public MyCategoryItemLabelGenerator(String label, Workshop workshop, Station station, Worker worker) {
		super();
		this.label = label;
		// this.index = index;
		this.workshop = workshop;
		this.station = station;
		this.worker = worker;
	}
	
	
	
	@Override
	public String generateLabel(CategoryDataset dataSet, int series, int categories) {
		return this.label;
		// Task task = ((TaskSeries) dataSet.getRowKeys().get(series)).get(categories);
		// System.out.println("Task : "+task.getSubtaskCount());
		// for(Activity activity : workshop.getActivities()) {
		// 	int tDebut = (int)Duration.between(workshop.getShifts()[0].getStart(), task.getSubtask(index).getDuration().getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()).toMinutes();
		// 	int tFin = (int)Duration.between(workshop.getShifts()[0].getStart(), task.getSubtask(index).getDuration().getEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()).toMinutes();
		// 	if(task.getDescription().equals(station.getId())&&activity.gettDebut().getValue()==tDebut&&activity.gettFin().getValue()==tFin) {
		// 		return activity.getId();
		// 	}
		// }
		
	// return "";
	}
	
	@Override
	public String generateRowLabel(CategoryDataset dataset, int row) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String generateColumnLabel(CategoryDataset dataset, int column) {
		// TODO Auto-generated method stub
		return null;
	}
}
