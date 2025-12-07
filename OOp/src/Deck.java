import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
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