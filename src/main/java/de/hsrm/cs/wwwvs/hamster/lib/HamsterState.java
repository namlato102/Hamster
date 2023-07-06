package de.hsrm.cs.wwwvs.hamster.lib;

import java.io.Serializable;

public class HamsterState implements Serializable {
	/**
	 * generated serial id
	 */
	private static final long serialVersionUID = -237671866961291816L;
	private short treatsLeft;
	private int rounds;
	private short cost;
	
	public int getTreatsLeft() {
		return treatsLeft;
	}
	
	public void setTreatsLeft(short treatsLeft) {
		this.treatsLeft = treatsLeft;
	}
	
	public int getRounds() {
		return rounds;
	}
	
	public void setRounds(int rounds) {
		this.rounds = rounds;
	}
	
	public int getCost() {
		return cost;
	}
	
	public void setCost(short cost) {
		this.cost = cost;
	}
	
	@Override
	public String toString() {
		return treatsLeft + " treats left, " + rounds + " rounds, " + cost + " €";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HamsterState other = (HamsterState) obj;
		if (cost != other.getCost())
			return false;
		if (rounds != other.getRounds())
			return false;
		if (treatsLeft != other.getTreatsLeft())
			return false;
		return true;
	}
}
