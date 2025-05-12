import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class EquityCalculatorGUI {
    private JFrame frame;
    JLabel title;
    private JPanel cardGridPanel;
    private JButton[][] cardButtons;
    private List<int[]> selectedCards = new ArrayList<>();
    private JLabel selectedCardsLabel;
    private Game game;
    private JLabel player1HandLabel;
    private JLabel player2HandLabel;
    private JLabel communityCardsLabel;
    private static String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    private static String[] suits = {"♠", "♥", "♦", "♣"};
    private boolean[][] cardSelected; // Tracks permanently selected cards

    public EquityCalculatorGUI() {
        game = new Game();
        frame = new JFrame("Poker Equity Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.setLayout(new BorderLayout());

        cardSelected = new boolean[ranks.length][suits.length]; // Initialize tracking array

        // Add title to the window
        addConfigurableText("Poker Equity Calculator", 20, 400, 20);

        // Add labels for player hands
        createPlayerHandLabels();

        // Add label to show selected cards
        createSelectedCardsLabel();

        // Create a grid for the cards
        createCardGrid();

        frame.setVisible(true);
    }

    private void createCardGrid() {
        cardGridPanel = new JPanel(new GridLayout(ranks.length, suits.length)); // 13 rows x 4 columns
        cardButtons = new JButton[ranks.length][suits.length];
        for (int i = 12; i >= 0; i--) { // Start at Ace (index 12) and go to 2 (index 0)
            for (int j = 0; j < suits.length; j++) {
                int[] cardIndex = {i, j}; // Save row and column indices
                JButton cardButton = new JButton(ranks[i] + suits[j]); // Button label: Rank + Suit
                cardButton.setFocusPainted(false);
                cardButton.addActionListener(new CardButtonListener(cardIndex));
                cardButtons[i][j] = cardButton;
                cardGridPanel.add(cardButton);
            }
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int width = frame.getWidth() * 2 / 3;   // Example: 1/4th of screen width
        int height = frame.getHeight() / 2; // Example: 1/4th of screen height
        cardGridPanel.setSize(new Dimension(width, height));
        cardGridPanel.setLocation(frame.getWidth() / 2 - width/2, 600 - height/2);

        frame.setLayout(null);
        frame.add(cardGridPanel);
    }

    private void createSelectedCardsLabel() {
        selectedCardsLabel = new JLabel("Selected Cards: ");
        selectedCardsLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        frame.add(selectedCardsLabel, BorderLayout.SOUTH);
    }

    private void addConfigurableText(String text, int fontSize, int xPosition, int yPosition) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, fontSize));
        label.setBounds(xPosition, yPosition, text.length() * fontSize, fontSize + 10);
        frame.setLayout(null);
        frame.add(label);
        frame.revalidate();
    }

    /**
     * This method handles the process of selecting a specified number of cards asynchronously.
     *
     * @param numberOfCards The number of cards to select.
     * @param callback      Callback to execute when the specified number of cards have been selected.
     */
    public void selectCards(int numberOfCards, SelectionCallback callback) {
        selectedCards.clear(); // Clear any existing selections
        updateSelectedCardsLabel();

        // Enable all buttons except permanently disabled cards
        for (int i = 0; i < cardButtons.length; i++) {
            for (int j = 0; j < cardButtons[i].length; j++) {
                if (!cardSelected[i][j]) { // Only enable cards that haven't been permanently selected
                    cardButtons[i][j].setEnabled(true);
                }
            }
        }

        // Use a thread to monitor the selection asynchronously
        new Thread(() -> {
            try {
                while (selectedCards.size() < numberOfCards) {
                    Thread.sleep(100); // Wait for user input
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Card selection was interrupted!", e);
            }
            // Once the desired number of cards are selected, execute the callback
            SwingUtilities.invokeLater(() -> {
                callback.onCardsSelected(selectedCards); // Pass selected cards to the callback
                resetCardButtons(); // Reset the grid for the next round
            });
        }).start();
    }

    private class CardButtonListener implements ActionListener {
        private final int[] cardInfo;

        public CardButtonListener(int[] cardInfo) {
            this.cardInfo = cardInfo;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton sourceButton = (JButton) e.getSource();

            // Prevent selecting more than the required number of cards in a session
            if (selectedCards.size() >= selectedCardsLabel.getText().length()) {
                JOptionPane.showMessageDialog(frame, "You reached the limit!");
                return;
            }

            // Prevent selecting the same card multiple times
            if (selectedCards.stream().anyMatch(card -> card[0] == cardInfo[0] && card[1] == cardInfo[1])) {
                JOptionPane.showMessageDialog(frame, "You already selected this card!");
                return;
            }

            // Prevent selecting a permanently disabled card (extra safeguard)
            if (cardSelected[cardInfo[0]][cardInfo[1]]) {
                JOptionPane.showMessageDialog(frame, "This card is no longer available!");
                return;
            }

            // Turn the button gray and disable it
            sourceButton.setBackground(Color.GRAY);
            sourceButton.setEnabled(false);

            // Mark card as permanently selected
            cardSelected[cardInfo[0]][cardInfo[1]] = true;

            // Add the card to selected list and update the label
            selectedCards.add(cardInfo);
            updateSelectedCardsLabel();
        }
    }

    private void updateSelectedCardsLabel() {
        StringBuilder labelText = new StringBuilder("Selected Cards: ");
        for (int[] card : selectedCards) {
            labelText.append("[").append(card[0]).append(", ").append(card[1]).append("] ");
        }
        selectedCardsLabel.setText(labelText.toString());
    }

    private void resetCardButtons() {
        for (int i = 0; i < cardButtons.length; i++) {
            for (int j = 0; j < cardButtons[i].length; j++) {
                if (!cardSelected[i][j]) { // Reset only buttons that are not permanently disabled
                    cardButtons[i][j].setBackground(null);
                    cardButtons[i][j].setEnabled(true);
                }
            }
        }
        // Clear the list of selected cards
        selectedCards.clear();
        updateSelectedCardsLabel();
    }

    public interface SelectionCallback {
        void onCardsSelected(List<int[]> cards);
    }

    private void createPlayerHandLabels() {
        // Labels for Player 1 and Player 2
        player1HandLabel = new JLabel("Player 1 Hand: ");
        setupHandLabel(player1HandLabel, new Rectangle(frame.getWidth() / 20, 75, frame.getWidth()/3, 50));
        player1HandLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center text horizontally
        player1HandLabel.setVerticalAlignment(SwingConstants.CENTER);   // Center text vertically

        player2HandLabel = new JLabel("Player 2 Hand: ");
        setupHandLabel(player2HandLabel, new Rectangle(frame.getWidth() * 19 /20 - frame.getWidth()/3, 75, frame.getWidth()/3, 50));
        player2HandLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center text horizontally
        player2HandLabel.setVerticalAlignment(SwingConstants.CENTER);   // Center text vertically

        // Label for Community Cards
        communityCardsLabel = new JLabel("Community Cards: ");
        setupHandLabel(communityCardsLabel, new Rectangle(frame.getWidth() / 20, 145, frame.getWidth() * 9 / 10, 50));
        communityCardsLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center text horizontally
        communityCardsLabel.setVerticalAlignment(SwingConstants.CENTER);   // Center text vertically

        // Revalidate UI structure
        frame.add(player1HandLabel);
        frame.add(player2HandLabel);
        frame.add(communityCardsLabel);
        frame.revalidate();
        frame.repaint();
    }

    private void setupHandLabel(JLabel label, Rectangle bounds) {
        label.setFont(new Font("Arial", Font.PLAIN, 18));
        label.setOpaque(true);
        label.setBackground(Color.LIGHT_GRAY);
        label.setBounds(bounds);
    }

    private void selectCommunityCards() {
        int numberOfCommunityCards = -1;

        // Loop until we get valid input (0-5)
        while (numberOfCommunityCards < 0 || numberOfCommunityCards > 5) {
            String input = JOptionPane.showInputDialog(frame, "How many community cards would you like to select? (0-5)");

            try {
                numberOfCommunityCards = Integer.parseInt(input);
                if (numberOfCommunityCards < 0 || numberOfCommunityCards > 5) {
                    JOptionPane.showMessageDialog(frame, "Please enter a number between 0 and 5.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid input! Please enter a number between 0 and 5.");
            }
        }

        // If they chose 0 community cards, skip selection and update UI
        if (numberOfCommunityCards == 0) {
            JOptionPane.showMessageDialog(frame, "No community cards selected!");
            communityCardsLabel.setText("Community Cards: None");
            promptForEquityCalculation(); // Ask if the user wants to calculate equity
            return;
        }

        // Message for selecting community cards
        JOptionPane.showMessageDialog(frame, "Please select " + numberOfCommunityCards + " community cards.");

        // Allow card selection for the chosen number of community cards
        selectCards(numberOfCommunityCards, communityCards -> {
            for (int[] card : communityCards) {
                game.addCommunityCard(card[0], card[1]); // Add to the game (as community cards)
            }
            // Display the selected community cards
            communityCardsLabel.setText("Community Cards: " + formatCards(communityCards));

            // Ask if the user wants to calculate equity
            promptForEquityCalculation();
        });
    }

    private void promptForEquityCalculation() {
        int response = JOptionPane.showConfirmDialog(
                frame,
                "Do you want to calculate the equity now?",
                "Calculate Equity",
                JOptionPane.YES_NO_OPTION
        );

        // If the user selects "Yes," run the simulation and display results
        if (response == JOptionPane.YES_OPTION) {
            displaySimulationResults();
        } else {
            JOptionPane.showMessageDialog(frame, "You chose not to calculate equity at this time.");
        }
    }

    private void displaySimulationResults() {
        // Run the simulation
        double[] results = game.runSim(); // Assuming results[0] is P1's win percentage, results[1] is P2's

        // Prepare the results as a formatted string
        String resultText = String.format(
                "<html><center>"
                        + "<h2>Simulation Results</h2>"
                        + "<p>Player 1 Win Percentage: %.2f%%</p>"
                        + "<p>Player 2 Win Percentage: %.2f%%</p>"
                        + "</center></html>",
                results[0], results[1]
        );

        // Create and configure the results label
        JLabel resultsLabel = new JLabel(resultText, SwingConstants.CENTER);
        resultsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        resultsLabel.setBackground(Color.LIGHT_GRAY);
        resultsLabel.setOpaque(true);
        resultsLabel.setBounds(10, 200, 960, 150); // Position and size (adjust as needed)

        // Add the results label to the frame
        frame.add(resultsLabel);

        // Update the frame to show the new label
        frame.revalidate();
        frame.repaint();
    }

    private static String formatCards(List<int[]> cards) {
        StringBuilder cardString = new StringBuilder();
        for (int[] card : cards) {
            // Get rank and suit based on card indices
            String rank = ranks[card[0]];
            String suit = suits[card[1]];
            cardString.append(rank).append(suit).append(" ");
        }
        return cardString.toString().trim(); // Remove trailing space
    }

    private void setupOpponentHandsInput() {
        // Prompt user to choose how to input opponent's hands
        String[] options = {"Manually Input Hands", "Input a Range"};
        int choice = JOptionPane.showOptionDialog(
                frame,
                "How would you like to specify Player 2's hand?",
                "Hand Setup",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        // Execute the user's choice
        if (choice == 0) {
            manuallyInputOpponentHands();
        } else if (choice == 1) {
            inputOpponentRangeUsingTable();
        } else {
            JOptionPane.showMessageDialog(frame, "No option selected. Defaulting to manual input.");
            manuallyInputOpponentHands();
        }
    }

    /**
     * Allows the user to manually input their opponent's two cards.
     */
    private void manuallyInputOpponentHands() {
        // Prompt the user for Player 2's cards
        JOptionPane.showMessageDialog(frame, "Select Player 2's cards manually.");
        selectCards(2, cards -> {
            for (int[] card : cards) {
                game.getPlayers().get(1).addCardToHand(new Card(card[0], card[1]));
            }
            // Display the selected cards for Player 2
            player2HandLabel.setText("Player 2 Hand: " + formatCards(cards));

            // Prompt the user to select community cards
            SwingUtilities.invokeLater(() -> selectCommunityCards());
        });
    }

    private void inputOpponentRangeUsingTable() {
        // Create a new frame for the range table
        JFrame rangeFrame = new JFrame("Select Opponent's Range");
        rangeFrame.setSize(800, 800);
        rangeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        rangeFrame.setLayout(new BorderLayout());

        // Create a 13x13 grid panel for the range table
        JPanel rangeTablePanel = new JPanel(new GridLayout(13, 13));
        JButton[][] rangeButtons = new JButton[13][13]; // Button grid
        boolean[][] rangeTable = new boolean[13][13];   // Tracks selected hands as a 2D array of booleans

        // Ranks for poker cards
        String[] ranks = {"A", "K", "Q", "J", "10", "9", "8", "7", "6", "5", "4", "3", "2"};

        // Populate the grid with buttons
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                String hand;
                if (i == j) {
                    hand = ranks[i] + ranks[j]; // Pocket pairs
                } else if (i < j) {
                    hand = ranks[i] + ranks[j] + "o"; // Offsuit hands
                } else {
                    hand = ranks[j] + ranks[i] + "s"; // Suited hands
                }

                // Create a button for each hand
                JButton button = new JButton(hand);

                // Initial background state
                button.setBackground(Color.LIGHT_GRAY);
                button.setOpaque(true); // Ensure the background is opaque for color to show
                button.setBorderPainted(false); // Optional: Remove borders for cleaner appearance

                final int row = i;
                final int col = j;

                // Toggle selection state and update button background on click
                button.addActionListener(e -> {
                    rangeTable[row][col] = !rangeTable[row][col]; // Toggle the selection
                    if (rangeTable[row][col]) {
                        button.setBackground(Color.GRAY); // Highlight selected button
                    } else {
                        button.setBackground(Color.LIGHT_GRAY); // Revert to unselected
                    }
                });

                // Add button to the grid panel
                rangeButtons[i][j] = button;
                rangeTablePanel.add(button);
            }
        }

        // Create a confirm button
        JButton confirmButton = new JButton("Confirm Range");
        confirmButton.addActionListener(e -> {
            // Pass the 2D boolean array to the rangeSim method
            double[] results = game.rangeSim(rangeTable, game.getPlayers().get(0).getHand()); // Passing the boolean[][] directly to the method

            // Format and display the results
            String resultText = String.format(
                    "<html><center>"
                            + "<h2>Range Simulation Results</h2>"
                            + "<p>Player 1 Win Percentage: %.2f%%</p>"
                            + "<p>Player 2 Win Percentage: %.2f%%</p>"
                            + "</center></html>",
                    results[0], results[1]
            );

            // Create and configure the results label
            JLabel resultsLabel = new JLabel(resultText, SwingConstants.CENTER);
            resultsLabel.setFont(new Font("Arial", Font.BOLD, 18));
            resultsLabel.setBackground(Color.LIGHT_GRAY);
            resultsLabel.setOpaque(true);
            resultsLabel.setBounds(10, 200, 960, 150); // Adjust position as needed

            // Add the results label to the main frame
            frame.add(resultsLabel);

            // Update the main frame to show the results
            frame.revalidate();
            frame.repaint();

            // Close the range selection frame
            rangeFrame.dispose();
        });

        // Add components to the range frame
        JScrollPane scrollPane = new JScrollPane(rangeTablePanel); // Add scroll functionality
        rangeFrame.add(scrollPane, BorderLayout.CENTER); // Grid in center
        rangeFrame.add(confirmButton, BorderLayout.SOUTH); // Confirm button at bottom

        // Validate and display the range frame
        SwingUtilities.invokeLater(() -> {
            rangeFrame.revalidate();
            rangeFrame.repaint();
            rangeFrame.setVisible(true); // Set visibility after adding components
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EquityCalculatorGUI gui = new EquityCalculatorGUI();
            JOptionPane.showMessageDialog(gui.frame, "Player 1: Please select 2 cards.");
            // Player 1 card selection
            gui.selectCards(2, player1Cards -> {
                for (int[] card : player1Cards) {
                    gui.game.getPlayers().get(0).addCardToHand(new Card(card[0], card[1]));
                }
                gui.player1HandLabel.setText("Player 1 Hand: " + formatCards(player1Cards));

                // Prompt User: Manual Input or Range Input
                String[] options = {"Manual Input", "Range Input"};
                int choice = JOptionPane.showOptionDialog(
                        gui.frame,
                        "How would you like to specify Player 2's hand?",
                        "Select Input Method",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                // Handle User's Choice
                if (choice == 0) {
                    gui.manuallyInputOpponentHands(); // Manual Input
                } else if (choice == 1) {
                    gui.inputOpponentRangeUsingTable(); // Range Input
                } else {
                    JOptionPane.showMessageDialog(gui.frame, "No valid option selected. Defaulting to manual input.");
                    gui.manuallyInputOpponentHands();
                }
            });
        });
    }

}