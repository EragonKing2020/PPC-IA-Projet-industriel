package data;

import java.util.LinkedList;

import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.PoolManager;

class Strategie extends AbstractStrategy<IntVar>{
	private PoolManager<IntDecision> pool = new PoolManager();
	private int appels = 0;
	private LinkedList<IntVar> prevDecision = new LinkedList<IntVar>();
	private LinkedList<Integer> valDecision = new LinkedList<Integer>();
	private Workshop workshop;
	
	
	public Strategie(IntVar[] decisionVars,Workshop workshop) {
		super(decisionVars);
		this.workshop = workshop;
	}
	
	private int getTempsApres(IntVar var) {
		int idNumAct = Integer.parseInt(var.getName().substring(8));
		Furniture furniture = workshop.getFurnitureOfActivity(idNumAct);
		int tempsApres = 0;
		for (Activity activity : furniture.getActivities()) 
			if (!activity.getWorker().isInstantiated() && activity.getNumberId() != idNumAct)
				tempsApres += activity.getDuration();
		return tempsApres;
	}
	
	private IntVar getUninstanciatedWithSmallestLB() {
		LinkedList<IntVar> varsSmallestLB = new LinkedList<IntVar>();
		int smallestLB = -1000;
		for (int i = 0; i < vars.length; i ++) {
			if (vars[i].getName().substring(0, 6).equals("tDebut") && !vars[i].isInstantiated()) {
				if (smallestLB < 0 || vars[i].getLB() < smallestLB) {
					varsSmallestLB.clear();
					varsSmallestLB.add(vars[i]);
					smallestLB = vars[i].getLB();
				}
				else if (vars[i].getLB() == smallestLB) {
					varsSmallestLB.add(vars[i]);
				}
			}
		}
		if (varsSmallestLB.isEmpty())
			return null;
		
		IntVar varMaxApres = varsSmallestLB.get(0);
		int maxApres = this.getTempsApres(varMaxApres);
		for (IntVar var : varsSmallestLB) {
			int tempsApres = this.getTempsApres(var);
			if (tempsApres > maxApres) {
				maxApres = tempsApres;
				varMaxApres = var;
			}
		}
		return varMaxApres;
	}
	
	private IntVar getStationAct(String act) {
		for (IntVar var : vars) {
			String name = var.getName();
			if (name.substring(0, 7).equals("Station") && name.substring(name.length() - act.length()).equals(act))
				return var;
		}
		return null;
	}
	
	@Override
	public Decision getDecision() {
		IntDecision d = this.pool.getE();
		if(d==null) d = new IntDecision(this.pool);
		IntVar next = null;
		int valNext = 0;
		
		int i;
		for (i = prevDecision.size() - 1; i >= 0 && !prevDecision.getLast().isInstantiatedTo(valDecision.getLast()); i --) {
			prevDecision.removeLast();
			valDecision.removeLast();
		};
		
		
		if (prevDecision.isEmpty() || prevDecision.getLast().getName().substring(0, 7).equals("Station")) {
			next = this.getUninstanciatedWithSmallestLB();
			if (next == null) {
				System.out.println(appels);
				return null;
			}
			prevDecision.add(next);
			valNext = next.getLB();
			valDecision.add(valNext);
		}
		else if (prevDecision.getLast().getName().substring(0, 6).equals("Worker")) {
			int idNumAct = Integer.parseInt(prevDecision.getLast().getName().substring(8));
			Activity activity = workshop.getActivityFromIdNum(idNumAct);
			if (!activity.getDurationVar().isInstantiated()) {
				next = activity.getDurationVar();
				valNext = next.getLB();
				d.setRefutable(false); //Instancie avec la durée la plus courte, car on ne gagnera rien à prendre une durée plus longue
								//sur la tâche (plusieurs valeurs possibles de la durée quand la tâche se fini juste avant une pause,
								//et que le temps de la tâche plus la durée de la pause tombe dans la pause.
			}
			else {
				next = this.getStationAct(prevDecision.getLast().getName().substring(7));
				prevDecision.add(next);			
				valNext = next.getLB();
				valDecision.add(valNext);
			}
		}
		else {
			int idNumAct = Integer.parseInt(prevDecision.getLast().getName().substring(8));
			Activity activity = workshop.getActivityFromIdNum(idNumAct);
			next = activity.getWorker();
			prevDecision.add(next);
			valNext = next.getLB();
			valDecision.add(valNext);
		}
		if (appels % 10000 == 0) System.out.println(appels);
		appels ++;
		d.set(next,valNext, DecisionOperatorFactory.makeIntEq());
	    return d;
    }
	
}