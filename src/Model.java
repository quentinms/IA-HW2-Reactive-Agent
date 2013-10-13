import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
		}
	}

	private void generateActions() {
		for (State state : states) {
			for (City city : state.currentCity.neighbors()) {
				state.actions.add(city);
			}
			state.actions.add(state.packetDestination);
		}
	}

	public void computeReinforcementLearningAlgorithm() {

		HashMap<State, Double> V = new HashMap<State, Double>();

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
			for (State s : states) {
				for (City a : s.actions) {
					double sum = 0;
					for (State s_prime : states) {
						sum += V.get(s_prime) * taskDistribution.probability(a, s_prime.currentCity);
					}
					Q.get(s).put(a,
									(taskDistribution.reward(s.currentCity, a)
									- a.distanceTo(s.currentCity) * vehicle.costPerKm())
									+ discount * sum);
				}

				V.put(s, Collections.max(Q.get(s).values()));

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

			}
			i++;
		} while (i < 1000);

		System.out.println(V);

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
}

class Action {

}
