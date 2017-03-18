package controllers.forTest;

import controllers.mytools.stateTuple;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;

/**
 * Created by andsora on 3/7/17.
 * just for test
 */
public class Agent extends AbstractPlayer {

    private int maxDepth;

    private Random randomGenerator;

    private ArrayList<stateTuple> route;

    //private ArrayList<StateObservation> visited;

    private HashSet<stateTuple> visited;

    private boolean success;

    private double currentScore;

    private Types.ACTIONS actionBuffer;

    private int TIME_THRES = 15;

    private int dfs_times = 0;

    public Agent(StateObservation so, ElapsedCpuTimer elapsedCpuTimer) {
        maxDepth = 100;
        randomGenerator = new Random();
        route = new ArrayList<stateTuple>();
        //visited = new ArrayList<StateObservation>();
        visited = new HashSet<stateTuple>();
        success = false;
    }

    private void Init(StateObservation stateObs) {
        route.clear();
        visited.clear();
        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        actionBuffer = actions.get(randomGenerator.nextInt(actions.size()));
        currentScore = stateObs.getGameScore();
        dfs_times = 0;
    }

    /*
    private double Heristic(StateObservation stateObs) {
        return 0.0;
    }
    */

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        Types.ACTIONS action = null;

        /* Here comes a test *
        System.out.println("Here comes a test");
        HashSet<StateObservation> HS = new HashSet<StateObservation>();
        HashSet<HashStateObservation> HHS = new HashSet<HashStateObservation>();
        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();

        StateObservation s0 = stateObs.copy(); System.out.println("hashCode of s0 = " + s0.hashCode());
        HashStateObservation hs0 = new HashStateObservation(s0); System.out.println("hashCode of hs0 = " + hs0.hashCode());
        StateObservation s1 = s0.copy(); s1.advance(Types.ACTIONS.ACTION_DOWN); System.out.println("hashCode of s1 = " + s1.hashCode());
        HashStateObservation hs1 = new HashStateObservation(s1); System.out.println("hashCode of hs1 = " + hs1.hashCode());
        StateObservation s2 = s1.copy(); s2.advance(Types.ACTIONS.ACTION_UP); System.out.println("hashCode of s2 = " + s2.hashCode());
        HashStateObservation hs2 = new HashStateObservation(s2); System.out.println("hashCode of hs2 = " + hs2.hashCode());

        HS.add(s0); HS.add(s1); HS.add(s2);
        HHS.add(hs0); HHS.add(hs1); HHS.add(hs2);

        System.out.println("Is s2 equals s0? " + s2.equalPosition(s0));
        System.out.println("Is hs2 equals hs0? " + hs2.equals(hs0));

        System.out.println("The size of HS is:" + HS.size());
        System.out.println("The size of HHS is:" + HHS.size());
        System.out.println("Is HS contains s2? " + HS.contains(s2));
        System.out.println("Is HHS contains hs2? " + HHS.contains(hs2));
        /* end of the test */

        if (success == true) {
            System.out.println("Already have a winning route, following it.");
            action = route.get(0).actionIn;
            route.remove(0);
        }

        if (success == false) {
            System.out.println("Don't have a winning route yet, will do a search...");

            Init(stateObs);
            boolean DFS_flag = LDFS(stateObs, elapsedTimer, 0);

            System.out.println("Remaining time = " + elapsedTimer.remainingTimeMillis() + " ms.");
            if (DFS_flag == true && elapsedTimer.remainingTimeMillis() >= TIME_THRES) {
                System.out.println("Search completed, a winning route found.");
                action = route.get(0).actionIn;
                route.remove(0);
            }
            else {
                if (DFS_flag == false) System.out.println("Search completed, but no winning route found.");
                else System.out.println("Search not completed for timing out.");
                action = actionBuffer;
            }
        }

        Vector2d apos = stateObs.getAvatarPosition();
        //Vector2d keypos, goalpos;
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
        System.out.println("Let's print movingPositions, length == " + movingPositions.length);
        for (int i = 0; i < movingPositions.length; ++ i) {
            System.out.println("\tmoving["+i+"].size == " + movingPositions[i].size());
            if (movingPositions[i].size() > 0) {
                System.out.println("\t\titype = " + movingPositions[i].get(0).itype);
                System.out.println("\t\tposition = " + movingPositions[i].get(0).position.toString());
            }
        } System.out.println();
        //Vector2d keypos = movingPositions[0].get(0).position;
        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
        System.out.println("Let's print fixedPositions, length == " + fixedPositions.length);
        for (int i = 0; i < fixedPositions.length; ++ i) {
            System.out.println("\tfixed["+i+"].size == " + fixedPositions[i].size());
            if (fixedPositions[i].size() > 0) {
                System.out.println("\t\titype = " + fixedPositions[i].get(0).itype);
                System.out.println("\t\tposition = " + fixedPositions[i].get(0).position.toString());
            }
        } System.out.println();
        //Vector2d goalpos = fixedPositions[7].get(0).position;
        System.out.println("The winner of game is: " + stateObs.getGameWinner().toString());
        System.out.println("The pos of Avatar is: " + apos.x + ", " + apos.y);
        //System.out.println("The pos of key is: " + keypos.x + ", " + keypos.y);
        //System.out.println("The pos of goal is: " + goalpos.x + ", " + goalpos.y);
        System.out.println("The action of this step is: " + action.toString());
        System.out.println("DFS times is: " + dfs_times);
        System.out.println();

        return action;
    }

    private boolean LDFS(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int depth) {
        if (stateObs.getGameWinner() == Types.WINNER.PLAYER_WINS) {
            success = true;
            return true;
        }
        if (elapsedTimer.remainingTimeMillis() < TIME_THRES) return true;
        if (depth > maxDepth) {
            if (stateObs.getGameScore() >= currentScore && route.get(0).actionIn != actionBuffer) actionBuffer = route.get(0).actionIn;
            return false;
        }

        dfs_times ++;

        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        java.util.Collections.shuffle(actions);

        for (int i = 0; i < actions.size(); ++ i) {
            Types.ACTIONS a = actions.get(i);
            StateObservation st = stateObs.copy();
            st.advance(a);
            //if (isStateUnvisited(st)) {
            if (!visited.contains(new stateTuple(st))) {
                //visited.add(st);
                visited.add(new stateTuple(st));
                route.add(new stateTuple(st, a));
                if (LDFS(st, elapsedTimer, depth + 1)) return true;
                route.remove(route.size() - 1);
            }
        }

        return false;
    }

    /*
    private boolean isStateUnvisited(StateObservation stateObs) {
        for (int i = 0; i < visited.size(); ++ i) {
            if (visited.get(i).equalPosition((stateObs))) return false;
        }
        return true;
    } */
}
