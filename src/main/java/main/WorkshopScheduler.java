package main;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import data.*;

public class WorkshopScheduler {
    public static void main(String[] args) throws Exception {
        long time = System.currentTimeMillis();
        Workshop workshop = Utils.fromFile("data/workshop_a.json", Workshop.class);
        assert workshop != null;
        System.out.println(workshop);
        SwingUtilities.invokeLater(() -> {  
     	   GanttChart workersChart = new GanttChart("Workers Schedule",workshop,"worker");  
     	           workersChart.setSize(800, 400);  
     	           workersChart.setLocationRelativeTo(null);  
     	           workersChart.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);  
     	           workersChart.setVisible(true);  
     	        });  
        SwingUtilities.invokeLater(() -> {  
      	   GanttChart stationsChart = new GanttChart("Stations Schedule",workshop,"station");  
      	           stationsChart.setSize(800, 400);  
      	           stationsChart.setLocationRelativeTo(null);  
      	           stationsChart.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);  
      	           stationsChart.setVisible(true);  
      	        }); 
    }
}
