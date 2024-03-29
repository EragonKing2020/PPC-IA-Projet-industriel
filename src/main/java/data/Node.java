package data;

import java.util.LinkedList;

import org.chocosolver.solver.variables.IntVar;

class Node{
	public LinkedList<IntVar> instancies;
	public LinkedList<IntVar> smallestLB;
	public int valLB;
	
	
	public Node(LinkedList<IntVar> instancies, LinkedList<IntVar> smallestLB) {
		super();
		this.instancies = instancies;
		this.smallestLB = smallestLB;
		this.valLB = smallestLB.get(0).getLB();
	}
}