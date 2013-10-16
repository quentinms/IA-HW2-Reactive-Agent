import java.util.HashMap;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveAgent implements ReactiveBehavior {

//	private Random random;
//	private double pPickup;
	private Model model;
	HashMap<State, City> B;

	@Override
	public void setup(Topology topology, TaskDistribution taskDistribution, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);

//		this.random = new Random();
//		this.pPickup = discount;
		model = new Model(topology, taskDistribution, agent.vehicles().get(0), discount);
		B = model.computeReinforcementLearningAlgorithm();
		
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		State s;
		if(availableTask != null){
			s = new State(vehicle.getCurrentCity(), availableTask.deliveryCity);
		} else {
			s = new State(vehicle.getCurrentCity(), null);
		}
		
		City dest = B.get(s);
		
		if(availableTask != null && dest.equals(availableTask.deliveryCity)){
			action = new Pickup(availableTask);
		}else{
			action = new Move(dest);
		}
		
		return action;
	}
}
