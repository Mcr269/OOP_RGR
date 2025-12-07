import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        GameManager game = GameManager.getInstance();
        game.addObserver(new ConsoleLogger());
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

    enum Suit {
        HEARTS("♥"), DIAMONDS("♦"), CLUBS("♣"), SPADES("♠");
        final String symbol;
        Suit(String symbol) { this.symbol = symbol; }
        @Override public String toString() { return symbol; }
    }

    enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10),
        JACK(10), QUEEN(10), KING(10), ACE(11);

        private final int value;
        Rank(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    interface ICard {
        int getValue();
        Rank getRank();
        String getDisplayString();
    }

    static class SimpleCard implements ICard {
        private final Suit suit;
        private final Rank rank;

        public SimpleCard(Suit suit, Rank rank) {
            this.suit = suit;
            this.rank = rank;
        }

        @Override public int getValue() { return rank.getValue(); }
        @Override public Rank getRank() { return rank; }
        @Override public String getDisplayString() { return rank + " " + suit; }
        @Override public String toString() { return getDisplayString(); }
    }

    abstract static class CardDecorator implements ICard {
        protected final ICard decoratedCard;
        public CardDecorator(ICard card) { this.decoratedCard = card; }
        @Override public int getValue() { return decoratedCard.getValue(); }
        @Override public Rank getRank() { return decoratedCard.getRank(); }
        @Override public String toString() { return getDisplayString(); }
    }

    static class FancyCardDecorator extends CardDecorator {
        public FancyCardDecorator(ICard card) { super(card); }
        @Override
        public String getDisplayString() {
            return "[" + decoratedCard.getDisplayString() + "]";
        }
    }

    interface CardFactory {
        ICard createCard(Suit suit, Rank rank);
    }

    static class SimpleCardFactory implements CardFactory {
        @Override
        public ICard createCard(Suit suit, Rank rank) {
            return new SimpleCard(suit, rank);
        }
    }

    static class FancyCardFactory implements CardFactory {
        @Override
        public ICard createCard(Suit suit, Rank rank) {
            return new FancyCardDecorator(new SimpleCard(suit, rank));
        }
    }

    static class Deck {
        final private List<ICard> cards;
        private CardFactory factory;

        public Deck(CardFactory factory) {
            this.factory = factory;
            this.cards = new ArrayList<>();
            refill();
        }

        public void setFactory(CardFactory factory) {
            this.factory = factory;
            refill();
        }

        public void refill() {
            cards.clear();
            if (factory == null) return;

            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.add(factory.createCard(suit, rank));
                }
            }
            shuffle();
        }

        public void shuffle() {
            Collections.shuffle(cards);
        }

        public ICard draw() throws DeckEmptyException {
            if (cards.isEmpty()) {
                throw new DeckEmptyException();
            }
            return cards.remove(0);
        }
    }

    static class Hand implements Iterable<ICard> {
        final private List<ICard> cards = new ArrayList<>();

        public void addCard(ICard card) {
            cards.add(card);
        }

        public List<ICard> getCards() { return cards; }

        public int calculateScore() {
            int score = 0;
            int aces = 0;

            for (ICard card : cards) {
                score += card.getValue();
                if (card.getRank() == Rank.ACE) aces++;
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

        @Override
        public Iterator<ICard> iterator() {
            return cards.iterator();
        }
    }

    interface MoveStrategy {
        boolean shouldHit(Hand hand, ICard dealerVisibleCard);
    }

    static class DealerStrategy implements MoveStrategy {
        @Override
        public boolean shouldHit(Hand hand, ICard dealerVisibleCard) {
            return hand.calculateScore() < GameConfig.DEALER_STOP_LIMIT;
        }
    }

    static class HumanStrategy implements MoveStrategy {
        final private Scanner scanner = new Scanner(System.in);

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

    abstract static class Participant {
        protected String name;
        protected Hand hand;
        protected MoveStrategy strategy;

        public Participant(String name, MoveStrategy strategy) {
            this.name = name;
            this.strategy = strategy;
            this.hand = new Hand();
        }

        public boolean makeMove(Deck deck, ICard dealerVisibleCard, GameObserver logger) throws GameException {
            if (strategy.shouldHit(hand, dealerVisibleCard)) {
                ICard card = deck.draw();
                hand.addCard(card);
                logger.update(name + " взяв карту: " + card);
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
        public Dealer() { super("Дилер", new DealerStrategy()); }

        public ICard getVisibleCard() {
            if (hand.getCards().isEmpty()) return null;
            return hand.getCards().get(0);
        }
    }

    static class ParticipantFactory {
        public static Participant create(String type) {
            switch (type.toUpperCase()) {
                case "PLAYER": return new Player("Гравець");
                case "DEALER": return new Dealer();
                default: throw new IllegalArgumentException("Невідомий тип учасника");
            }
        }
    }

    interface GameObserver {
        void update(String message);
    }

    static class ConsoleLogger implements GameObserver {
        @Override
        public void update(String message) {
            System.out.println(message);
        }
    }

    static class GameManager {
        private static GameManager instance;
        final private Deck deck;
        private Participant player;
        private Participant dealer;
        final private List<GameObserver> observers = new ArrayList<>();
        private CardFactory cardFactory;

        private GameManager() {
            this.cardFactory = new SimpleCardFactory();
            this.deck = new Deck(cardFactory);
        }

        public static GameManager getInstance() {
            if (instance == null) instance = new GameManager();
            return instance;
        }

        public void setCardFactory(CardFactory factory) {
            this.cardFactory = factory;
            this.deck.setFactory(factory);
        }

        public void addObserver(GameObserver observer) {
            observers.add(observer);
        }

        private void notifyObservers(String message) {
            for (GameObserver observer : observers) {
                observer.update(message);
            }
        }

        public void start() {
            notifyObservers("--- БЛЕКДЖЕК PRO STARTED ---");

            this.player = ParticipantFactory.create("PLAYER");
            this.dealer = ParticipantFactory.create("DEALER");

            this.deck.refill();

            try {
                playRound();
            } catch (GameException e) {
                notifyObservers("Критична помилка гри: " + e.getMessage());
            }
        }

        private void playRound() throws GameException {
            dealInitialCards();

            processParticipantTurn(player, ((Dealer)dealer).getVisibleCard());

            if (player.getHand().isBusted()) {
                notifyObservers("ПЕРЕБІР! Ви програли.");
                return;
            }

            notifyObservers("\n--- Хід Дилера ---");
            processParticipantTurn(dealer, null);

            determineWinner();
        }

        private void dealInitialCards() throws GameException {
            player.getHand().addCard(deck.draw());
            dealer.getHand().addCard(deck.draw());
            player.getHand().addCard(deck.draw());
            dealer.getHand().addCard(deck.draw());

            notifyObservers("Роздача завершена.");
        }

        private void processParticipantTurn(Participant participant, ICard visibleCard) throws GameException {
            boolean active = true;
            while (active) {
                notifyObservers(participant.getName() + " карти: " + participant.getHand() +
                        " (Очки: " + participant.getHand().calculateScore() + ")");

                if (participant.getHand().isBusted()) {
                    active = false;
                } else if (participant.getHand().isBlackjack()) {
                    notifyObservers(participant.getName() + " має Блекджек!");
                    active = false;
                } else {
                    active = participant.makeMove(deck, visibleCard, msg -> notifyObservers(msg));
                }
            }
        }

        private void determineWinner() {
            int pScore = player.getHand().calculateScore();
            int dScore = dealer.getHand().calculateScore();

            notifyObservers("\n--- РЕЗУЛЬТАТ ---");
            notifyObservers("Гравець: " + pScore + " | Дилер: " + dScore);

            if (dealer.getHand().isBusted()) {
                notifyObservers("Дилер згорів! Ви виграли!");
            } else if (pScore > dScore) {
                notifyObservers("Ви перемогли!");
            } else if (pScore < dScore) {
                notifyObservers("Дилер переміг.");
            } else {
                notifyObservers("Нічия.");
            }
        }
    }
}