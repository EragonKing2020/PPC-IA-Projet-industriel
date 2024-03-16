package main;

import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.JFrame;   
import org.jfree.chart.ChartFactory;  
import org.jfree.chart.ChartPanel;  
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.category.IntervalCategoryDataset;  
import org.jfree.data.gantt.Task;  
import org.jfree.data.gantt.TaskSeries;  
import org.jfree.data.gantt.TaskSeriesCollection;

import data.Activity;
import data.Furniture;
import data.Station;
import data.Worker;
import data.Workshop;

public class GanttChart extends JFrame {  
   private static final long serialVersionUID = 1L;
   private final Workshop workshop;
  
   public GanttChart(String title, Workshop workshop, String scheduleType) {  
      super(title);
      this.workshop = workshop;
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      if(scheduleType.equals("worker")) {
    	  setLayout(new GridLayout(workshop.getWorkers().length, 1));
    	  for(Worker worker : workshop.getWorkers()) {
    		// Create dataset  
              IntervalCategoryDataset dataset = getWorkerDataset(worker);  
        	  // Create chart  
              JFreeChart chart = ChartFactory.createGanttChart(  
                    "", // Chart title 
                    "", // X-Axis Label  
                    "Timeline", // Y-Axis Label  
                    dataset);
              CategoryPlot plot = (CategoryPlot) chart.getPlot();
              DateAxis rangeAxis = (DateAxis) plot.getRangeAxis();
              GanttRenderer renderer = (GanttRenderer) plot.getRenderer();
              // Définir les limites de l'axe des abscisses pour chaque diagramme de Gantt
              rangeAxis.setMinimumDate(Date.from(workshop.getShifts()[0].getStart().atZone(ZoneId.systemDefault()).toInstant()));
              rangeAxis.setMaximumDate(Date.from(workshop.getShifts()[workshop.getShifts().length-1].getEnd().atZone(ZoneId.systemDefault()).toInstant()));
              // Définir l'épaisseur des tâches
              renderer.setItemMargin(-0.5);
              add(new ChartPanel(chart));
    	  }
      }
      if(scheduleType.equals("station")) {
    	  setLayout(new GridLayout(workshop.getStations().length, 1));
    	  for(Station station : workshop.getStations()) {
      		// Create dataset  
                IntervalCategoryDataset dataset = getStationDataset(station);  
          	  // Create chart  
                JFreeChart chart = ChartFactory.createGanttChart(  
                      "", // Chart title 
                      "", // X-Axis Label  
                      "Timeline", // Y-Axis Label  
                      dataset);
                CategoryPlot plot = (CategoryPlot) chart.getPlot();
                DateAxis rangeAxis = (DateAxis) plot.getRangeAxis();
                // Ajustement de la largeur des barres des tâches
                GanttRenderer renderer = (GanttRenderer) plot.getRenderer();
                // Définir les limites de l'axe des abscisses pour chaque diagramme de Gantt
                rangeAxis.setMinimumDate(Date.from(workshop.getShifts()[0].getStart().atZone(ZoneId.systemDefault()).toInstant()));
                rangeAxis.setMaximumDate(Date.from(workshop.getShifts()[workshop.getShifts().length-1].getEnd().atZone(ZoneId.systemDefault()).toInstant()));
                // Définir l'épaisseur des tâches
                renderer.setItemMargin(-0.5);
                add(new ChartPanel(chart));
      	  }
      }
   }

	private IntervalCategoryDataset getWorkerDataset(Worker worker) {
		LinkedList<TaskSeries> taskseries = new LinkedList<TaskSeries>();
		for(Furniture furniture : workshop.getFurnitures()) {
			   taskseries.add(new TaskSeries(furniture.getId()));
		   }
		for(int i = 0;  i<workshop.getFurnitures().length;i++) {
		   for(Activity activity : workshop.getFurnitures()[i].getActivities()) {
			   if(activity.getWorker().getValue()==worker.getNumberId()) {
				   LocalDateTime startLocalDate = LocalDateTime.from(workshop.getShifts()[0].getStart()).plusMinutes((long)activity.gettDebut().getValue());
				   Date startDate = Date.from(startLocalDate.atZone(ZoneId.systemDefault()).toInstant());
				   LocalDateTime endLocalDate = LocalDateTime.from(workshop.getShifts()[0].getStart()).plusMinutes((long)activity.gettFin().getValue());
				   Date endDate = Date.from(endLocalDate.atZone(ZoneId.systemDefault()).toInstant());
				   taskseries.get(i).add(new Task(
						   worker.getId(),
						   startDate,
						   endDate
						   )
						   );
			   }
		   }
		}
		TaskSeriesCollection dataset = new TaskSeriesCollection();  
	    for(TaskSeries tasks : taskseries) {
	    	dataset.add(tasks);
	    }
		return dataset;
	}
	
	private IntervalCategoryDataset getStationDataset(Station station) {
		LinkedList<TaskSeries> taskseries = new LinkedList<TaskSeries>();
		for(Furniture furniture : workshop.getFurnitures()) {
			   taskseries.add(new TaskSeries(furniture.getId()));
		   }
		for(int i = 0;  i<workshop.getFurnitures().length;i++) {
			for(Activity activity : workshop.getFurnitures()[i].getActivities()) {
				if(activity.getStation().getValue()==station.getNumberId()) {
					LocalDateTime startLocalDate = LocalDateTime.from(workshop.getShifts()[0].getStart().plusMinutes((long)activity.gettDebut().getValue()));
				    Date startDate = Date.from(startLocalDate.atZone(ZoneId.systemDefault()).toInstant());
				    LocalDateTime endLocalDate = LocalDateTime.from(workshop.getShifts()[0].getStart().plusMinutes((long)activity.gettFin().getValue()));
				    Date endDate = Date.from(endLocalDate.atZone(ZoneId.systemDefault()).toInstant());
				    taskseries.get(i).add(new Task(
						station.getId(),
						startDate,
						endDate
				   )
				   );
			   }
		   }
	   }
	   TaskSeriesCollection dataset = new TaskSeriesCollection();  
	   for(TaskSeries tasks : taskseries) {
		   dataset.add(tasks);
	   }
	   return dataset;
	}
}


