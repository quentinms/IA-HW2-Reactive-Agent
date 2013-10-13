import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;


public class Model {
	
	private ArrayList<State> states;
	private Topology topo;
	private Vehicle v;
	TaskDistribution td;
	double discountFactor;
	
	public Model(Topology topo, TaskDistribution td, Vehicle v, double discountFactor){
		this.topo = topo;
		this.td = td;
		this.v = v;
		this.discountFactor = discountFactor;
		states = new ArrayList<State>();
		
		generateStates(topo);
		generateActions(topo);
	}
	
	private void generateStates(Topology topo){
		for (City origin : topo.cities()) {
			for (City dest : topo.cities()) {
				if (!origin.equals(dest)){
				states.add(new State(origin, dest));
				}
			}
		}
	}
	
	private void generateActions(Topology topo){
		for (State s : states) {
			for (City city : s.currentCity.neighbors()) {
				s.actions.add(city);
			}
			s.actions.add(s.packetDestination);
		}
	}
	
	public void computeReinforcementLearningAlgorithm(){
		
		
		
		HashMap <State, Double> V = new HashMap<State, Double>(); 
		
		for (State s : states) {
			V.put(s, 0.0);
		}
		
		HashMap <State, HashMap<City, Double> > Q = new HashMap<State, HashMap<City,Double>>();
		
		for (State s : states) {
			HashMap<City, Double> tmp = new HashMap<City, Double>();
			
			for(City a: s.actions){
				tmp.put(a, 0.0);
			}
			
			Q.put(s, tmp);
		}
		
		int i = 0;
		do{
			for (State s : states) {
				for (City a : s.actions) {
					double sum = 0;
					for (State s_prime : states) {
						sum+= V.get(s_prime) * td.probability(a, s_prime.currentCity);
					}
					Q.get(s).put(a, (td.reward(s.currentCity, a) - a.distanceTo(s.currentCity)*v.costPerKm()) + discountFactor  * sum);
				}
				
				V.put(s, Collections.max(Q.get(s).values()));
				
				/*
				 * repeat
				 *	for s ∈ S do
				 *			for a ∈ A do
				 *				Q(s,a)←R(s,a)+γ􏰐s′∈S T(s,a,s′)·V(s′)
				 *			end for
				 *			V (S) ← maxaQ(s, a) 
				 *		end for
				 *	until good enough
				 * 
				 * */
				
			}
			i++;
		}while( i < 1000);
		
		System.out.println(V);
		
	}
	
}

class State{
	protected City currentCity;
	protected City packetDestination; //either City or ∅
	protected ArrayList<City> actions;
	
	public State(City current, City packetDestination){
		this.currentCity = current;
		this.packetDestination = packetDestination;
		actions = new ArrayList<Topology.City>();
	}
}

class Action{
	
}
