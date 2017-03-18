import java.lang.annotation.Repeatable;
import java.util.HashMap;
import java.util.Random;

import core.ArcadeMachine;
import core.competition.CompetitionParameters;

import java.util.ArrayList;
import java.util.Stack;


/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test
{

    public static void main(String[] args)
    {

        CompetitionParameters.ACTION_TIME = 100; // set to the time that allow you to do the depth first search
        ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl3.txt", true, "controllers.ASTAR.Agent", null, new Random().nextInt(), false);

        /*
        HashMap<String, keshan> hm = new HashMap<String, keshan>();
        hm.put("a", new keshan(1));
        hm.put("b", new keshan(2));

        if (hm.get("a") != null) {
            System.out.println(hm.get("a").A);
        }

        keshan k = hm.get("b");
        if (k != null) {
            System.out.println(hm.get("b").A);
        }

        k = hm.get("c");
        if (k == null) {
            System.out.println("null catched.");
        }
        */

        //ArcadeMachine.playOneGame( "examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl4.txt", null, new Random().nextInt());
    }
}
