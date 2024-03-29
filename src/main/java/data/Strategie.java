package data;

import java.util.HashMap;
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
	LinkedList<Node> varsVarsSmallestLB = new LinkedList<Node>();
	LinkedList<IntVar> ordreInstanciation = new LinkedList<IntVar>();
	LinkedList<Integer> valInstanciation = new LinkedList<Integer>();
	
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
	
	private boolean sameOrderValueUpToL1(LinkedList<IntVar> l1, LinkedList<IntVar> l2) {
		if (l1.size() > l2.size()) return false;
		for (int i = 0; i < l1.size(); i ++) {
			if (l1.get(i) != l2.get(i))
				return false;
		}
		return true;
	}
	
	private boolean sameOrderValue(LinkedList<IntVar> l1, LinkedList<IntVar> l2) {
		if (l1.size() != l2.size()) return false;
		for (int i = 0; i < l1.size(); i ++) {
			if (l1.get(i) != l2.get(i))
				return false;
		}
		return true;
	}
	
	private boolean contains(LinkedList<IntVar> l1, LinkedList<IntVar> l2) {
		if (l1.size() > l2.size()) return false;
		for (IntVar var : l1)
			if (!l2.contains(var))
				return false;
		return true;
	}
	
	private LinkedList<Node> getNodesBefore(){
		LinkedList<Node> nodes = new LinkedList<Node>();
		for (Node node : varsVarsSmallestLB)
			if (this.sameOrderValueUpToL1(node.instancies, ordreInstanciation))
				nodes.add(node);
		return nodes;
	}
	
	private IntVar[] getUninstanciatedWithSmallestLB() {
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
			return new IntVar[] {null, null};
		
		boolean boolAdd = true;
		LinkedList<Node> nodesBefore = getNodesBefore();
		for (int iNode = 0; iNode < nodesBefore.size(); iNode ++) {
			if (this.contains(varsSmallestLB, nodesBefore.get(iNode).smallestLB)) {
				if (varsSmallestLB.get(0).getLB() > nodesBefore.get(iNode).valLB) {
					return new IntVar[] {varsSmallestLB.get(0), null};
				}
				boolAdd = false;
			}
		}
		
		IntVar varMaxApres = varsSmallestLB.get(0);
		int maxApres = this.getTempsApres(varMaxApres);
		for (IntVar var : varsSmallestLB) {
			int tempsApres = this.getTempsApres(var);
			if (tempsApres > maxApres) {
				maxApres = tempsApres;
				varMaxApres = var;
			}
		}
		
		if (boolAdd)
			addNode(new Node(ordreInstanciation, varsSmallestLB));
		
		return new IntVar[] {varMaxApres, varMaxApres};
	}
	
	private void addNode(Node node) {
		int i = 0;
		while (i < varsVarsSmallestLB.size() && this.sameOrderValueUpToL1(varsVarsSmallestLB.get(i).instancies, node.instancies))
			i ++;
		if (i > 0 && this.sameOrderValue(varsVarsSmallestLB.get(i - 1).instancies, node.instancies)) {
			varsVarsSmallestLB.set(i - 1, node);
		}
		else {
			varsVarsSmallestLB.add(node);
		}
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
		
		int i0 = ordreInstanciation.size() - 1;
		int i = ordreInstanciation.size() - 1;
		while (i >= 0 && ordreInstanciation.get(i).isInstantiatedTo(valInstanciation.get(i))){
			ordreInstanciation.removeLast();
			valInstanciation.removeLast();
			i --;
		}
		
		boolean inverseOp = false;
		int valNext = 0;
		if (prevDecision == null || prevDecision.getName().substring(0, 7).equals("Station")) {
			IntVar[] res = this.getUninstanciatedWithSmallestLB();
			prevDecision = res[0];
			if (prevDecision == null) {
				System.out.println(appels);
				return null;
			}
			if (res[1] == null) inverseOp = true;
			next = prevDecision;
			valNext = prevDecision.getLB();
			ordreInstanciation.add(next);
			valInstanciation.add(valNext);
		}
		else if (prevDecision.getName().substring(0, 6).equals("Worker")) {
			prevDecision = this.getStationAct(prevDecision.getName().substring(7));
			next = prevDecision;			
			valNext = prevDecision.getLB();
		}
		else {
			int idNumAct = Integer.parseInt(prevDecision.getName().substring(8));
			Activity activity = workshop.getActivityFromIdNum(idNumAct);
			for (int w : activity.getPossibleWorkers())
				for (IntVar varBreak : activity.getBreaksWorker(w))
					if (!varBreak.isInstantiated()) {
						next = varBreak;
						valNext = varBreak.getLB();
					}
			if (next == null) {
				prevDecision = this.getWorkerAct(prevDecision.getName().substring(7));
				next = prevDecision;
				valNext = prevDecision.getLB();
			}
		}
		if (appels % 10000 == 0) System.out.println(appels);
		appels ++;
		if (!inverseOp)
			d.set(next,valNext, DecisionOperatorFactory.makeIntEq());
		else {
			//System.out.println(appels + " : inegalite");
			d.set(next, valNext, DecisionOperatorFactory.makeIntNeq());
			d.setRefutable(false);
		}
	    return d;
    }
	
}