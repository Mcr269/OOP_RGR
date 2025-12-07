public class Dealer extends Participant {
    public Dealer() {
        super("Дилер", new DealerStrategy());
    }

    public ICard getVisibleCard() {
        if (hand.getCards().isEmpty()) return null;
        return hand.getCards().get(0);
    }
}