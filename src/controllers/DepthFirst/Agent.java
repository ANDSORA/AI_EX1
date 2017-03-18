package controllers.DepthFirst;

import controllers.mytools.stateTuple;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by andsora on 3/7/17.
 */
public class Agent extends AbstractPlayer {

    private Random randomGenerator;

    private ArrayList<stateTuple> route;

    private HashSet<stateTuple> visited;

    private boolean success;

    private int TIME_THRES = 20;

    public Agent(StateObservation so, ElapsedCpuTimer elapsedCpuTimer) {
        randomGenerator = new Random();
        route = new ArrayList<stateTuple>();
        visited = new HashSet<stateTuple>();
        success = false;
    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        Types.ACTIONS action = null;

        if (success == false) {
            System.out.println("Don't have a winning route yet, will do a DFS search...");
            DFS(stateObs, elapsedTimer);
        }

        if (success == true) {
            System.out.println("We have got a winning route, let's follow it.");
            action = route.get(0).actionIn;
            route.remove(0);
        }

        if (action == null) System.out.println("We don't have action this time.");
        else System.out.println("The action of this step is: " + action.toString());
        System.out.println();

        return action;
    }

    private boolean DFS(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if(stateObs.getGameWinner() == Types.WINNER.PLAYER_WINS) {
            success = true;
            return true;
        }
        if(elapsedTimer.remainingTimeMillis() < TIME_THRES) return true;

        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        java.util.Collections.shuffle(actions);

        for (Types.ACTIONS a: actions) {
            StateObservation st = stateObs.copy();
            st.advance(a);
            if (st.getGameWinner() != Types.WINNER.PLAYER_LOSES && !visited.contains(new stateTuple(st))) {
                visited.add(new stateTuple(st));
                route.add(new stateTuple(st, a));
                if (DFS(st, elapsedTimer)) return true;
                route.remove(route.size() - 1);
            }
        }

        return false;
    }
}
