import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChordManager {

    public static String getRandomString(int targetLength){
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetLength);
        for (int i = 0; i < targetLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    public static NetworkFile randomNetworkFile(){
        int length = (new Random()).nextInt(16) + 4;
        String name = getRandomString(length) + "." + getRandomString(3);
        byte[] data = getRandomString(100).getBytes();
        return new NetworkFile(name, data);
    }


    public static void main(String[] args) {

        // Это наши начальные данные
        int M_intM = 10;
        int sizeOfNetwork = 100;
        List<Integer> positions = new ArrayList<>();

        //int[] m_arPos = new int[]{25, 40, 133, 80, 99, 405, 7, 899, 1001, 763};
        //for (int i:m_arPos) positions.add(i);
        (new Random()).ints(1, (int)Math.pow(2, M_intM))
                .distinct().limit(sizeOfNetwork).sorted().forEach(positions::add);

        //Чтобы с чего-то начать - регистрируем "голову" (первого пира)
        Peer head = new Peer("localhost", 264, M_intM);
        head.join(null);

        //Придумываем остальных пиров
        List<Peer> peers = new ArrayList<>();
        positions.sort(Integer::compareTo);
        positions.forEach(id -> peers.add(new Peer(getRandomString(10), id, M_intM)));

        //Присоединяем к сети всех новых пиров через голову
        peers.forEach(head::invite);
        // Не забудем добавить голову в список (это на потом)
        peers.add(head);
        peers.sort(Comparator.comparingInt(ChordNode::getID));

        // Запустим потоки фикса и стабилизации на всех устройствах
        peers.forEach(Peer::startDaemons);
        //Подождем пока потоки отработают хоть немного
        try {
            System.out.println("Обработка на компах... (Ждите 1-2 секунды)");
            TimeUnit.MILLISECONDS.sleep(1600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Можем продолжать работу на установленной сетке

        // Придумаем 200 файлов, например
        List<NetworkFile> networkFilesStack = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            networkFilesStack.add(randomNetworkFile());
        }
        // Отправим эти выдуманные файлы в сеть через "голову"
        for (NetworkFile file: networkFilesStack){
            head.putFileToNetwork(file);
        };

        // Посмотрим какие файлы куда отправились :^)
        peers.forEach(peer -> {
            System.out.println("\n\nPeer <" + peer.getIPAddress() + "> (id=" + peer.getID()+")");
            System.out.print("Files list: ");
            if (peer.getLocalFiles().size() > 0)
                peer.getLocalFiles().forEach(file -> System.out.print("\n       "+file
                    + " | point="+ ((file.hashCode() % head.powTwo + head.powTwo) % head.powTwo)));
            else System.out.println("EMPTY");
        });
        System.out.println();

        // Завершим потоки, чтобы выйти из программы
        peers.forEach(Peer::stopDaemons);
    }
}
