public class ParticipantFactory {
    public static Participant create(String type) {
        switch (type.toUpperCase()) {
            case "PLAYER": return new Player("Гравець");
            case "DEALER": return new Dealer();
            default: throw new IllegalArgumentException("Невідомий тип учасника");
        }
    }
}