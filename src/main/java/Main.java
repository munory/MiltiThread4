import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    public static BlockingQueue<String> queueA = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> queueB = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> queueC = new ArrayBlockingQueue<>(100);

    public static Map<Character, Long> result = new ConcurrentHashMap<>();

    public static int textCount = 10000;
    public static int textLength = 100_000;

    public static void main(String[] args) throws InterruptedException {

        List<Thread> threads = new ArrayList<>();

        Runnable fillRunnable = () -> {
            for (int i = 0; i < textCount; i++) {
                String text = generateText("abc", textLength);

                try {
                    queueA.put(text);
                    queueB.put(text);
                    queueC.put(text);
                } catch (InterruptedException e) {
                    return;
                }
            }
        };
        Thread fillThread = new Thread(fillRunnable);
        threads.add(fillThread);
        fillThread.start();

        addThread(threads, 'a');
        addThread(threads, 'b');
        addThread(threads, 'c');

        for (Thread thread : threads) {
            thread.join();
        }

        for (var entry: result.entrySet()) {
            System.out.println("максимальное количество символа '" + entry.getKey() + "' равно " + entry.getValue());
        }

    }

    public static void addThread(List<Thread> threads, char ch) {
        Runnable countRunnable = () -> {
            try {
                for (int i = 0; i < textCount; i++) {
                    String text = (ch == 'a' ? queueA.take() : (ch == 'b' ? queueB.take() : queueC.take()));

                    var count = countChar(ch, text);

                    result.putIfAbsent(ch, 0L);

                    if (count > result.get(ch)) {
                        result.put(ch, count);
                    }
                }
            } catch (InterruptedException e) {
                return;
            }
        };
        Thread countThread = new Thread(countRunnable);
        threads.add(countThread);
        countThread.start();
    }

    public static long countChar(char ch, String text) {
        return text.codePoints().filter(c -> c == ch).count();
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

}
