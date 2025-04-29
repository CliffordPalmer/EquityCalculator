import java.util.ArrayList;
import java.util.List;

public class Table {
    public List<Player> players;
    public List<Card> communityCards;

    public Table() {
        players = new ArrayList<>();
        communityCards = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void setCommunityCards(List<Card> cards) {
        if (cards.size() > 5) throw new IllegalArgumentException("Too many community cards");
        communityCards = new ArrayList<>(cards);
    }

    public void displayState() {
        System.out.println("--- Poker Table ---");
        for (Player player : players) {
            System.out.println(player);
        }
        System.out.print("Community Cards: ");
        for (Card card : communityCards) {
            System.out.print(card + " ");
        }
        System.out.println();
    }
}