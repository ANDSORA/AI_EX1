package controllers.Astar;

import controllers.mytools.stateTuple;
import controllers.mytools.StateTupleComparator;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Created by andsora on 3/7/17.
 */
public class Agent extends AbstractPlayer {

    /**
     * random generator
     */
    private Random randomGenerator;

    /**
     * record the directed edge between states, for the use of findRoute()
     */
    private HashMap<stateTuple, stateTuple> graphmap;

    /**
     * when the search successfully finished, route would contain a winning path.
     */
    private ArrayList<Types.ACTIONS> route;

    /**
     * priority queue, for the use of A*
     */
    private PriorityQueue<stateTuple> pqueue;

    /**
     * record the visited states, not necessarilly, but could reduce the size of pqueue
     */
    private HashSet<stateTuple> visited;

    /**
     * the flag to indicate whether we already have a winning route
     */
    private boolean success;

    /**
     * the flag to indicate whether we have an unfinished A* search
     */
    private boolean trying;

    /**
     * if remaining time < TIME_THRES, A* search have to be interrupted
     */
    private int TIME_THRES = 20;

    /**
     * record how many states A* have explored
     */
    private int astar_times = 0;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        randomGenerator = new Random();
        graphmap = new HashMap<stateTuple, stateTuple>();
        route = new ArrayList<Types.ACTIONS>();
        pqueue = new PriorityQueue<stateTuple>(100, new StateTupleComparator());
        visited = new HashSet<stateTuple>();
        success = false;
        trying = false;
    }

    /**
     * Init function for a fresh new A* search
     * @param stateObs beginning state
     */
    private void Init(StateObservation stateObs) {
        graphmap.clear();
        route.clear();
        pqueue.clear();
        pqueue.add(new stateTuple(stateObs));
        visited.clear();
        trying = true;
        astar_times = 0;
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
            System.out.println("Don't have a winning route yet, will do a A* search...");

            //Init(stateObs);
            if (trying == true) {
                System.out.println("...last search not finished, will continue the work.");
                Astar(elapsedTimer);
            }
            else {
                System.out.println("...will start a new search.");
                Init(stateObs);
                Astar(elapsedTimer);
            }
        }

        if (success == true) {
            System.out.println("We have a winning route, following it.");
            action = route.remove(route.size() - 1);
        }

        if (action == null) System.out.println("We don't have action this time.");
        else System.out.println("The action of this step is: " + action.toString());
        System.out.println("The number of explored states is: " + astar_times);
        System.out.println();

        return action;
    }

    /**
     * when A* searched is finished successfully, this function would be called to retrieve
     * the winning route
     * @param st a winning state
     * @return a winning route
     */
    private ArrayList<Types.ACTIONS> findRoute(StateObservation st) {
        ArrayList<Types.ACTIONS> actions = new ArrayList<Types.ACTIONS>();
        stateTuple stuple = graphmap.get(new stateTuple(st));
        while (stuple.actionIn != null) {
            actions.add(stuple.actionIn);
            stuple = graphmap.get(new stateTuple(stuple.parentState));
        }
        return actions;
    }

    /**
     * this function is used to compute a HeristicScore
     * @param stateObs
     * @return Hscore
     */
    private double HeristicScore(StateObservation stateObs) {
        double score = 0.0;
        int atype = stateObs.getAvatarType();
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();

        // get positions
        Vector2d apos = stateObs.getAvatarPosition();
        Vector2d keypos = apos, goalpos = apos;
        if (atype == 1) {
            for (ArrayList<Observation> movingPos: movingPositions) {
                if (movingPos.size() > 0 && movingPos.get(0).itype == 6) {
                    keypos = movingPos.get(0).position;
                    break;
                }
            }
        }
        for (ArrayList<Observation> fixedPos: fixedPositions) {
            if (fixedPos.size() > 0 && fixedPos.get(0).itype == 7) {
                goalpos = fixedPos.get(0).position;
                break;
            }
        }

        // calculate the score
        score += Math.abs(apos.x - keypos.x) + Math.abs(apos.y - keypos.y);
        score += Math.abs(keypos.x - goalpos.x) + Math.abs(keypos.y - goalpos.y);
        score -= stateObs.getGameScore() * 1000.0;

        return score;
    }

    /**
     * this function would conduct an A* search
     * @param elapsedTimer
     * @return whether search is successful
     */
    private boolean Astar(ElapsedCpuTimer elapsedTimer) {

        while (!pqueue.isEmpty()) {
            // if time is up, interrupt the search
            if (elapsedTimer.remainingTimeMillis() < TIME_THRES) return false;

            // get the state to explore, if it is visited, ignore it, otherwise update it.
            stateTuple stuple = pqueue.remove();
            if (visited.contains(stuple)) continue;
            graphmap.put(new stateTuple(stuple.state), stuple);
            visited.add(new stateTuple(stuple.state));

            // check if we have got to the goal
            if (stuple.state.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                success = true;
                route = findRoute(stuple.state);
                return true;
            }

            // counter ++
            astar_times ++;

            // get the action set and shuffle it
            ArrayList<Types.ACTIONS> actions = stuple.state.getAvailableActions();
            java.util.Collections.shuffle(actions);

            // add the children states to priorityQueue
            for (Types.ACTIONS a : actions) {
                StateObservation st = stuple.state.copy();
                st.advance(a);
                if (st.getGameWinner() != Types.WINNER.PLAYER_LOSES) {
                    pqueue.add(new stateTuple(st, stuple.state, a, stuple.Ascore + 50.0, HeristicScore(st)));
                }
            }

            // print the conditions
            System.out.println("\tSize of pqueue, visited, graphmap is: " + pqueue.size() + ", " + visited.size() + ", " + graphmap.size());
        }

        // all states are explored but no winning route found (nearly impossible)
        trying = false;
        return false;
    }
}
