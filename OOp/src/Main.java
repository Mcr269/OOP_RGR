import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GameManager game = GameManager.getInstance();
        game.addObserver(new ConsoleLogger());

        // Ви можете змінити фабрику на SimpleCardFactory() для звичайного вигляду
        game.setCardFactory(new FancyCardFactory());

        Scanner scanner = new Scanner(System.in);

        while (true) {
            game.start();

            System.out.println("Натисніть Enter щоб зіграти ще раз, або введіть 'q' для виходу:");
            String input = scanner.nextLine();

            if (input.trim().equalsIgnoreCase("q")) {
                System.out.println("Дякуємо за гру!");
                break;
            }
        }
    }
}