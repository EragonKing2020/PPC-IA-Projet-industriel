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
	private IntVar prevDecision = null;
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
	
	private IntVar getWorkerAct(String act) {
		for (IntVar var : vars) {
			String name = var.getName();
			if (name.substring(0, 6).equals("Worker") && name.substring(name.length() - act.length()).equals(act))
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
		if (prevDecision == null || prevDecision.getName().substring(0, 7).equals("Station")) {
			prevDecision = this.getUninstanciatedWithSmallestLB();
			if (prevDecision == null) {
				System.out.println(appels);
				return null;
			}
			next = prevDecision;
			valNext = prevDecision.getLB();
		}
		else if (prevDecision.getName().substring(0, 6).equals("Worker")) {
			prevDecision = this.getStationAct(prevDecision.getName().substring(7));
			next = prevDecision;			
			valNext = prevDecision.getLB();
		}
		else {
			int idNumAct = Integer.parseInt(prevDecision.getName().substring(8));
			Activity activity = workshop.getActivityFromIdNum(idNumAct);
//			for (int w : activity.getPossibleWorkers())
//				for (IntVar varBreak : activity.getBreaksWorker(w))
			next = activity.getDurationVar();
			while (valNext<=activity.getDurationVar().getUB()) {
				valNext = activity.getDurationVar().nextValue(valNext);
			}
			if (next == null||valNext==Integer.MAX_VALUE) {
				prevDecision = this.getWorkerAct(prevDecision.getName().substring(7));
				next = prevDecision;
				valNext = prevDecision.getLB();
			}
		}
		if (appels % 10000 == 0) System.out.println(appels);
		appels ++;
		d.set(next,valNext, DecisionOperatorFactory.makeIntEq());
	    return d;
    }
	
}