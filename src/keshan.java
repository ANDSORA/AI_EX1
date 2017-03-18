/**
 * Created by andsora on 3/8/17.
 */

import java.util.HashMap;

public class keshan {
    public int A;
    public keshan(int a) {
        A = a;
    }

    static void main(String args[]) {
        HashMap<String, keshan> hm = new HashMap<String, keshan>();
        hm.put("a", new keshan(1));
        hm.put("b", new keshan(2));

        if (hm.get("a") != null) {
            System.out.println(hm.get("a"));
        }

        keshan k = hm.get("b");
        if (k != null) {
            System.out.println(hm.get("b"));
        }

        k = hm.get("c");
        if (k == null) {
            System.out.println("null catched.");
        }
    }
}
