import javax.swing.*;

public class GameFrame extends JFrame{

	private static final long serialVersionUID = 1L;

	GameFrame() {
		String difficulty = selectDifficulty();
		GamePanel panel = new GamePanel(difficulty);
		this.add(panel);
		this.setTitle("snake");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.pack();
		this.setVisible(true);
		this.setLocationRelativeTo(null);
	}

	private String selectDifficulty() {
        String[] options = {Difficulty.Easy, Difficulty.Medium, Difficulty.Hard};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Select Difficulty: ",
                "Difficulty",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);
		// Return String of difficulty chosen
        if (choice >= 0 && choice < options.length) {
            return options[choice];
        } else {
            return Difficulty.Medium; // Default to MEDIUM if somehow no option is selected
        }
    }
}
