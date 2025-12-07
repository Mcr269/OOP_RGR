public class SimpleCardFactory implements CardFactory {
    @Override
    public ICard createCard(Suit suit, Rank rank) {
        return new SimpleCard(suit, rank);
    }
}