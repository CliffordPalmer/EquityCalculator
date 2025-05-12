import java.util.*;

public class Game {
    private Card[] deck = new Card[52];
    private List<Player> players = new ArrayList<>();
    private List<Card> communityCards = new ArrayList<>();
    private int cardsLeft;
    // Poker range table for common shorthand hand notations
    private static final String[][] pokerRangeTable = {
            {"AA", "AKs", "AQs", "AJs", "ATs", "A9s", "A8s", "A7s", "A6s", "A5s", "A4s", "A3s", "A2s" },
            {"AKo", "KK", "KQs", "KJs", "KTs", "K9s", "K8s", "K7s", "K6s", "K5s", "K4s", "K3s", "K2s" },
            {"AQo", "KQo", "QQ", "QJs", "QTs", "Q9s", "Q8s", "Q7s", "Q6s", "Q5s", "Q4s", "Q3s", "Q2s" },
            {"AJo", "KJo", "QJo", "JJ", "JTs", "J9s", "J8s", "J7s", "J6s", "J5s", "J4s", "J3s", "J2s" },
            {"ATo", "KTo", "QTo", "JTo", "TT", "T9s", "T8s", "T7s", "T6s", "T5s", "T4s", "T3s", "T2s" },
            {"A9o", "K9o", "Q9o", "J9o", "T9o", "99", "98s", "97s", "96s", "95s", "94s", "93s", "92s" },
            {"A8o", "K8o", "Q8o", "J8o", "T8o", "98o", "88", "87s", "86s", "85s", "84s", "83s", "82s" },
            {"A7o", "K7o", "Q7o", "J7o", "T7o", "97o", "87o", "77", "76s", "75s", "74s", "73s", "72s" },
            {"A6o", "K6o", "Q6o", "J6o", "T6o", "96o", "86o", "76o", "66", "65s", "64s", "63s", "62s" },
            {"A5o", "K5o", "Q5o", "J5o", "T5o", "95o", "85o", "75o", "65o", "55", "54s", "53s", "52s" },
            {"A4o", "K4o", "Q4o", "J4o", "T4o", "94o", "84o", "74o", "64o", "54o", "44", "43s", "42s" },
            {"A3o", "K3o", "Q3o", "J3o", "T3o", "93o", "83o", "73o", "63o", "53o", "43o", "33", "32s" },
            {"A2o", "K2o", "Q2o", "J2o", "T2o", "92o", "82o", "72o", "62o", "52o", "42o", "32o", "22" }
    };
    HandEvaluator eval;

    public Game() {
        int index = 0;
        for (int suit = 0; suit < 4; suit++) {
            for (int rank = 2; rank <= 14; rank++) {
                deck[index++] = new Card(rank, suit);
            }
        }
        cardsLeft = 52;
        players.add(new Player("Clifford"));
        ;
        players.add(new Player("Cody"));
        ;
    }

