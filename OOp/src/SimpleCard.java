public class SimpleCard implements ICard {
    private final Suit suit;
    private final Rank rank;

    public SimpleCard(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    @Override
    public int getValue() {
        return rank.getValue();
    }

    @Override
    public Rank getRank() {
        return rank;
    }

    @Override
    public String getDisplayString() {
        return rank + " " + suit;
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}