package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.Task;

public class Furniture {
    private final String id;

    private final Activity[] activities;

    private final Activity[][] precedence;

    private final Activity[][] sequences;
    
    private LinkedList<Task> tasks = new LinkedList<Task>();
    
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
        this.linkAct(this.precedence);
        this.linkAct(this.sequences);
    }
    
    private void linkAct(Activity[][] preseq) {
    	for (int i = 0; i < preseq.length; i ++) {
        	for (int j = 0; j < preseq[i].length; j ++) {
        		preseq[i][j] = this.getActivityByID(preseq[i][j].getId());
        	}
        }
    }
    
    private Activity getActivityByID(String id) {
    	for (Activity act : this.activities)
    		if (act.getId().equals(id))
    			return act;
    	throw new Error("ID activity not found in this furniture");
    }
    
    private boolean activityIsInSequence(Activity activity) {
    	for(Activity[] sequence : this.sequences) {
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
    	for(Activity[] sequence : this.sequences) {
    		tasks.add(model.taskVar(sequence[0].gettDebut(), model.intVar(0, 24*60), sequence[sequence.length-1].gettFin()));
    		for(int i = 0;i<sequence.length-1;i++) {
    			model.arithm(sequence[i].gettFin(), "<=", sequence[i+1].gettDebut()).post();
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
    
    public String solToString() {
    	return "Furniture{" + "id='" + id + "'" +
    			", activities=" + Arrays.toString(activities) +
                ", precedence=" + Arrays.deepToString(precedence) +
                ", sequences=" + Arrays.deepToString(sequences) +
                "}\n\r" + this.solActToString();
    }
    
    public String solActToString() {
    	String ligne1 = "-----|-----|-----|-----|-----|-----|-----|-----|";
    	Activity[] activitiesSorted = this.activities.clone();
    	Arrays.sort(activitiesSorted, Comparator.comparingInt(act -> act.gettDebut().getValue()));
    	int t = 0;
    	int i = 0;
    	String ligne2 = "";
    	for (Activity activity : activitiesSorted) {
    		int tDebut = activity.gettDebut().getValue() / 10;
    		while (t < tDebut) {
    			ligne2 += '-';
    			t ++;
    		}
			int tFin = activity.gettFin().getValue()/10;
			while (t < tFin) {
				ligne2 += i;
				t ++;
			}
			i++;
    	}
    	while (t < ligne1.length()) {
    		ligne2 += "-";
    		t ++;
    	}
    	System.out.println(Arrays.deepToString(activitiesSorted));
    	return ligne1 + "\n\r" + ligne2;
    }
}
