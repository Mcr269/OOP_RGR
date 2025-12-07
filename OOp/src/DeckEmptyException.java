public class DeckEmptyException extends GameException {
    public DeckEmptyException() {
        super("У колоді закінчилися карти!");
    }
}