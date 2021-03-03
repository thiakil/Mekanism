package mekanism;

/**
 * Created by Thiakil on 28/02/2021.
 */
public class Pair<A,B> {
    public final A left;
    public final B right;

    public Pair(A left, B right) {
        this.left = left;
        this.right = right;
    }

    public static <A,B> Pair<A,B> of (A left, B right) {
        return new Pair<>(left,right);
    }
}
