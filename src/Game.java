import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game {
    private Card[] deck = new Card[52];
    private List<Player> players = new ArrayList<>();
    private List<Card> communityCards = new ArrayList<>();
    private int cardsLeft;
    HandEvaluator eval;

    public Game() {
        int index = 0;
        for (int suit = 0; suit < 4; suit++) {
            for (int rank = 2; rank <= 14; rank++) {
                deck[index++] = new Card(rank, suit);
            }
        }
        cardsLeft = 52;
    }

    public void runSim(){
        eval = new HandEvaluator();
        Scanner scanner = new Scanner(System.in);

        Player playerOne = new Player("Clifford");
        Player playerTwo = new Player("Cody");
        playerOne.addCardToHand(dealCard(14, 1));
        playerOne.addCardToHand(dealCard(8, 1));
        playerTwo.addCardToHand(dealCard(7, 2));
        playerTwo.addCardToHand(dealCard(8, 2));
        players.add(playerOne);
        players.add(playerTwo);
//        communityCards.add(dealCard(14, 3));
//        communityCards.add(dealCard(13, 3));
//        communityCards.add(dealCard(3, 0));
//        communityCards.add(dealCard(5, 0));
//        communityCards.add(dealCard(8, 0));

        int numCommunity = communityCards.size();

        int playerOneWins = 0;
        int playerTwoWins = 0;
        int ties = 0;

        System.out.println("Press enter to calculate EV");
        String pause = scanner.nextLine();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 1000; j++) {
                int cardsToDeal = 5 - numCommunity;
                for (int k = 0; k < cardsToDeal; k++) {
                    communityCards.add(randDealCard());
                }
                long playerOneStrength = eval.evaluateHand(players.get(0).getHand(), communityCards);
                long playerTwoStrength = eval.evaluateHand(players.get(1).getHand(), communityCards);
                if(playerOneStrength > playerTwoStrength){
                    playerOneWins++;
                }
                else if(playerOneStrength < playerTwoStrength){
                    playerTwoWins++;
                }
                else{
                    ties++;
                }

                displayState(playerOneStrength, playerTwoStrength);
                for (int k = 0; k < cardsToDeal; k++) {
                    communityCards.remove(communityCards.size() - 1).setDealt(false);
                    cardsLeft++;
                }
            }
            int totalHands = playerOneWins + playerTwoWins + ties;
            System.out.println("Player 1: " + (100.0 * playerOneWins/totalHands));
            System.out.println("Player 2: " + (100.0 * playerTwoWins/totalHands));
        }

        scanner.close();
    }

    public void run() {
        HandEvaluator eval = new HandEvaluator();
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

        long playerOneStrength = eval.evaluateHand(players.get(0).getHand(), communityCards);
        long playerTwoStrength = eval.evaluateHand(players.get(1).getHand(), communityCards);

        System.out.println("Player one: " + playerOneStrength);
        System.out.println("Player two: " + playerTwoStrength);

//        Player playerOne = new Player("Clifford");
//        Player playerTwo = new Player("Cody");
//        playerOne.addCardToHand(new Card(14, 1));
//        playerOne.addCardToHand(new Card(14, 2));
//        playerTwo.addCardToHand(new Card(13, 3));
//        playerTwo.addCardToHand(new Card(13, 0));
//        players.add(playerOne);
//        players.add(playerTwo);
//
//        int numCommunity = 0;
//
//        int playerOneWins = 0;
//        int playerTwoWins = 0;
//        int ties = 0;
//
//        System.out.println("Press enter to calculate EV");
//        String pause = scanner.nextLine();
//        for (int i = 0; i < 10; i++) {
//            for (int j = 0; j < 100; j++) {
//                int cardsToDeal = 5 - numCommunity;
//                for (int k = 0; k < cardsToDeal; k++) {
//                    communityCards.add(randDealCard());
//                }
//                long playerOneStrength = eval.evaluateHand(players.get(0).getHand(), communityCards);
//                long playerTwoStrength = eval.evaluateHand(players.get(1).getHand(), communityCards);
//                if(playerOneStrength > playerTwoStrength){
//                    playerOneWins++;
//                }
//                else if(playerOneStrength < playerTwoStrength){
//                    playerTwoWins++;
//                }
//                else{
//                    ties++;
//                }
//
//                displayState(playerOneStrength, playerTwoStrength);
//                for (int k = 0; k < cardsToDeal; k++) {
//                    communityCards.remove(communityCards.size() - 1).setDealt(false);
//                    cardsLeft++;
//                }
//            }
//            int totalHands = playerOneWins + playerTwoWins + ties;
//            System.out.println("Player 1: " + (100.0 * playerOneWins/totalHands));
//            System.out.println("Player 2: " + (100.0 * playerTwoWins/totalHands));
//        }

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

    private Card dealCard(int rank, int suit){
        // Calculate index in the deck array
        int index = (suit * 13) + (rank - 2);
        Card card = deck[index];

        if (!card.isDealt) {
            card.isDealt = true;
            cardsLeft --;
            return card;
        } else {
            return null;
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

    private void displayState(long playerOneStrength, long playerTwoStrength) {
        System.out.println("--- Game State ---");
        for (Player player : players) {
            System.out.println(player);
        }
        System.out.print("Community Cards: ");
        for (Card card : communityCards) {
            System.out.print(card + " ");
        }
        System.out.println();
        System.out.println("player 1 hand strength: " + playerOneStrength);
        System.out.println(HandEvaluator.describeHand(playerOneStrength));
        System.out.println("player 2 hand strength: " + playerTwoStrength);
        System.out.println(HandEvaluator.describeHand(playerTwoStrength));
    }

    private Card randDealCard() {

        int index;
        Card card;

        if (cardsLeft == 0) {
            return null;
        }

        index = (int) (Math.random() * cardsLeft) + 1;
        int i = 0;

        do {
            if (!deck[i].isDealt) {
                index--;
            }
            i++;
        } while (index > 0 && i < 52);

        if (i > 0 && i <= 52) {
            card = deck[i-1];
            card.isDealt = true;
            cardsLeft--;
            return card;
        }

        return null;
    }

    private void calcEq(){
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.runSim();
    }
}