package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.LinkedList;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.Task;

public class Furniture {
    private final String id;

    private final Activity[] activities;

    private final Activity[][] precedence;

    private final Activity[][] sequences;
    
    private LinkedList<Task> tasks;
    
    @JsonCreator
    public Furniture(
            @JsonProperty("id") String id,
            @JsonProperty("activities") Activity[] activities,
            @JsonProperty("precedence") Activity[][] precedence,
            @JsonProperty("sequences") Activity[][] sequences
    ) {
        this.id = id;
        this.activities = activities;
        this.precedence = precedence;
        this.sequences = sequences;
    }
    
    private boolean activityIsInSequence(Activity activity) {
    	for(Activity[] sequence : sequences) {
    		for(Activity act : sequence) {
    			if(act.getId().equals(activity.getId())) {
        			return true;
        		}
    		}
    	}
    	return false;
    }
    
    public void addTask(Activity activity) {
    	if(!activityIsInSequence(activity)) {
    		tasks.add(activity.getTask());
    	}
    }
    
    public void createSequenceTasks(Model model) {
    	for(Activity[] sequence : sequences) {
    		tasks.add(model.taskVar(sequence[0].gettDebut(), null, sequence[sequence.length-1].gettFin()));
    		for(int i = 0;i<sequence.length-1;i++) {
    			model.arithm(sequence[i].gettFin(), "<=", sequence[i+1].gettDebut());
    		}
    	}
    }
    
    public String getId() {
        return id;
    }

    public Activity[] getActivities() {
        return activities;
    }

    public LinkedList<Task> getTasks(Model model) {
        for(Activity activity : activities) {
        	addTask(activity);
        }
        createSequenceTasks(model);
        return tasks;
    }
    
    public Activity[] getActivitiesType(ActivityType type) {
    	LinkedList<Activity> activities = new LinkedList<Activity>();
    	for (Activity activity : this.getActivities()) {
    		if (activity.getType() == type) {
    			activities.add(activity);
    		}
    	}
    	return (Activity[]) activities.toArray();
    }

    public Activity[][] getPrecedence() {
        return precedence;
    }

    public Activity[][] getSequences() {
        return sequences;
    }

    @JsonIgnore
    public int getTotalDuration() {
        return Arrays.stream(activities).mapToInt(Activity::getDuration).sum();
    }

    @Override
    public String toString() {
        return "Furniture{" +
                "id='" + id + '\'' +
                ", activities=" + Arrays.toString(activities) +
                ", precedence=" + Arrays.deepToString(precedence) +
                ", sequences=" + Arrays.deepToString(sequences) +
                '}';
    }
}