    public double[] runSim() {
        double[] toReturn = new double[3];
        eval = new HandEvaluator();
        int numCommunity = communityCards.size();

        int playerOneWins = 0;
        int playerTwoWins = 0;
        int ties = 0;

        System.out.println("Press enter to calculate EV");
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 1000; j++) {
                int cardsToDeal = 5 - numCommunity;
                for (int k = 0; k < cardsToDeal; k++) {
                    communityCards.add(randDealCard());
                }
                long playerOneStrength = eval.evaluateHand(players.get(0).getHand(), communityCards);
                long playerTwoStrength = eval.evaluateHand(players.get(1).getHand(), communityCards);
                if (playerOneStrength > playerTwoStrength) {
                    playerOneWins++;
                } else if (playerOneStrength < playerTwoStrength) {
                    playerTwoWins++;
                } else {
                    ties++;
                }

                //displayState(playerOneStrength, playerTwoStrength);
                for (int k = 0; k < cardsToDeal; k++) {
                    communityCards.remove(communityCards.size() - 1).setDealt(false);
                    cardsLeft++;
                }
            }
            int totalHands = playerOneWins + playerTwoWins + ties;
            // First index is player one equity
            toReturn[0] = 100.0 * playerOneWins / totalHands;
            // Second index is player two equity
            toReturn[1] = 100.0 * playerTwoWins / totalHands;
            // Third index is player 3 equity
            toReturn[2] = 100.0 * ties / totalHands;
//            System.out.println(toReturn[0]);
//            System.out.println(toReturn[1]);
//            System.out.println(toReturn[2]);
        }
        return toReturn;
    }

    public void run() {
        HandEvaluator eval = new HandEvaluator();
        Scanner scanner = new Scanner(System.in);

        int numPlayers = 2;

        for (int i = 0; i < numPlayers; i++) {
            Player player = new Player(Character.toString(i));
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


    public double[] rangeSim(boolean[][] rangeTable, Card[] myHand) {
        double playerOneEquity = 0.0;
        double playerTwoEquity = 0.0;
        double tieEquity = 0.0;
        int hands = 0;

        // Iterate through the range table and generate possible opponent hands
        for (int i = 0; i < rangeTable.length; i++) {
            for (int j = 0; j < rangeTable[i].length; j++) {
                if (rangeTable[i][j]) {
                    String rangeCode = pokerRangeTable[i][j];
                    Card[] opponentHand = parseHandFromRangeCode(rangeCode);

                    if (opponentHand != null && isHandValid(opponentHand, myHand, communityCards)) {
                        // Add opponent hand to players for simulation
                        players.get(1).hand = opponentHand;

                        // Run the simulation using runSim
                        players.get(0).hand = myHand;
                        double[] results = runSim();

                        // Only consider equity for our hand (player 0)
                        playerOneEquity += results[0];
                        playerTwoEquity += results[1];
                        tieEquity += results[2];
                        hands++;
                    }
                }
            }
        }
        double[] toReturn = new double[3];
        toReturn[0] = playerOneEquity/hands;
        toReturn[1] = playerTwoEquity/hands;
        toReturn[2] = tieEquity/hands;

        // Average equity calculation
        return toReturn;
    }

    /**
     * Helper method to parse a hand (2 cards) from a range table code.
     *
     * @param rangeCode The shorthand for the poker hand in the range table.
     * @return An array of 2 cards or null if parsing fails.
     */
    private Card[] parseHandFromRangeCode(String rangeCode) {
        // Example: "AKs" means Ace of Spades & King of Spades (suited)
        // Example: "98o" means 9 & 8 off-suit (different suits)
        try {
            int rank1 = getRankFromChar(rangeCode.charAt(0));
            int rank2 = getRankFromChar(rangeCode.charAt(1));
            boolean suited = rangeCode.length() == 3 && rangeCode.charAt(2) == 's';

            // Generate the two cards based on suited/offsuit
            for (int suit1 = 0; suit1 <= 3; suit1++) {
                for (int suit2 = 0; suit2 <= 3; suit2++) {
                    if ((suited && suit1 != suit2) || (!suited && suit1 == suit2)) {
                        continue;
                    }
                    Card card1 = dealCard(rank1, suit1);
                    Card card2 = dealCard(rank2, suit2);

                    if (card1 != null && card2 != null) {
                        return new Card[]{card1, card2};
                    }
                }
            }
        } catch (Exception e) {
            // Parsing failed, return null
        }
        return null;
    }

    /**
     * Helper method to check if a given hand is valid (no overlap with other cards in play).
     *
     * @param hand           The hand to validate.
     * @param myHand         Your current hand.
     * @param communityCards The community cards on the table.
     * @return True if the hand is valid, false otherwise.
     */
    private boolean isHandValid(Card[] hand, Card[] myHand, List<Card> communityCards) {
        Set<Card> usedCards = new HashSet<>();
        usedCards.addAll(Arrays.asList(myHand));
        usedCards.addAll(communityCards);

        for (Card card : hand) {
            if (usedCards.contains(card)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method to convert a rank character into an integer.
     *
     * @param c The character representing the rank ('2'-'9', 'T', 'J', 'Q', 'K', 'A').
     * @return The integer rank.
     */
    private int getRankFromChar(char c) {
        if (c >= '2' && c <= '9') {
            return c - '0';
        }
        switch (c) {
            case 'T':
                return 10;
            case 'J':
                return 11;
            case 'Q':
                return 12;
            case 'K':
                return 13;
            case 'A':
                return 14;
            default:
                throw new IllegalArgumentException("Invalid rank character: " + c);
        }
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

    public List<Player> getPlayers() {
        return players;
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.runSim();
    }

    public void addCommunityCard(int rank, int suit){
        communityCards.add(dealCard(rank, suit));
    }
}