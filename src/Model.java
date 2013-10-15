import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class Model {

	private ArrayList<State> states;
	private Topology topology;
	private Vehicle vehicle;
	TaskDistribution taskDistribution;
	double discount;

	public Model(Topology topology, TaskDistribution taskDistribution, Vehicle vehicle, double discount) {
		this.topology = topology;
		this.taskDistribution = taskDistribution;
		this.vehicle = vehicle;
		this.discount = discount;
		states = new ArrayList<State>();

		generateStates();
		generateActions();
	}

	private void generateStates() {
		for (City currentCity : topology.cities()) {
			for (City packetDestination : topology.cities()) {
				if (!currentCity.equals(packetDestination)) {
					states.add(new State(currentCity, packetDestination));
				}
			}
			states.add(new State(currentCity, null));
		}
	}

	private void generateActions() {
		for (State state : states) {
			for (City city : state.currentCity.neighbors()) {
				state.actions.add(city);
			}
			
			if(!state.actions.contains(state.packetDestination) && state.packetDestination != null){
				state.actions.add(state.packetDestination);
			}
		}
	}

	public HashMap<State, City> computeReinforcementLearningAlgorithm() {

		HashMap<State, Double> V = new HashMap<State, Double>();
		HashMap<State, Double> V_old = new HashMap<State, Double>();
		HashMap<State, City> B = new HashMap<State, City>();

		for (State state : states) {
			V.put(state, 0.0);
		}

		HashMap<State, HashMap<City, Double>> Q = new HashMap<State, HashMap<City, Double>>();

		for (State state : states) {
			HashMap<City, Double> tmp = new HashMap<City, Double>();

			for (City action : state.actions) {
				tmp.put(action, 0.0);
			}

			Q.put(state, tmp);
		}

		int i = 0;
		do {
			V_old = new HashMap<State, Double>(V);
			for (State s : states) {
				System.out.println("For state: "+s + " #actions: "+s.actions.size());
				for (City a : s.actions) {
					System.out.println("For action: "+a);
					double sum = 0;
					double sum_proba = 0.0;
					for (City s_prime : topology.cities()) {
						if(!s_prime.equals(a)){
							System.out.println("from: "+a+" to: "+s_prime+" p="+taskDistribution.probability(a, s_prime));
							sum += V.get(new State(a, s_prime)) * taskDistribution.probability(a, s_prime);
							sum_proba += taskDistribution.probability(a, s_prime);
							System.out.println("***");
						}
						
					}
					System.out.println(sum_proba);
					sum += V.get(new State(a, null)) * (1-sum_proba);
					//If we pick up the task
					if(a.equals(s.packetDestination)){
						Q.get(s).put(a,
										taskDistribution.reward(s.currentCity, a)
										- a.distanceTo(s.currentCity) * vehicle.costPerKm()
										+ discount * sum);
					} else { // We do not pick up the task
						Q.get(s).put(a,
								- a.distanceTo(s.currentCity) * vehicle.costPerKm()
								+ discount * sum);
					}
					
					System.out.println("**** end city ****");
				}
				
				
				double max = 0.0;
				City max_index = null;
				for(City c: Q.get(s).keySet()){
					if(Q.get(s).get(c) > max){
						max = Q.get(s).get(c);
						max_index = c;
					}
				}
				
				V.put(s, max);
				B.put(s, max_index);
				
				
				if(V.get(s).equals(Double.NaN)){
					System.exit(0);
				}
				/*
				 *	repeat
				 *		for s ∈ S do
				 *			for a ∈ A do
				 *				Q(s, a) ← R(s, a) + γ
				 *			end for
				 *			V (S) ← maxa Q(s, a)
				 *		end for
				 *	until good enough
				 */
				System.out.println("********** end state *********");
			}
			i++;
			double diff = 0.0;
			Object[] v = V.values().toArray();
			Object[] v_old = V_old.values().toArray();
			 for (int j = 0; j < V.values().size(); j++) {
				 
				diff += Double.valueOf(v[j].toString()) - Double.valueOf(v_old[j].toString());
			}
			 
			 System.out.println(i+" : "+diff);
		} while (i < 100);
		
		return B;

	}

}

class State {
	protected City currentCity;
	protected City packetDestination; // either City or ∅
	protected ArrayList<City> actions;

	public State(City current, City packetDestination) {
		this.currentCity = current;
		this.packetDestination = packetDestination;
		actions = new ArrayList<Topology.City>();
	}
	
	@Override
	public boolean equals(Object obj) {
		State s = (State) obj;
		//return this.currentCity.equals(s.currentCity) && this.packetDestination.equals(s.packetDestination);
		//System.out.println(s);
		//System.out.println(this);
		return s.toString().equals(this.toString());
		
		
	}
	
	@Override
	public int hashCode() {
		//System.out.println("Java c'est de la merde");
		return this.toString().hashCode();
	}
	
	@Override
	public String toString() {
		return "("+this.currentCity+" -> "+this.packetDestination+")";
	}
}

/*class Action {

}*/
