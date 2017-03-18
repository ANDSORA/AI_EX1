package controllers.limitdepthfirst;

import controllers.mytools.stateTuple;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

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
     * max depth to search
     */
    private int maxDepth;

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
     * the Hscore of current actionBuffer
     */
    private double currentMinScore;

    /**
     * the greedy choice of action for current state
     */
    private Types.ACTIONS actionBuffer;

    /**
     * if remaining time < TIME_THRES, A* search have to be interrupted
     */
    private int TIME_THRES = 15;

    /**
     * record how many states LDFS have explored
     */
    private int ldfs_times = 0;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        maxDepth = 100;
        randomGenerator = new Random();
        route = new ArrayList<stateTuple>();
        visited = new HashSet<stateTuple>();
        success = false;
    }

    /**
     * Init function for a fresh new LDFS search
     * @param stateObs beginning state
     */
    private void Init(StateObservation stateObs) {
        route.clear();
        visited.clear();
        actionBuffer = null;
        currentMinScore = HeristicScore(stateObs);
        ldfs_times = 0;
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        Types.ACTIONS action;

        if (success == false) {
            System.out.println("Don't have a winning route yet, will do a LDFS search...");
            Init(stateObs);
            LDFS(stateObs, elapsedTimer, 0);
        }

        if (success == false) {
            System.out.println("...search ended, didn't find a winning route, will take a greedy step.");
            action = actionBuffer;
        }
        else {
            System.out.println("We have got a winning route, let's follow it.");
            action = route.get(0).actionIn;
            route.remove(0);
        }

        if (action == null) System.out.println("We don't have action this time.");
        else System.out.println("The action of this step is: " + action.toString());
        System.out.println("LDFS times is: " + ldfs_times);
        System.out.println();

        return action;
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
     * this function would conduct an LDFS search
     * @param elapsedTimer
     * @return whether search is successful
     */
    private boolean LDFS(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int depth) {
        // check if we have got to the goal
        if (stateObs.getGameWinner() == Types.WINNER.PLAYER_WINS) {
            success = true;
            return true;
        }
        // if time is up, interrupt the search
        if (elapsedTimer.remainingTimeMillis() < TIME_THRES) return true;
        // if maxDepth is reached, evaluate the state and update actionBuffer
        if (depth > maxDepth) {
            if (HeristicScore(stateObs) < currentMinScore) {
                currentMinScore = HeristicScore(stateObs);
                actionBuffer = route.get(0).actionIn;
            }
            return false;
        }

        // counter ++
        ldfs_times ++;

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
                if (LDFS(st, elapsedTimer, depth + 1)) return true;
                route.remove(route.size() - 1);
            }
        }

        return false;
    }
}
