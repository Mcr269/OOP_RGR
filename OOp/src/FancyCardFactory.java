public class FancyCardFactory implements CardFactory {
    @Override
    public ICard createCard(Suit suit, Rank rank) {
        return new FancyCardDecorator(new SimpleCard(suit, rank));
    }
}