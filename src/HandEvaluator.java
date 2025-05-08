import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandEvaluator {
    /**
     * Evaluates a poker hand and returns a numeric score representing its strength.
     * Higher score means stronger hand. Uses player's two hole cards plus community cards.
     *
     * @param playerHand The player's two hole cards
     * @param communityCards The community cards (up to 5)
     * @return A long representing the hand's strength
     */
    public static long evaluateHand(Card[] playerHand, List<Card> communityCards) {
        // Combine hole cards and community cards
        List<Card> allCards = new ArrayList<>();
        // Add player cards, filtering out nulls
        for (Card card : playerHand) {
            if (card != null) {
                allCards.add(card);
            }
        }
        // Add community cards, filtering out nulls
        for (Card card : communityCards) {
            if (card != null) {
                allCards.add(card);
            }
        }

        // Sort cards by rank in descending order
        allCards.sort(Comparator.comparing((Card c) -> c.rank).reversed());

        // Create a copy of the cards to mark as used
        boolean[] used = new boolean[allCards.size()];

        // Start evaluating from highest hand type
        long score = 0;

        // Check for straight flush
        score = checkStraightFlush(allCards, used);
        if (score > 0) {
            return score * 1_00_00_00_00_00_00_00_00L;
        }

        // Reset used cards for next check
        Arrays.fill(used, false);

        // Check for four of a kind
        score = checkFourOfAKind(allCards, used);
        if (score > 0) {
            // Add high card for kicker
            return score * 1_00_00_00_00_00_00_00L + addHighestUnusedCard(allCards, used);
        }

        // Reset used cards
        Arrays.fill(used, false);

        // Check for full house
        score = checkFullHouse(allCards, used);
        if (score > 0) {
            return score * 1_00_00_00_00_00_0L;
        }

        // Reset used cards
        Arrays.fill(used, false);

        // Check for flush
        score = checkFlush(allCards, used);
        if (score > 0) {
            return score * 1_00_00_00_00_00L;
        }

        // Reset used cards
        Arrays.fill(used, false);

        // Check for straight
        score = checkStraight(allCards, used);
        if (score > 0) {
            return score * 1_00_00_00_00L;
        }

        // Reset used cards
        Arrays.fill(used, false);

        // Check for three of a kind
        score = checkThreeOfAKind(allCards, used);
        if (score > 0) {
            // Add two high cards for kickers
            long kicker1 = addHighestUnusedCard(allCards, used);
            long kicker2 = addHighestUnusedCard(allCards, used);
            score = score * 1_00_00_00L + kicker1 + kicker2;
            return score;
        }

        // Reset used cards
        Arrays.fill(used, false);

        // Check for two pair
        score = checkTwoPair(allCards, used);
        if (score > 0) {
            // Add one high card for kicker
            score = score * 1_00_0L + addHighestUnusedCard(allCards, used);
            return score;
        }

        // Reset used cards
        Arrays.fill(used, false);

        // Check for one pair
        score = checkOnePair(allCards, used);
        if (score > 0) {
            // Add three high cards for kickers
            long kicker1 = addHighestUnusedCard(allCards, used);
            long kicker2 = addHighestUnusedCard(allCards, used);
            long kicker3 = addHighestUnusedCard(allCards, used);
            score = score * 1_00L + kicker1 + kicker2 + kicker3;
            return score;
        }

        // If nothing else, check for high card
        return checkHighCard(allCards);
    }

    /**
     * Checks for a straight flush and returns its value, or 0 if none
     */
    private static long checkStraightFlush(List<Card> cards, boolean[] used) {
        // Check for straight flush in each suit
        for (int suit = 0; suit < 4; suit++) {
            // Get all cards of this suit
            List<Card> sameSuit = new ArrayList<>();
            for (Card card : cards) {
                if (card.suit == suit) {
                    sameSuit.add(card);
                }
            }

            // Need at least 5 cards of same suit for a straight flush
            if (sameSuit.size() < 5) {
                continue;
            }

            // Sort by rank
            sameSuit.sort(Comparator.comparing((Card c) -> c.rank).reversed());

            // Check for straight (considering Ace can be low)
            for (int i = 0; i <= sameSuit.size() - 5; i++) {
                if (isStraight(sameSuit.subList(i, sameSuit.size()))) {
                    // Mark cards as used
                    for (int j = 0; j < 5; j++) {
                        Card card = sameSuit.get(i + j);
                        int index = cards.indexOf(card);
                        used[index] = true;
                    }

                    // Value is the high card of the straight
                    int highCard = sameSuit.get(i).rank;

                    // Special case: A-5 straight (Ace is low)
                    if (highCard == 14 && sameSuit.get(i + 1).rank == 5) {
                        return 5; // A-5 straight flush is valued as 5-high
                    }

                    return highCard - 4; // Subtract 4 to get the correct value (e.g., 6-high straight = 02)
                }
            }

            // Check special case: A-5 straight flush (where Ace is low)
            if (hasAceLowStraight(sameSuit)) {
                // Mark cards as used
                for (Card card : sameSuit) {
                    if (card.rank == 14 || card.rank <= 5) {
                        int index = cards.indexOf(card);
                        used[index] = true;
                    }
                }
                return 1; // A-5 straight flush is the lowest (01)
            }
        }

        return 0; // No straight flush
    }

    /**
     * Checks if a sorted list of cards contains a straight
     */
    private static boolean isStraight(List<Card> sortedCards) {
        if (sortedCards.size() < 5) return false;

        int count = 1;
        int lastRank = sortedCards.get(0).rank;

        for (int i = 1; i < sortedCards.size() && count < 5; i++) {
            int currentRank = sortedCards.get(i).rank;

            if (currentRank == lastRank - 1) {
                count++;
                lastRank = currentRank;
                if (count == 5) return true;
            } else if (currentRank != lastRank) { // Skip duplicates
                count = 1;
                lastRank = currentRank;
            }
        }

        return false;
    }

    /**
     * Checks for Ace-low straight (A-2-3-4-5)
     */
    private static boolean hasAceLowStraight(List<Card> cards) {
        boolean hasAce = false;
        boolean has2 = false;
        boolean has3 = false;
        boolean has4 = false;
        boolean has5 = false;

        for (Card card : cards) {
            if (card.rank == 14) hasAce = true;
            else if (card.rank == 2) has2 = true;
            else if (card.rank == 3) has3 = true;
            else if (card.rank == 4) has4 = true;
            else if (card.rank == 5) has5 = true;
        }

        return hasAce && has2 && has3 && has4 && has5;
    }

    /**
     * Checks for four of a kind and returns its value, or 0 if none
     */
    private static long checkFourOfAKind(List<Card> cards, boolean[] used) {
        Map<Integer, Integer> rankCount = countRanks(cards);

        for (Map.Entry<Integer, Integer> entry : rankCount.entrySet()) {
            int rank = entry.getKey();
            int count = entry.getValue();

            if (count == 4) {
                // Mark the four cards as used
                for (int i = 0; i < cards.size(); i++) {
                    if (cards.get(i).rank == rank) {
                        used[i] = true;
                    }
                }
                return rank;
            }
        }

        return 0; // No four of a kind
    }

    /**
     * Checks for full house and returns its value, or 0 if none
     */
    private static long checkFullHouse(List<Card> cards, boolean[] used) {
        Map<Integer, Integer> rankCount = countRanks(cards);
        int threeOfAKindRank = 0;
        int pairRank = 0;

        // First find highest three of a kind
        for (Map.Entry<Integer, Integer> entry : rankCount.entrySet()) {
            int rank = entry.getKey();
            int count = entry.getValue();

            if (count >= 3 && rank > threeOfAKindRank) {
                threeOfAKindRank = rank;
            }
        }

        if (threeOfAKindRank == 0) {
            return 0; // No three of a kind, can't have a full house
        }

        // Mark the three cards as used
        int threeCount = 0;
        for (int i = 0; i < cards.size() && threeCount < 3; i++) {
            if (cards.get(i).rank == threeOfAKindRank) {
                used[i] = true;
                threeCount++;
            }
        }

        // Then find highest pair excluding three of a kind rank
        for (Map.Entry<Integer, Integer> entry : rankCount.entrySet()) {
            int rank = entry.getKey();
            int count = entry.getValue();

            if (rank != threeOfAKindRank && count >= 2 && rank > pairRank) {
                pairRank = rank;
            }
        }

        if (pairRank == 0) {
            return 0; // No pair to complete the full house
        }

        // Mark the pair as used
        int pairCount = 0;
        for (int i = 0; i < cards.size() && pairCount < 2; i++) {
            if (!used[i] && cards.get(i).rank == pairRank) {
                used[i] = true;
                pairCount++;
            }
        }

        // Value is three of a kind rank * 100 + pair rank
        return threeOfAKindRank * 14L + pairRank;
    }

    /**
     * Checks for flush and returns its value, or 0 if none
     */
    private static long checkFlush(List<Card> cards, boolean[] used) {
        int[] suitCounts = new int[4];

        // Count cards by suit
        for (Card card : cards) {
            suitCounts[card.suit]++;
        }

        // Find the suit with at least 5 cards
        int flushSuit = -1;
        for (int suit = 0; suit < 4; suit++) {
            if (suitCounts[suit] >= 5) {
                flushSuit = suit;
                break;
            }
        }

        if (flushSuit == -1) {
            return 0; // No flush
        }

        // Get all cards of the flush suit
        List<Card> flushCards = new ArrayList<>();
        for (Card card : cards) {
            if (card.suit == flushSuit) {
                flushCards.add(card);
            }
        }

        // Sort by rank (should already be sorted but just to be sure)
        flushCards.sort(Comparator.comparing((Card c) -> c.rank).reversed());

        // Mark the top 5 cards as used
        long value = 0;
        for (int i = 0; i < 5; i++) {
            Card card = flushCards.get(i);
            int index = cards.indexOf(card);
            used[index] = true;

            // Encode the rank in the value (higher digits for higher cards)
            value += card.rank;
        }

        return value;
    }

    /**
     * Checks for straight and returns its value, or 0 if none
     */
    private static long checkStraight(List<Card> cards, boolean[] used) {
        // Remove duplicate ranks for straight checking
        List<Card> distinctRanks = new ArrayList<>();
        int lastRank = -1;

        for (Card card : cards) {
            if (card.rank != lastRank) {
                distinctRanks.add(card);
                lastRank = card.rank;
            }
        }

        // Check for straight
        for (int i = 0; i <= distinctRanks.size() - 5; i++) {
            if (distinctRanks.get(i).rank == distinctRanks.get(i + 4).rank + 4) {
                // Value is the high card of the straight
                int highCard = distinctRanks.get(i).rank;
                return highCard;
            }
        }

        // Check for A-5 straight (Ace is low)
        if (hasAceLowStraightDistinct(distinctRanks)) {
            return 5; // A-5 straight is valued as 5-high
        }

        return 0; // No straight
    }

    /**
     * Checks for Ace-low straight (A-2-3-4-5) in cards with distinct ranks
     */
    private static boolean hasAceLowStraightDistinct(List<Card> distinctCards) {
        boolean hasAce = false;
        boolean has2 = false;
        boolean has3 = false;
        boolean has4 = false;
        boolean has5 = false;

        for (Card card : distinctCards) {
            if (card.rank == 14) hasAce = true;
            else if (card.rank == 2) has2 = true;
            else if (card.rank == 3) has3 = true;
            else if (card.rank == 4) has4 = true;
            else if (card.rank == 5) has5 = true;
        }

        return hasAce && has2 && has3 && has4 && has5;
    }

    /**
     * Checks for three of a kind and returns its value, or 0 if none
     */
    private static long checkThreeOfAKind(List<Card> cards, boolean[] used) {
        Map<Integer, Integer> rankCount = countRanks(cards);

        // Find highest three of a kind
        int bestRank = 0;
        for (Map.Entry<Integer, Integer> entry : rankCount.entrySet()) {
            int rank = entry.getKey();
            int count = entry.getValue();

            if (count >= 3 && rank > bestRank) {
                bestRank = rank;
            }
        }

        if (bestRank == 0) {
            return 0; // No three of a kind
        }

        // Mark the three cards as used
        int count = 0;
        for (int i = 0; i < cards.size() && count < 3; i++) {
            if (cards.get(i).rank == bestRank) {
                used[i] = true;
                count++;
            }
        }

        return bestRank;
    }

    /**
     * Checks for two pair and returns its value, or 0 if none
     */
    private static long checkTwoPair(List<Card> cards, boolean[] used) {
        Map<Integer, Integer> rankCount = countRanks(cards);
        List<Integer> pairs = new ArrayList<>();

        // Find all pairs
        for (Map.Entry<Integer, Integer> entry : rankCount.entrySet()) {
            int rank = entry.getKey();
            int count = entry.getValue();

            if (count >= 2) {
                pairs.add(rank);
            }
        }

        // Sort pairs by rank (descending)
        pairs.sort(Comparator.reverseOrder());

        if (pairs.size() < 2) {
            return 0; // Less than two pairs
        }

        // Take the two highest pairs
        int highPair = pairs.get(0);
        int lowPair = pairs.get(1);

        // Mark the four cards as used
        for (int rank : new int[]{highPair, lowPair}) {
            int pairCount = 0;
            for (int i = 0; i < cards.size() && pairCount < 2; i++) {
                if (cards.get(i).rank == rank) {
                    used[i] = true;
                    pairCount++;
                }
            }
        }

        // Value is high pair * 100 + low pair
        return highPair * 14L + lowPair;
    }

    /**
     * Checks for one pair and returns its value, or 0 if none
     */
    private static long checkOnePair(List<Card> cards, boolean[] used) {
        Map<Integer, Integer> rankCount = countRanks(cards);

        // Find highest pair
        int bestRank = 0;
        for (Map.Entry<Integer, Integer> entry : rankCount.entrySet()) {
            int rank = entry.getKey();
            int count = entry.getValue();

            if (count >= 2 && rank > bestRank) {
                bestRank = rank;
            }
        }

        if (bestRank == 0) {
            return 0; // No pair
        }

        // Mark the pair as used
        int count = 0;
        for (int i = 0; i < cards.size() && count < 2; i++) {
            if (cards.get(i).rank == bestRank) {
                used[i] = true;
                count++;
            }
        }

        return bestRank;
    }

    /**
     * Checks for high card and returns its value
     */
    private static long checkHighCard(List<Card> cards) {
        // Sort cards by rank (should already be sorted)
        cards.sort(Comparator.comparing((Card c) -> c.rank).reversed());

        // Use top 5 cards
        long value = 0;
        for (int i = 0; i < Math.min(5, cards.size()); i++) {
            value = value + cards.get(i).rank;
        }

        return value;
    }

    /**
     * Counts occurrences of each rank in the cards
     */
    private static Map<Integer, Integer> countRanks(List<Card> cards) {
        Map<Integer, Integer> rankCount = new HashMap<>();

        for (Card card : cards) {
            rankCount.put(card.rank, rankCount.getOrDefault(card.rank, 0) + 1);
        }

        return rankCount;
    }

    /**
     * Returns the highest unused card and marks it as used
     */
    private static int addHighestUnusedCard(List<Card> cards, boolean[] used) {
        for (int i = 0; i < cards.size(); i++) {
            if (!used[i]) {
                used[i] = true;
                return cards.get(i).rank;
            }
        }
        return 0; // Should not happen if there are enough cards
    }

    /**
     * Converts a hand score to a description
     */
    public static String describeHand(long score) {
        if (score >= 1_00_00_00_00_00_00_00_00L) {
            return "Straight Flush";
        } else if (score >= 1_00_00_00_00_00_00_00L) {
            return "Four of a Kind";
        } else if (score >= 1_00_00_00_00_00_00L) {
            return "Full House";
        } else if (score >= 1_00_00_00_00_00L) {
            return "Flush";
        } else if (score >= 1_00_00_00_00L) {
            return "Straight";
        } else if (score >= 1_00_00_00L) {
            return "Three of a Kind";
        } else if (score >= 1_00_00L) {
            return "Two Pair";
        } else if (score > 1_00L) {
            return "One Pair";
        } else {
            return "High Card";
        }
    }
}