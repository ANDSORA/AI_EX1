package controllers.ASTAR;

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

    private Random randomGenerator;

    private HashMap<stateTuple, stateTuple> graphmap;

    private ArrayList<Types.ACTIONS> route;

    private PriorityQueue<stateTuple> pqueue;

    private HashSet<stateTuple> visited;

    private boolean success;

    private boolean trying;

    private int TIME_THRES = 20;

    private int astar_times = 0;

    public Agent(StateObservation so, ElapsedCpuTimer elapsedCpuTimer) {
        randomGenerator = new Random();
        graphmap = new HashMap<stateTuple, stateTuple>();
        route = new ArrayList<Types.ACTIONS>();
        //visited = new HashSet<HashStateObservation>();
        pqueue = new PriorityQueue<stateTuple>(100, new StateTupleComparator());
        visited = new HashSet<stateTuple>();
        success = false;
        trying = false;
    }

    private void Init(StateObservation stateObs) {
        graphmap.clear();
        route.clear();
        //visited.clear();
        pqueue.clear();
        pqueue.add(new stateTuple(stateObs));
        visited.clear();
        trying = true;
        //currentScore = stateObs.getGameScore();
        astar_times = 0;
    }

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

            /*
            boolean ASTAR_flag = Astar(stateObs, elapsedTimer);

            System.out.println("Remaining time = " + elapsedTimer.remainingTimeMillis() + " ms.");
            if (ASTAR_flag == true && elapsedTimer.remainingTimeMillis() >= TIME_THRES) {
                System.out.println("Search completed, a winning route found.");
                action = route.get(route.size() - 1);
                route.remove(route.size() - 1);
            }
            else {
                if (ASTAR_flag == false) System.out.println("Search completed, but no winning route found.");
                else System.out.println("Search not completed for timing out.");
                ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
                action = actions.get(randomGenerator.nextInt(actions.size()));
            }*/
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

    private ArrayList<Types.ACTIONS> findRoute(StateObservation st) {
        ArrayList<Types.ACTIONS> actions = new ArrayList<Types.ACTIONS>();
        stateTuple stuple = graphmap.get(new stateTuple(st));
        while (stuple.actionIn != null) {
            actions.add(stuple.actionIn);
            stuple = graphmap.get(new stateTuple(stuple.parentState));
        }
        return actions;
    }

    private double HeristicScore(StateObservation stateObs) {
        double score = 0.0;
        int atype = stateObs.getAvatarType();
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();

        /* get positions */
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

        score += Math.abs(apos.x - keypos.x) + Math.abs(apos.y - keypos.y);
        score += Math.abs(keypos.x - goalpos.x) + Math.abs(keypos.y - goalpos.y);
        score -= stateObs.getGameScore() * 1000.0;

        return score;
    }

    private boolean Astar(ElapsedCpuTimer elapsedTimer) {

        while (!pqueue.isEmpty()) {
            stateTuple stuple = pqueue.remove();
            graphmap.put(new stateTuple(stuple.state), stuple);
            visited.add(new stateTuple(stuple.state));

            if (stuple.state.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                success = true;
                route = findRoute(stuple.state);
                return true;
            }

            astar_times ++;

            ArrayList<Types.ACTIONS> actions = stuple.state.getAvailableActions();
            java.util.Collections.shuffle(actions);

            for (Types.ACTIONS a : actions) {
                StateObservation st = stuple.state.copy();
                st.advance(a);
                if (st.getGameWinner() != Types.WINNER.PLAYER_LOSES && !visited.contains(new stateTuple(st))) {
                    pqueue.add(new stateTuple(st, stuple.state, a, stuple.Ascore + 50.0, HeristicScore(st)));
                }
            }

            System.out.println("\tSize of pqueue, visited, graphmap is: " + pqueue.size() + ", " + visited.size() + ", " + graphmap.size());

            if (elapsedTimer.remainingTimeMillis() < TIME_THRES) return false;
        }

        trying = false;
        return false;
    }
}
