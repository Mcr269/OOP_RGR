import java.util.Scanner;

public class HumanStrategy implements MoveStrategy {
    // Використовуємо один Scanner для уникнення проблем із закриттям потоку
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public boolean shouldHit(Hand hand, ICard dealerVisibleCard) {
        while (true) {
            System.out.print(">> Ваш хід: (1) Взяти карту, (2) Досить: ");
            String input = scanner.nextLine().trim();

            if (input.equals("1")) return true;
            if (input.equals("2")) return false;

            System.out.println("Помилка: введіть 1 або 2.");
        }
    }
}