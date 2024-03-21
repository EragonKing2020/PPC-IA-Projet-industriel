package main;

import java.awt.GridLayout;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.JFrame;   
import org.jfree.chart.ChartFactory;  
import org.jfree.chart.ChartPanel;  
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.CategoryDataset;
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
			  TaskSeriesCollection dataset = getStationDataset(station);  
          	  // Create chart  
                JFreeChart chart = ChartFactory.createGanttChart(  
                      "", // Chart title 
                      "", // X-Axis Label  
                      "Timeline", // Y-Axis Label  
                      dataset);
                CategoryPlot plot = (CategoryPlot) chart.getPlot();
                DateAxis rangeAxis = (DateAxis) plot.getRangeAxis();
                // Ajustement de la largeur des barres des tâches
                MyGanttRenderer renderer = new MyGanttRenderer(workshop,station,null, dataset);
                plot.setRenderer(renderer);
                // Définir les limites de l'axe des abscisses pour chaque diagramme de Gantt
                rangeAxis.setMinimumDate(Date.from(workshop.getShifts()[0].getStart().atZone(ZoneId.systemDefault()).toInstant()));
                rangeAxis.setMaximumDate(Date.from(workshop.getShifts()[workshop.getShifts().length-1].getEnd().atZone(ZoneId.systemDefault()).toInstant()));
                // Définir l'épaisseur des tâches
                renderer.setItemMargin(-0.5);

				// renderer.setDefaultItemLabelGenerator(new MyCategoryItemLabelGenerator(ABORT, workshop, station, null));
                // renderer.setDefaultItemLabelGenerator(new CategoryItemLabelGenerator() {
                // 	public String generateLabel(CategoryDataset dataSet, int series, int categories) {
    			// 		/* your code to get the label */
                // 		Task task = ((TaskSeries) dataset.getRowKeys().get(series)).get(categories);
				// 		System.out.println("\n==============================\n");
				// 		System.out.println(task.getSubtaskCount());
				// 		System.out.println("\n==============================\n");
                // 		for(int i = 0;i<task.getSubtaskCount();i++) {
                // 			for(Activity activity : workshop.getActivities()) {
                // 				int tDebut = (int)Duration.between(workshop.getShifts()[0].getStart(), task.getSubtask(i).getDuration().getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()).toMinutes();
                // 				int tFin = (int)Duration.between(workshop.getShifts()[0].getStart(), task.getSubtask(i).getDuration().getEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()).toMinutes();
                // 				if(task.getDescription().equals(station.getId())&&activity.gettDebut().getValue()==tDebut&&activity.gettFin().getValue()==tFin) {
                // 					System.out.println("\n-----------------------\n");
                // 					System.out.println("activity.getId() : "+activity.getId()+"\n");
                // 					System.out.println("-----------------------\n");

				// 					return activity.getId();
                // 				}
                // 			}
                // 		}
    			// 		return "";
    		    //     }

    		    //     public String generateColumnLabel(CategoryDataset dataset, int categories) {
    		    //         return dataset.getColumnKey(categories).toString();
    		    //     }

    		    //     public String generateRowLabel(CategoryDataset dataset, int series) {
    		    //         return dataset.getRowKey(series).toString();
    		    //     }

                // });

                renderer.setDefaultItemLabelsVisible(true);
                renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE9, TextAnchor.CENTER_LEFT));
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
				   if(taskseries.get(i).get(worker.getId())==null) {
						taskseries.get(i).add(new Task(
								worker.getId(),
								Date.from(LocalDateTime.from(workshop.getShifts()[0].getStart()).atZone(ZoneId.systemDefault()).toInstant()),
								Date.from(LocalDateTime.from(workshop.getShifts()[workshop.getShifts().length-1].getEnd()).atZone(ZoneId.systemDefault()).toInstant())
						   )
						   );
						taskseries.get(i).get(worker.getId()).addSubtask(new Task(
								worker.getId(),
								startDate,
								endDate
						   )
						);
						
					}
					else {
						taskseries.get(i).get(worker.getId()).addSubtask(new Task(
										worker.getId(),
										startDate,
										endDate
								   )
								);
					}
			   }
		   }
		}
		TaskSeriesCollection dataset = new TaskSeriesCollection();  
	    for(TaskSeries tasks : taskseries) {
	    	dataset.add(tasks);
	    }
		return dataset;
	}
	
	private TaskSeriesCollection getStationDataset(Station station) {
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
					if(taskseries.get(i).get(station.getId())==null) {
						taskseries.get(i).add(new Task(
								station.getId(),
								Date.from(LocalDateTime.from(workshop.getShifts()[0].getStart()).atZone(ZoneId.systemDefault()).toInstant()),
								Date.from(LocalDateTime.from(workshop.getShifts()[workshop.getShifts().length-1].getEnd()).atZone(ZoneId.systemDefault()).toInstant())
						   )
						   );
						taskseries.get(i).get(station.getId()).addSubtask(new Task(
								activity.getId(),
								startDate,
								endDate
						   )
						);
						
					}
					else {
						taskseries.get(i).get(station.getId()).addSubtask(new Task(
										activity.getId(),
										startDate,
										endDate
								   )
								);
					}
			   }
		   }
	   }
	   TaskSeriesCollection dataset = new TaskSeriesCollection();  
	   for(TaskSeries tasks : taskseries) {
			// System.out.println(tasks.get(station.getId()).getSubtaskCount());
		   dataset.add(tasks);
	   }
	   return dataset;
	}
}


