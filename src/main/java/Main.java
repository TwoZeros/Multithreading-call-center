import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

public class Main {
    private static final int COUNT_CALL = 7;
    private static final int CALL_DELAY = 1000;
    private static final int TIME_SPEND_CALL_MIN = 3000;
    private static final int TIME_SPEND_CALL_MAX = 3500;
    private static final Random random = new Random();

    public static void main(String[] args) {

        LinkedBlockingQueue<Call> linkedBlockingQueue = new LinkedBlockingQueue<Call>(COUNT_CALL);

        Thread atc = new Thread(() -> {
            IntStream.rangeClosed(1, COUNT_CALL).mapToObj(i -> new Call("Звонок " + i)).forEach(i -> {
                try {
                    Thread.sleep(CALL_DELAY);
                    System.out.printf("Поступил : %s \n",
                            i.getTelephone());
                    linkedBlockingQueue.add(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("АТС прекратила принимать новые входящие вызовы");
        }, "АТС");

        Runnable operatorFunc = () -> {
            String operatorName = Thread.currentThread().getName();
            Call call;
            while (!Thread.interrupted()) {
                while ( (  call = linkedBlockingQueue.poll() ) != null) {
                    try {
                        System.out.printf("Оператор %s начал обработку  %s\n",
                                operatorName,
                                call.getTelephone());
                        Thread.sleep(random.nextInt(TIME_SPEND_CALL_MIN, TIME_SPEND_CALL_MAX));
                        System.out.printf("Оператор %s закончил  %s \n",
                                operatorName,
                                call.getTelephone());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (linkedBlockingQueue.peek() == null &&
                        !atc.isAlive()) {
                    System.out.printf("Оператор %s ушел домой, так как ATC не работает\n", operatorName);
                    break; //Завершаем работу потока, выходим из цикла
                }
            }
        };

        Thread operator1 = new Thread(operatorFunc, "Петя");
        Thread operator2 = new Thread(operatorFunc, "Вася");


        operator1.start();
        operator2.start();
        atc.start();

    }
}
