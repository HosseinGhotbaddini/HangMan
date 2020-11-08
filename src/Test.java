import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Test {
    public static void main(String[] args) {
        Semaphore controller = new Semaphore(1, true);
        Scanner s = new Scanner(System.in);
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        controller.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Thread.");
                }
            }
        }.start();

        while (true) {
            String inp = s.next();
            if (inp.equals("a"))
                continue;
            else
                controller.release();
            System.out.println(controller.availablePermits());
        }

    }
}
