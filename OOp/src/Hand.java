import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Hand implements Iterable<ICard> {
    final private List<ICard> cards = new ArrayList<>();

    public void addCard(ICard card) {
        cards.add(card);
    }

    public List<ICard> getCards() {
        return cards;
    }

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
    public String toString() {
        return cards.toString();
    }

    @Override
    public Iterator<ICard> iterator() {
        return cards.iterator();
    }
}