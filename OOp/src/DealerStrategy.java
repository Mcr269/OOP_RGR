public class DealerStrategy implements MoveStrategy {
    @Override
    public boolean shouldHit(Hand hand, ICard dealerVisibleCard) {
        return hand.calculateScore() < GameConfig.DEALER_STOP_LIMIT;
    }
}