public class Player {
    public String name;
    public Card[] hand;

    public Player(String name) {
        this.name = name;
        this.hand = new Card[2];
    }

    public void addCardToHand(Card card) {
        if (hand[0] == null) {
            hand[0] = card;
        } else if (hand[1] == null) {
            hand[1] = card;
        } else {
            throw new IllegalStateException("Player already has two cards");
        }
        card.isDealt = true;
    }

    @Override
    public String toString() {
        return name + ": [" + hand[0] + ", " + hand[1] + "]";
    }
}