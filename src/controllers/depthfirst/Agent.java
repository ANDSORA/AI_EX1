package controllers.depthfirst;

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

    /**
     * random generator
     */
    private Random randomGenerator;

    /**
     * when the search successfully finished, route would contain a winning path.
     */
    private ArrayList<stateTuple> route;

    /**
     * record the visited states
     */
    private HashSet<stateTuple> visited;

    /**
     * the flag to indicate whether we already have a winning route
     */
    private boolean success;

    /**
     * if remaining time < TIME_THRES, A* search have to be interrupted
     */
    private int TIME_THRES = 20;

    /**
     * record how many states DFS have explored
     */
    private int dfs_times = 0;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        randomGenerator = new Random();
        route = new ArrayList<stateTuple>();
        visited = new HashSet<stateTuple>();
        success = false;
    }

    /**
     * Init function for a fresh new DFS search
     * @param stateObs beginning state
     */
    private void Init(StateObservation stateObs) {
        route.clear();
        visited.clear();
        dfs_times = 0;
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        Types.ACTIONS action = null;

        if (success == false) {
            System.out.println("Don't have a winning route yet, will do a DFS search...");
            Init(stateObs);
            DFS(stateObs, elapsedTimer);
        }

        if (success == true) {
            System.out.println("We have got a winning route, let's follow it.");
            action = route.get(0).actionIn;
            route.remove(0);
        }

        if (action == null) System.out.println("We don't have action this time.");
        else System.out.println("The action of this step is: " + action.toString());
        System.out.println("DFS times is: " + dfs_times);
        System.out.println();

        return action;
    }

    /**
     * this function would conduct an DFS search
     * @param elapsedTimer
     * @return whether search is successful
     */
    private boolean DFS(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        // check if we have got to the goal
        if(stateObs.getGameWinner() == Types.WINNER.PLAYER_WINS) {
            success = true;
            return true;
        }
        // if time is up, interrupt the search
        if(elapsedTimer.remainingTimeMillis() < TIME_THRES) return true;

        // counter ++
        dfs_times ++;

        // get the action set and shuffle it
        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        java.util.Collections.shuffle(actions);

        // explore the children states
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
