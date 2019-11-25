import java.util.concurrent.ExecutionException;

public abstract class ChordNode {

    public final int powTwo;

    private class Finger {

        private int start;
        private int exponentOfTwo;
        private ChordNode node;

        Finger(int i){
            exponentOfTwo = (int) Math.pow(2, i);
            start = (identifier + exponentOfTwo) % powTwo;
        }
    }

    private int identifier;
    private Finger[] fingers;
    private ChordNode predecessor = null;
    private int lastFinger = 0;

    int getID() {
        return identifier;
    }

    protected ChordNode predecessor() {
        return predecessor;
    }

    protected ChordNode(int id, int m){

        powTwo = (int) Math.pow(2, m);
        identifier = id % powTwo;
        fingers = new Finger[m];
        for (int i = 0; i < fingers.length; i++) fingers[i] = new Finger(i);
    }

    protected final ChordNode findSuccessorFor(int id){
        id = (id % powTwo + powTwo) % powTwo;
        return findPredecessor(id).fingers[0].node;
    }

    protected void join(ChordNode node){
        if (node != null){
            initFingerTable(node);
            updateOthers();
        } else {
            for (Finger finger : fingers) finger.node = this;
            predecessor = this;
        }
    }

    public void disconnect(){
        predecessor.fingers[0].node = fingers[0].node;
        fingers[0].node.predecessor = predecessor;

        for (int i = 0; i < fingers.length; i++) {

            int id = identifier - fingers[i].exponentOfTwo * 2;
            id = id < 0 ? powTwo : id;
            findPredecessor(id).updateFingerTable(fingers[0].node, i, 1);
        }
    }

    protected final void stabilize(){

        ChordNode x = fingers[0].node.predecessor;
        if (inRange(x.identifier, identifier, fingers[0].node.identifier, 0))
            fingers[0].node = x;

        fingers[0].node.checkAsPredecessor(this);
    }

    private void checkAsPredecessor(ChordNode node){

        if (predecessor == null || inRange (node.identifier,
                predecessor.identifier, identifier, 0))

            predecessor = node;
    }

    protected final void fixFingers(){
        lastFinger += 1;
        if (lastFinger >= fingers.length) lastFinger = 0;
        fingers[lastFinger].node = findSuccessorFor(fingers[lastFinger].start);
    }

    private ChordNode findPredecessor(int id){

        ChordNode t = this;
        int deepness = 1;
        while (true){
            if (deepness % 2000 == 0){
                System.out.println("\n[findPred (88 line)]" +
                        "\nI'm stuck :( PLS HELP" +
                        "\nid="+id + " orig id="+identifier);
            }
            else deepness++;

            Finger finger = t.fingers[0];
            int fnid = finger.node.identifier;
            int tid = t.identifier;
            boolean bool = inRange(id, tid, fnid, 1);
            if (bool) break;

            t = t.closestPrecedingNode(id);
        }

//        while (!inRange(id, t.identifier, t.fingers[0].node.identifier, 1))
//            t = t.closestPrecedingNode(id);

        return t;
    }

    private ChordNode closestPrecedingNode(int id){
        for (int i = fingers.length-1; i >= 0; i--)
            if (inRange(fingers[i].node.identifier, identifier, id, 0))
                return fingers[i].node;
        return this;
    }

    private void initFingerTable(ChordNode node){

        fingers[0].node = node.findSuccessorFor(fingers[0].start);
        predecessor = fingers[0].node.predecessor;
        fingers[0].node.predecessor = this;

        for (int i = 0; i < fingers.length-1; i++)
            if (inRange(fingers[i + 1].start, identifier, fingers[i].node.identifier, -1))
                fingers[i + 1].node = fingers[i].node;
            else
                fingers[i + 1].node = node.findSuccessorFor(fingers[i + 1].start);
    }

    private void updateOthers(){
        System.out.println("id="+identifier);

        for (int i = 0, fingersLength = fingers.length; i < fingersLength; i++){
            findPredecessor(identifier - fingers[i].exponentOfTwo).updateFingerTable(this, i, 1);
            System.out.print(" ["+i+"]");
        }
        System.out.println();
    }

    private void updateFingerTable(ChordNode sNode, int i, int deepness){
        if (deepness % 5000 == 0) {
            System.out.println("\n[upFingTabl (131 line)]" +
                    "\nI'm stuck :( PLS HELP" +
                    "\nid="+sNode.identifier);
        }
        if (inRange(sNode.identifier, fingers[i].start, fingers[i].node.identifier, -1)){
            fingers[i].node = sNode;
            predecessor.updateFingerTable(sNode, i, deepness+1);
        }
    }

    protected boolean inRange(int id, int left, int right, int border) {
        // values clipping, in case of shrek them into [0, 2^m]
        id = (id % powTwo + powTwo) % powTwo;
        left = (left % powTwo + powTwo) % powTwo;
        right = (right % powTwo + powTwo) % powTwo;

        if (left >= right) {
            right += powTwo;
            if (left > id) id += powTwo;
        }
        boolean stuck = id > left && id < right;
        switch (Integer.compare(border,0)){
            case -1: return (id == left % powTwo) || stuck;
            case  0: return stuck;
            case  1: return (id == right % powTwo) || stuck;
            default: return false;
        }
    }
}
