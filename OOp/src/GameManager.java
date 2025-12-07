import java.util.ArrayList;
import java.util.List;

public class GameManager {
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
                active = participant.makeMove(deck, visibleCard, this::notifyObservers);
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