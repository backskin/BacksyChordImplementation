import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class NetworkFile {

    private String name;
    private byte[] data;

    public NetworkFile(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    public static NetworkFile toNetworkFile(File file){

        try {
            byte[] output = Files.readAllBytes(file.toPath());
            return new NetworkFile(file.getName(), output);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new NetworkFile("null", new byte[1]);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {

        return name + " | [" + data.length + " bytes]";
    }
}
