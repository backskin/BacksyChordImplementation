import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChordManager {

    private static Random random = new Random();

    private static String getRandomString(int targetLength){
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'

        StringBuilder buffer = new StringBuilder(targetLength);
        for (int i = 0; i < targetLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    private static NetworkFile randomNetworkFile(){

        // Создается реально виртуально
        int length = random.nextInt(16) + 4;
        String name = getRandomString(length) + "." + getRandomString(3);
        byte[] data = getRandomString(random.nextInt(1000)+100).getBytes();
        return new NetworkFile(name, data);
    }

    private static void disconnectGuy(Peer peer){
        peer.disconnect();
        System.out.println(peer + " has been disconnected :(");
    }

    private Runnable test1 = () -> {

        // Пусть это -  наши начальные данные
        int M_intM = 10;
        int sizeOfNetwork = 20;

        // Насоздаем рандомные идентификаторы в сети
        // (можно конечно было сделать и с лейблами,
        // чтобы плотнее распределялись компы при небольшом их количестве,
        // но кому это надо?)
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
            System.out.println("Обработка на компах... (Ждите 2-3 секунды)");
            Timer timer = new Timer();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Можем продолжать работу на установленной сетке

        // Придумаем 200 файлов, например
        List<NetworkFile> networkFilesStack = new ArrayList<>();
        for (int i = 0; i < 200; i++) networkFilesStack.add(randomNetworkFile());

        // Отправим эти выдуманные файлы в сеть через "голову"
        networkFilesStack.forEach(head::putFileToNetwork);

        // Посмотрим какие файлы куда отправились :^)

        System.out.println("==================== All the Files stored in the P2P network ========================");
        peers.forEach(peer -> {
            System.out.println("\n");
            System.out.println(peer);
            System.out.print("Files list:");
            if (peer.getLocalFiles().size() > 0) {
                peer.getLocalFiles().forEach(file -> System.out.print("\n         " + file
                        + " | hashCodeTail = " + ((file.hashCode() % head.powTwo + head.powTwo) % head.powTwo)));
                System.out.println();
            }
            else System.out.println("EMPTY");
        });
        System.out.println("=====================================================================================");
        NetworkFile networkFile = networkFilesStack.get(177);

        System.out.println("\nLet's find out what peer holds the file " + networkFile);
        Peer unknown = head.getPeerThatHoldingFile(networkFile.getName());
        System.out.println("Found " + unknown);
        System.out.println("He is holding " + unknown.getFileFromNetwork(networkFile.getName()));


        System.out.println("========================= Let's disconnect some poor guys ===========================");

        disconnectGuy(peers.get(4));
        disconnectGuy(peers.get(7));
        disconnectGuy(peers.get(12));
        disconnectGuy(unknown);

        System.out.println("=====================================================================================");

        NetworkFile networkFile2 = networkFilesStack.get(177);
        System.out.println("\nLet's find out what peer holds the file " + networkFile2);
        Peer unknown2 = head.getPeerThatHoldingFile(networkFile2.getName());
        System.out.println("Found " + unknown2);
        System.out.println("He is holding " + unknown2.getFileFromNetwork(networkFile2.getName()));

        // Завершим потоки, чтобы выйти из программы
        peers.forEach(Peer::stopDaemons);
    };

    public static void main(String[] args) {

        ChordManager manager = new ChordManager();

        Thread thread = new Thread(manager.test1);
        thread.start();

    }

}
