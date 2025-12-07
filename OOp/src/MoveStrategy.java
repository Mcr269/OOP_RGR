public interface MoveStrategy {
    boolean shouldHit(Hand hand, ICard dealerVisibleCard);
}