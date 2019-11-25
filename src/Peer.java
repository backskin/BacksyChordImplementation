import java.util.*;

public class Peer extends ChordNode {

    private List<NetworkFile> networkFiles;
    private String netName;
    private Thread stabilisation;
    private Thread fixer;
    private Timer stabilTimer = new Timer();
    private Timer fixTimer = new Timer();;

    private static void waitFor(int milliseconds){
        try {
            System.out.println("Обработка... (Ждите)");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getNetName() {
        return netName;
    }

    public Peer(String address, int id, int m) {
        super(id, m);
        netName = address;
        networkFiles = new ArrayList<>();

        TimerTask stabilTask = new TimerTask() {@Override public void run() { stabilize();}};
        stabilisation = new Thread(() -> stabilTimer.schedule(stabilTask, 50, 100));

        TimerTask fixTask = new TimerTask() {@Override public void run() { fixFingers(); }};
        fixer = new Thread(() -> fixTimer.schedule(fixTask, 50, 100));
    }

    public void startDaemons(){

        stabilisation.start();
        fixer.start();
    }

    public void stopDaemons(){
        stabilTimer.cancel();
        fixTimer.cancel();
    }


    private Peer findSuccPeer(int key){
        return (Peer) findSuccessor(key);
    }

    void join(Peer toPeer) {

        super.join(toPeer);
        if (toPeer != null) findSuccPeer(getID()).shareWith(this);
        // Запустим потоки фикса и стабилизации
        startDaemons();
    }

    @Override
    public void disconnect() {
        stopDaemons();
        waitFor(250);
        Peer neighbour = (Peer) this.successor();
        super.disconnect();
        // переносим все файлы на соседей
        networkFiles.forEach(neighbour::addFile);
        networkFiles.clear();
    }



    private void shareWith(Peer node){

        List<NetworkFile> toRemove = new ArrayList<>();

        for (NetworkFile file: networkFiles)
            if (inRange(file.hashCode(), predecessor().getID(), node.getID(), -1)) {
                node.addFile(file);
                toRemove.add(file);
            }

        removeFilesLater(toRemove);
    }

    private void removeFilesLater(List<NetworkFile> filesToRemove){

        networkFiles.removeAll(filesToRemove);
    }

    public void putFileToNetwork(NetworkFile file){
        Peer holder = (Peer) this.findSuccessor(file.hashCode());
        holder.addFile(file);
    }

    public NetworkFile getFileFromNetwork(String filename){
        return getPeerThatHoldingFile(filename).getFile(filename);
    }

    public Peer getPeerThatHoldingFile(String filename){

        return (Peer) this.findSuccessor(filename.hashCode());
    }

    private NetworkFile getFile(String filename){
        for (NetworkFile f:networkFiles)
            if (f.getName().equals(filename)) return f;
        return null;
    }

    private void addFile(NetworkFile file){
        networkFiles.add(file);
    }

    public List<NetworkFile> getLocalFiles(){
        networkFiles.sort(Comparator.comparingInt(
                o -> (o.hashCode() % powTwo + powTwo) % powTwo));

        return Collections.unmodifiableList(networkFiles);
    }

    @Override
    public String toString() {
        return "Peer <" + netName + "> (id="+getID()+")";
    }
}
