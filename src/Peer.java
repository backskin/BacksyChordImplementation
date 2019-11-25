import java.util.*;

public class Peer extends ChordNode {

    private List<NetworkFile> networkFiles;
    private String IPAddress;
    private Thread stabilisation;
    private Thread fixer;

    public String getIPAddress() {
        return IPAddress;
    }

    public Peer(String address, int id, int m) {
        super(id, m);
        IPAddress = address;
        networkFiles = new ArrayList<>();

        stabilisation = new Thread(() -> (new Timer()).schedule(
                new TimerTask() {@Override public void run() { stabilize();}},
                100, 100));


        fixer = new Thread(() -> (new Timer()).schedule(
                new TimerTask() {@Override public void run() { fixFingers(); }},
                100, 100));


    }

    public void startDaemons(){

        stabilisation.start();
        fixer.start();
    }

    public void stopDaemons(){
        stabilisation.interrupt();
        fixer.interrupt();
    }

    static int num = 0;

    public void invite(Peer newbie){
        num += 1;
        System.out.println("the "+ newbie.getID() + " (" + num + ") joins " + this.getID());
        newbie.join(this);
    }

    void join(Peer toPeer) {

        Peer peerWhoShares = null;
        if (toPeer != null) peerWhoShares = (Peer) toPeer.findSuccessorFor(getID());
        super.join(toPeer);
        if (peerWhoShares != null) peerWhoShares.shareWith(this);
        //startDaemons();
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
        Peer holder = (Peer) this.findSuccessorFor(file.hashCode());
        holder.addFile(file);
    }

    public NetworkFile getFileFromNetwork(String filename){
        Peer holder = (Peer) this.findSuccessorFor(filename.hashCode());
        return holder.getFile(filename);
    }

    private NetworkFile getFile(String filename){
        for (NetworkFile f:networkFiles)
            if (f.getName().equals(filename)) return f;
        return null;
    }


    @Override
    public void disconnect() {
        super.disconnect();

    }

    private void addFile(NetworkFile file){
        networkFiles.add(file);
    }

    public List<NetworkFile> getLocalFiles(){
        networkFiles.sort(Comparator.comparingInt(
                o -> (o.hashCode() % powTwo + powTwo) % powTwo));

        return Collections.unmodifiableList(networkFiles);
    }
}
