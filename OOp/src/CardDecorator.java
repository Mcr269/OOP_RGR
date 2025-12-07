public abstract class CardDecorator implements ICard {
    protected final ICard decoratedCard;

    public CardDecorator(ICard card) {
        this.decoratedCard = card;
    }

    @Override
    public int getValue() {
        return decoratedCard.getValue();
    }

    @Override
    public Rank getRank() {
        return decoratedCard.getRank();
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}