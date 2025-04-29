public class Card {
    public int rank; // 2–14 where 11 = J, 12 = Q, 13 = K, 14 = A
    public int suit; // 0 = Spades, 1 = Hearts, 2 = Diamonds, 3 = Clubs
    public boolean isDealt;

    public Card(int rank, int suit) {
        if (rank < 2 || rank > 14) throw new IllegalArgumentException("Rank must be 2–14");
        if (suit < 0 || suit > 3) throw new IllegalArgumentException("Suit must be 0–3");
        this.rank = rank;
        this.suit = suit;
        this.isDealt = false;
    }

    @Override
    public String toString() {
        String[] suits = {"♠", "♥", "♦", "♣"};
        String[] ranks = {"", "", "2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"};
        return ranks[rank] + suits[suit];
    }
}
