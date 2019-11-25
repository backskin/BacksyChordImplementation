public abstract class ChordNode {

    final int powTwo;

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

    protected ChordNode successor() { return fingers[0].node; }
    protected ChordNode predecessor() {
        return predecessor;
    }

    protected ChordNode(int id, int m){

        powTwo = (int) Math.pow(2, m);
        identifier = id % powTwo;
        fingers = new Finger[m];
        for (int i = 0; i < fingers.length; i++) fingers[i] = new Finger(i);
    }

    protected final ChordNode findSuccessor(int id){
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
        fingers[0].node.updateOthers();
        for (int i = 0; i < fingers.length; i++) {

            int id = identifier - fingers[i].exponentOfTwo;
            id = id < 0 ? powTwo : id;
            findPredecessor(id).updateFingerTable(fingers[0].node, i);
        }
        predecessor = null;
//        TO-DO: пока нормально отконнектиться не получается, если делать последнюю строчку
//        for (int i = 0; i < fingers.length; i++) fingers[i].node = this;
    }

    protected final void stabilize(){

        ChordNode x = fingers[0].node.predecessor;
        if (inRange(x.identifier, identifier, fingers[0].node.identifier, 0))
            fingers[0].node = x;

        fingers[0].node.checkPredecessor(this);
    }

    private void checkPredecessor(ChordNode node){

        if (predecessor == null || inRange (node.identifier,
                predecessor.identifier, identifier, 0))

            predecessor = node;
    }

    protected final void fixFingers(){
//        lastFinger += 1;
//        if (lastFinger >= fingers.length) lastFinger = 0;
        for (Finger finger: fingers) finger.node = findSuccessor(finger.start);
//        fingers[lastFinger].node = findSuccessor(fingers[lastFinger].start);
    }

    private ChordNode findPredecessor(int id){

        ChordNode t = this;
        while (!inRange(id, t.identifier, t.fingers[0].node.identifier, 1))
            t = t.closestPrecedingNode(id);
        return t;
    }

    private ChordNode closestPrecedingNode(int id){
        for (int i = fingers.length-1; i >= 0; i--)
            if (inRange(fingers[i].node.identifier, identifier, id, 0))
                return fingers[i].node;
        return this;
    }

    private void initFingerTable(ChordNode node){

        fingers[0].node = node.findSuccessor(fingers[0].start);
        predecessor = fingers[0].node.predecessor;
        fingers[0].node.predecessor = this;

        for (int i = 0; i < fingers.length-1; i++)
            if (inRange(fingers[i + 1].start, identifier, fingers[i].node.identifier, -1))
                fingers[i + 1].node = fingers[i].node;
            else
                fingers[i + 1].node = node.findSuccessor(fingers[i + 1].start);
    }

    private void updateOthers(){
        for (int i = 0, fingersLength = fingers.length; i < fingersLength; i++)
            findPredecessor(identifier - fingers[i].exponentOfTwo / 2)
                    .updateFingerTable(this, i);
    }

    // Перевод на русский: если узел sNode должен быть i-ой записью в таблице маршрутизации текущего узла,
    // то записываем узел sNode в узел и предлагаем предшественнику проверить то же самое.
    private void updateFingerTable(ChordNode sNode, int i) {
        if (inRange(sNode.identifier, fingers[i].start, fingers[i].node.identifier, -1)) {
            fingers[i].node = sNode;
            predecessor.updateFingerTable(sNode, i);
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
            case -1: return (id % powTwo == left % powTwo) || stuck;
            case  0: return stuck;
            case  1: return (id % powTwo == right % powTwo) || stuck;
            default: return false;
        }
    }
}
