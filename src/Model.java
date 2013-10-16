import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class Model {

	private ArrayList<State> states;
	private Topology topology;
	private Vehicle vehicle;
	private TaskDistribution taskDistribution;
	double discount;

	public Model(Topology topology, TaskDistribution taskDistribution, Vehicle vehicle, double discount) {
		this.topology = topology;
		this.taskDistribution = taskDistribution;
		this.vehicle = vehicle;
		this.discount = discount;
		this.states = new ArrayList<State>();

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
			
			if (!state.actions.contains(state.packetDestination) && state.packetDestination != null) {
				state.actions.add(state.packetDestination);
			}
		}
	}

	
	public HashMap<State, City> computeReinforcementLearningAlgorithm() {

		/*
		 * Initialisation
		 */
		HashMap<State, Double> V = new HashMap<State, Double>();
		HashMap<State, Double> oldV = new HashMap<State, Double>();
		HashMap<State, City> B = new HashMap<State, City>();

		for (State state : states) {
			V.put(state, 0.0);
			B.put(state, topology.randomCity(new Random()));
		}

		HashMap<State, HashMap<City, Double>> Q = new HashMap<State, HashMap<City, Double>>();

		for (State state : states) {
			
			HashMap<City, Double> tmp = new HashMap<City, Double>();

			for (City action : state.actions) {
				tmp.put(action, 0.0);
			}

			Q.put(state, tmp);
			
		}

		/*
		 * Algorithm
		 *
		 *	repeat
		 *		for s ∈ S do
		 *			for a ∈ A do
		 *				Q(s,a) ← R(s,a) + γ × Σ(s′∈S) T(s,a,s′)×V(s)
		 *			end for
		 *			V (S) ← max(a) Q(s,a)
		 *		end for
		 *	until good enough
		 *
		 * With our model, action a is the chosen next city to go,
		 * and T(s,a,s′) = p(a,s′), and R(s,a) = I×r(s,a) − cost(s,a),
		 * with I = 1 if the task is accepted, 0 otherwise.
		 * 
		 */
		
		// repeat
		do {
			
			oldV = new HashMap<State, Double>(V);
			
			// for s ∈ S do
			for (State s : states) {
				System.out.println("For state " + s + ", #actions: " + s.actions.size());
				
				// for a ∈ A do
				for (City a : s.actions) {
					System.out.println("For action " + a);
					double sum = 0.0;
					double sum_proba = 0.0;
					
					// Σ(s′∈S) T(s,a,s)×V(s)
					for (City s_prime : topology.cities()) {
						if (!s_prime.equals(a)) {
							System.out.println("p(" + a + ", " + s_prime + ") = " + taskDistribution.probability(a, s_prime));
							sum += V.get(new State(a, s_prime)) * taskDistribution.probability(a, s_prime);
							sum_proba += taskDistribution.probability(a, s_prime);
							System.out.println("***");
						}
						
					}
					
					System.out.println(sum_proba);
					
					// transition probability if no task will be availaible is 1 − Σ(i,j,i≠j) p(i,j)
					sum += V.get(new State(a, null)) * (1 - sum_proba);
					
					// Q(s,a) ← R(s,a) + γ × sum
					// If we pick up the task
					if (a.equals(s.packetDestination)) {
						Q.get(s).put(a, taskDistribution.reward(s.currentCity, a)
										- a.distanceTo(s.currentCity) * vehicle.costPerKm()
										+ discount * sum);
					
					// We do not pick up the task
					} else {
						Q.get(s).put(a, - a.distanceTo(s.currentCity) * vehicle.costPerKm()
										+ discount * sum);
					}
					
					System.out.println("**** end city ****");
				}
				
				
				// V (S) ← max(a) Q(s,a)
				double max = Double.NEGATIVE_INFINITY;
				City max_index = null;
				
				for (City c : Q.get(s).keySet()) {
					if (Q.get(s).get(c) > max) {
						max = Q.get(s).get(c);
						max_index = c;
					}
				}
				
				V.put(s, max);
				B.put(s, max_index);
				
				System.out.println("********** end state *********");
			}
		
		// until good enough
		} while (!goodEnough(V, oldV));
		
		return B;

	}
	
	
	/**
	 * Until good enough = until goodEnough().
	 */
	private boolean goodEnough(HashMap<State, Double> V, HashMap<State, Double> oldV) {
		
		double epsilon = 1E-5;
		double diff = 0.0;
		
		Double[] v = V.values().toArray(new Double[V.values().size()]);
		Double[] oldv = oldV.values().toArray(new Double[oldV.values().size()]);
		
		for (int i = 0; i < V.values().size(); i++) {
			diff += v[i] - oldv[i];
		}
		
		return diff <= epsilon;
		
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
		if (obj == null) return false;
		State s = (State) obj;
		if (s == this) return true;
		//return this.currentCity.equals(s.currentCity) && this.packetDestination.equals(s.packetDestination);
		return s.toString().equals(this.toString()); // Problem?
		
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public String toString() {
		return "(" + this.currentCity + " → " + this.packetDestination + ")";
	}
	
}
