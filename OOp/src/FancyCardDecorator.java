public class FancyCardDecorator extends CardDecorator {
    public FancyCardDecorator(ICard card) {
        super(card);
    }

    @Override
    public String getDisplayString() {
        return "[" + decoratedCard.getDisplayString() + "]";
    }
}