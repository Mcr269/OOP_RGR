public abstract class Participant {
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

    public Hand getHand() {
        return hand;
    }

    public String getName() {
        return name;
    }
}