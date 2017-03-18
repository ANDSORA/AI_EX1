package controllers.mytools;

import core.game.StateObservation;
import ontology.Types;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by andsora on 3/8/17.
 */
public class stateTuple {
    public StateObservation state;
    public StateObservation parentState;
    public Types.ACTIONS actionIn;
    public double Ascore;
    public double Hscore;

    public stateTuple(StateObservation s) {
        state = s; parentState = null; actionIn = null; Ascore = 0.0; Hscore = 0.0;
    }
    public stateTuple(StateObservation s, StateObservation ps) {
        state = s; parentState = ps; actionIn = null; Ascore = 0.0; Hscore = 0.0;
    }
    public stateTuple(StateObservation s, Types.ACTIONS a) {
        state = s; parentState = null; actionIn = a; Ascore = 0.0; Hscore = 0.0;
    }
    public stateTuple(StateObservation s, StateObservation ps, Types.ACTIONS a) {
        state = s; parentState = ps; actionIn = a; Ascore = 0.0; Hscore = 0.0;
    }
    public stateTuple(StateObservation s, double as, double hs) {
        state = s; parentState = null; actionIn = null; Ascore = as; Hscore = hs;
    }
    public stateTuple(StateObservation s, StateObservation ps, Types.ACTIONS a, double as, double hs) {
        state = s; parentState = ps; actionIn = a; Ascore = as; Hscore = hs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o.getClass() == stateTuple.class) {
            stateTuple stuple = (stateTuple) o;
            return state.equalPosition(stuple.state);
        }
        return false;
    }

    @Override
    public int hashCode() {
        //return stateObs.getAvatarType() + 10 * (int)stateObs.getGameScore() + 100 * ((int)stateObs.getAvatarPosition().x + 5000) + 10000 * ((int)stateObs.getAvatarPosition().y + 5000);
        return 1 + (int)state.getGameScore() + 100 * (int)state.getAvatarPosition().x + 10000 * (int)state.getAvatarPosition().y;
        //return 1;
    }

    public static void main(String[] args) {
        Comparator<stateTuple> comp = new StateTupleComparator();
        PriorityQueue<stateTuple> queue = new PriorityQueue<stateTuple>(10, comp);
        queue.add(new stateTuple(null, 0.0, 1.0));
        queue.add(new stateTuple(null, 0.0, 3.0));
        queue.add(new stateTuple(null, 0.0, 0.0));
        while (queue.size() != 0) {
            System.out.println(queue.remove().actionIn.toString());
        }
    }
}

