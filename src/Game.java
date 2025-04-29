import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game {
    private Card[] deck = new Card[52];
    private List<Player> players = new ArrayList<>();
    private List<Card> communityCards = new ArrayList<>();
    private int cardsLeft;

    public Game() {
        int index = 0;
        for (int suit = 0; suit < 4; suit++) {
            for (int rank = 2; rank <= 14; rank++) {
                deck[index++] = new Card(rank, suit);
            }
        }
        cardsLeft = 52;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter number of players: ");
        int numPlayers = getValidIntInput(scanner, 1, 10);
        scanner.nextLine(); // consume newline

        for (int i = 0; i < numPlayers; i++) {
            System.out.print("Enter player name: ");
            String name = scanner.nextLine();
            Player player = new Player(name);
            for (int j = 0; j < 2; j++) {
                Card card = promptForCard(scanner);
                player.addCardToHand(card);
            }
            players.add(player);
        }

        System.out.print("Enter number of community cards (0-5): ");
        int numCommunity = getValidIntInput(scanner, 0, 5);
        scanner.nextLine(); // consume newline
        for (int i = 0; i < numCommunity; i++) {
            Card card = promptForCard(scanner);
            communityCards.add(card);
        }

        displayState();
        scanner.close();
    }

    private int getValidIntInput(Scanner scanner, int min, int max) {
        int value;
        while (true) {
            try {
                value = scanner.nextInt();
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.printf("Please enter a number between %d and %d: ", min, max);
                }
            } catch (Exception e) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.nextLine(); // consume invalid input
            }
        }
    }

    private Card promptForCard(Scanner scanner) {
        while (true) {
            try {
                System.out.print("Enter card rank (2-10, J=11, Q=12, K=13, A=14): ");
                int rank = getValidIntInput(scanner, 2, 14);

                System.out.print("Enter card suit (0=♠, 1=♥, 2=♦, 3=♣): ");
                int suit = getValidIntInput(scanner, 0, 3);

                scanner.nextLine(); // consume newline

                // Calculate index in the deck array
                int index = (suit * 13) + (rank - 2);

                // Double-check index bounds (should be unnecessary with validation above)
                if (index < 0 || index >= 52) {
                    System.out.println("Invalid card combination. Try again.");
                    continue;
                }

                Card card = deck[index];
                if (!card.isDealt) {
                    card.isDealt = true;
                    cardsLeft --;
                    return card;
                } else {
                    System.out.println("Card already dealt. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Try again.");
                scanner.nextLine(); // consume invalid input
            }
        }
    }

    private void displayState() {
        System.out.println("--- Game State ---");
        for (Player player : players) {
            System.out.println(player);
        }
        System.out.print("Community Cards: ");
        for (Card card : communityCards) {
            System.out.print(card + " ");
        }
        System.out.println();
    }

    private Card randDealCard() {

        int index;
        Card card;

        if (cardsLeft == 0) {
            return null;
        }
        index = (int) (Math.random() * cardsLeft) + 1;
        for (int i = 0; i < 52; i++) {
            card = deck[index];
            if (!card.isDealt) {
                index--;
            }
            if (index == 0) {
                return deck[i];
            }
        }
    }

    private void calcEq(){

    }

    public static void main(String[] args) {
        Game game = new Game();
        game.run();
    }
}