package controllers.LimitedDepthFirst;

import com.sun.org.apache.xml.internal.security.Init;
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

    private int maxDepth;

    private Random randomGenerator;

    private ArrayList<stateTuple> route;

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
        visited = new HashSet<stateTuple>();
        success = false;
    }

    private void Init(StateObservation stateObs) {
        route.clear();
        visited.clear();
        actionBuffer = null;
        currentScore = HeristicScore(stateObs);
        dfs_times = 0;
    }

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
        System.out.println("DFS times is: " + dfs_times);
        System.out.println();

        return action;
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

    private boolean LDFS(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int depth) {
        if (stateObs.getGameWinner() == Types.WINNER.PLAYER_WINS) {
            success = true;
            return true;
        }
        if (elapsedTimer.remainingTimeMillis() < TIME_THRES) return true;
        if (depth > maxDepth) {
            if (HeristicScore(stateObs) < currentScore && route.get(0).actionIn != actionBuffer) actionBuffer = route.get(0).actionIn;
            return false;
        }

        dfs_times ++;

        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        java.util.Collections.shuffle(actions);

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
