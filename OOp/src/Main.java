import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        GameManager game = GameManager.getInstance();
        game.start();
    }

    static class GameConfig {
        public static final int BLACKJACK_LIMIT = 21;
        public static final int DEALER_STOP_LIMIT = 17;
    }

    static class GameException extends Exception {
        public GameException(String message) { super(message); }
    }

    static class DeckEmptyException extends GameException {
        public DeckEmptyException() { super("У колоді закінчилися карти!"); }
    }

    enum Suit { HEARTS, DIAMONDS, CLUBS, SPADES }
    enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10),
        JACK(10), QUEEN(10), KING(10), ACE(11);

        private final int value;
        Rank(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    static class Card {
        private final Suit suit;
        private final Rank rank;

        public Card(Suit suit, Rank rank) {
            this.suit = suit;
            this.rank = rank;
        }

        public int getValue() { return rank.getValue(); }

        @Override
        public String toString() { return rank + " of " + suit; }
    }

    static class Deck {
        final private List<Card> cards;

        public Deck() {
            this.cards = new ArrayList<>();
            refill();
        }

        public void refill() {
            cards.clear();
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.add(new Card(suit, rank));
                }
            }
            shuffle();
        }

        public void shuffle() {
            Collections.shuffle(cards);
        }

        public Card draw() throws DeckEmptyException {
            if (cards.isEmpty()) {
                throw new DeckEmptyException();
            }
            return cards.removeFirst();
        }
    }

    static class Hand {
        final private List<Card> cards = new ArrayList<>();

        public void addCard(Card card) {
            cards.add(card);
        }

        public List<Card> getCards() { return cards; }

        public int calculateScore() {
            int score = 0;
            int aces = 0;

            for (Card card : cards) {
                score += card.getValue();
                if (card.rank == Rank.ACE) aces++;
            }

            while (score > GameConfig.BLACKJACK_LIMIT && aces > 0) {
                score -= 10;
                aces--;
            }
            return score;
        }

        public boolean isBusted() {
            return calculateScore() > GameConfig.BLACKJACK_LIMIT;
        }

        public boolean isBlackjack() {
            return calculateScore() == GameConfig.BLACKJACK_LIMIT;
        }

        @Override
        public String toString() { return cards.toString(); }
    }

    interface MoveStrategy {
        boolean shouldHit(Hand hand, Card dealerVisibleCard);
    }

    static class DealerStrategy implements MoveStrategy {
        @Override
        public boolean shouldHit(Hand hand, Card dealerVisibleCard) {
            return hand.calculateScore() < GameConfig.DEALER_STOP_LIMIT;
        }
    }

    static class HumanStrategy implements MoveStrategy {
        final private Scanner scanner = new Scanner(System.in);

        @Override
        public boolean shouldHit(Hand hand, Card dealerVisibleCard) {
            while (true) {
                System.out.print("Ваш хід: (1) Взяти карту, (2) Досить: ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Помилка: Ввід не може бути пустим. Спробуйте ще раз.");
                    continue;
                }

                if (input.equals("1")) {
                    return true;
                } else if (input.equals("2")) {
                    return false;
                } else {
                    System.out.println("Помилка: Некоректний символ '" + input + "'. Будь ласка, введіть 1 або 2.");
                }
            }
        }
    }

    abstract static class Participant {
        protected String name;
        protected Hand hand;
        protected MoveStrategy strategy;

        public Participant(String name, MoveStrategy strategy) {
            this.name = name;
            this.strategy = strategy;
            this.hand = new Hand();
        }

        public boolean makeMove(Deck deck, Card dealerVisibleCard) throws GameException {
            if (strategy.shouldHit(hand, dealerVisibleCard)) {
                Card card = deck.draw();
                hand.addCard(card);
                System.out.println(name + " взяв карту: " + card);
                return true;
            }
            return false;
        }

        public Hand getHand() { return hand; }
        public String getName() { return name; }
    }

    static class Player extends Participant {
        public Player(String name) { super(name, new HumanStrategy()); }
    }

    static class Dealer extends Participant {
        public Dealer() { super("Dealer", new DealerStrategy()); }

        public Card getVisibleCard() {
            return hand.getCards().isEmpty() ? null : hand.getCards().getFirst();
        }
    }

    static class GameManager {
        private static GameManager instance;
        final private Deck deck;
        final private Participant player;
        final private Dealer dealer;

        private GameManager() {
            deck = new Deck();
            player = new Player("Гравець");
            dealer = new Dealer();
        }

        public static GameManager getInstance() {
            if (instance == null) instance = new GameManager();
            return instance;
        }

        public void start() {
            System.out.println("--- БЛЕКДЖЕК PRO ---");
            try {
                playRound();
            } catch (GameException e) {
                System.err.println("Помилка гри: " + e.getMessage());
            }
        }

        private void playRound() throws GameException {
            dealInitialCards();

            processParticipantTurn(player, dealer.getVisibleCard());

            if (player.getHand().isBusted()) {
                System.out.println("ПЕРЕБІР! Ви програли.");
                return;
            }

            System.out.println("\n--- Хід Дилера ---");
            processParticipantTurn(dealer, null);

            determineWinner();
        }

        private void dealInitialCards() throws GameException {
            player.getHand().addCard(deck.draw());
            dealer.getHand().addCard(deck.draw());
            player.getHand().addCard(deck.draw());
            dealer.getHand().addCard(deck.draw());
        }

        private void processParticipantTurn(Participant participant, Card visibleCard) throws GameException {
            boolean active = true;
            while (active) {
                System.out.println(participant.getName() + " карти: " + participant.getHand() +
                        " (Очки: " + participant.getHand().calculateScore() + ")");

                if (participant.getHand().isBusted()) {
                    active = false;
                } else if (participant.getHand().isBlackjack()) {
                    System.out.println(participant.getName() + " має Блекджек!");
                    active = false;
                } else {
                    active = participant.makeMove(deck, visibleCard);
                }
            }
        }

        private void determineWinner() {
            int pScore = player.getHand().calculateScore();
            int dScore = dealer.getHand().calculateScore();

            System.out.println("\n--- РЕЗУЛЬТАТ ---");
            System.out.println("Гравець: " + pScore + " | Дилер: " + dScore);

            if (dealer.getHand().isBusted()) {
                System.out.println("Дилер згорів! Ви виграли!");
            } else if (pScore > dScore) {
                System.out.println("Ви перемогли!");
            } else if (pScore < dScore) {
                System.out.println("Дилер переміг.");
            } else {
                System.out.println("Нічия.");
            }
        }
    }
}