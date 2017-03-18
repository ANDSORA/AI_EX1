package controllers.mytools;

import java.util.Comparator;

/**
 * Created by andsora on 3/17/17.
 */
public class StateTupleComparator implements Comparator<stateTuple> {
    @Override
    public int compare(stateTuple x, stateTuple y) {
        double EPS = 1e-5;
        if ((x.Ascore + x.Hscore) > (y.Ascore + y.Hscore + EPS)) return 1;
        else if ((x.Ascore + x.Hscore) < (y.Ascore + y.Hscore - EPS)) return -1;
        else return 0;
    }
}
