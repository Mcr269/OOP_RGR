public class ConsoleLogger implements GameObserver {
    @Override
    public void update(String message) {
        System.out.println(message);
    }
}