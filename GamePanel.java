import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;


import javax.swing.JPanel;

public class GamePanel extends JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;
	
	static final int WIDTH = 500;
	static final int HEIGHT = 500;
	static final int UNIT_SIZE = 20;
	static final int NUMBER_OF_UNITS = (WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

	// hold x and y coordinates for body parts of the snake
	final int x[] = new int[NUMBER_OF_UNITS];
	final int y[] = new int[NUMBER_OF_UNITS];
	
	// initial length of the snake
	int length = 5;
	int foodEaten;
	int foodX;
	int foodY;
	int[] obstaclesX;
	int[] obstaclesY;
	int numObstacles = 0;
	int[] poisonFoodX;
    int[] poisonFoodY;
    int numPoisonFood = 0;
	char direction = 'D';
	boolean running = false;
	Random random;
	Timer timer;
	String difficulty;
	
	GamePanel(String difficulty) {
		this.difficulty = difficulty;
		random = new Random();
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.setBackground(Color.DARK_GRAY);
		this.setFocusable(true);
		this.addKeyListener(new MyKeyAdapter());
		play();
	}	
	
	public void play() {
		addFood();
		running = true;
		
		int delay = 100;
		switch (difficulty) {
			case Difficulty.Easy:
				delay=150;
				numObstacles = 2;
				obstaclesX = new int[numObstacles];
				obstaclesY = new int[numObstacles];
				numPoisonFood = 1;
				break;
			case Difficulty.Medium:
				delay=100;
				numObstacles = 5;
				obstaclesX = new int[numObstacles];
				obstaclesY = new int[numObstacles];
				numPoisonFood = 2;
				break;
			case Difficulty.Hard:
				delay=50;
				numObstacles = 10;
				obstaclesX = new int[numObstacles];
				obstaclesY = new int[numObstacles];
				numPoisonFood = 3;
				break;
		}
		poisonFoodX = new int[numPoisonFood];
        poisonFoodY = new int[numPoisonFood];
		addObstacles();
		addPoisonFood();
		timer = new Timer(delay, this);
		timer.start();	
	}
	
	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		draw(graphics);
	}
	
	public void move() {
		for (int i = length; i > 0; i--) {
			// shift the snake one unit to the desired direction to create a move
			x[i] = x[i-1];
			y[i] = y[i-1];
		}
		
		if (direction == 'L') {
			x[0] = x[0] - UNIT_SIZE;
		} else if (direction == 'R') {
			x[0] = x[0] + UNIT_SIZE;
		} else if (direction == 'U') {
			y[0] = y[0] - UNIT_SIZE;
		} else {
			y[0] = y[0] + UNIT_SIZE;
		}	
	}
	
	public void checkFood() {
		if(x[0] == foodX && y[0] == foodY) {
			length++;
			foodEaten++;
			addFood();
		}

		for (int i = 0; i < numPoisonFood; i++) {
            if (x[0] == poisonFoodX[i] && y[0] == poisonFoodY[i]) {
                foodEaten--;
                if (foodEaten < 0) {
                    running = false;
                } else {
                    poisonFoodX[i] = random.nextInt((int) (WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                    poisonFoodY[i] = random.nextInt((int) (HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
                }
            }
        }
	}
	
	public void draw(Graphics graphics) {
		
		if (running) {
			//draws the food
			graphics.setColor(new Color(210, 115, 90));
			graphics.fillOval(foodX, foodY, UNIT_SIZE, UNIT_SIZE);

			//draws the obstacles
			for(int i = 0; i < numObstacles; i++){
				graphics.setColor(Color.CYAN);
				graphics.fillOval(obstaclesX[i], obstaclesY[i], UNIT_SIZE, UNIT_SIZE);
			}

			// Draw poisonous food
			for (int i = 0; i < numPoisonFood; i++) {
                graphics.setColor(Color.RED);
                graphics.fillOval(poisonFoodX[i], poisonFoodY[i], UNIT_SIZE, UNIT_SIZE);
            }
			
			//draws the head
			graphics.setColor(Color.white);
			graphics.fillRect(x[0], y[0], UNIT_SIZE, UNIT_SIZE);

			//draws the body
			for (int i = 1; i < length; i++) {
				graphics.setColor(new Color(40, 200, 150));
				graphics.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
			}
			
			//draws the scoreboard
			graphics.setColor(Color.white);
			graphics.setFont(new Font("Sans serif", Font.ROMAN_BASELINE, 25));
			FontMetrics metrics = getFontMetrics(graphics.getFont());
			graphics.drawString("Score: " + foodEaten, (WIDTH - metrics.stringWidth("Score: " + foodEaten)) / 2, graphics.getFont().getSize());
		
		} else {
			gameOver(graphics);
		}
	}
	
	public void addFood() {
		foodX = random.nextInt((int)(WIDTH / UNIT_SIZE))*UNIT_SIZE;
		foodY = random.nextInt((int)(HEIGHT / UNIT_SIZE))*UNIT_SIZE;
	}
	public void addObstacles(){
		//creates the # of obstacles
		for(int i = 0; i < numObstacles; i++){
			boolean made;
			do{
				//creates the coords for the obstacle
				made = false;
				int x = random.nextInt((int)(WIDTH / UNIT_SIZE))*UNIT_SIZE;
				int y = random.nextInt((int)(WIDTH / UNIT_SIZE))*UNIT_SIZE;
				//make sure the obstacle that was made isnt already made
				for(int j = 0; j<i ; j++){
					//if it is then it stops and restarts the loop
					if (x == obstaclesX[j] && y == obstaclesY[j]) {
						made = true;
						break;
					}
				}
				
				obstaclesX[i] = x; //X
				obstaclesY[i] = y; //Y
				//need to check if the spot for the obstacle isnt on the food
				//or if its not on top on another obstacle
			}while((obstaclesX[0] == foodX && obstaclesY[0] == foodY) || made);
		}
	}

	public void addPoisonFood() {
        for (int i = 0; i < numPoisonFood; i++) {
            boolean made;
            do {
                made = false;
                int x = random.nextInt((int) (WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                int y = random.nextInt((int) (WIDTH / UNIT_SIZE)) * UNIT_SIZE;
                for (int j = 0; j < i; j++) {
                    if (x == poisonFoodX[j] && y == poisonFoodY[j]) {
                        made = true;
                        break;
                    }
                }
                poisonFoodX[i] = x;
                poisonFoodY[i] = y;
            } while ((poisonFoodX[0] == foodX && poisonFoodY[0] == foodY) || made);
        }
    }
	
	public void checkHit() {
		// check if head run into its body
		for (int i = length; i > 0; i--) {
			if (x[0] == x[i] && y[0] == y[i]) {
				running = false;
			}
		}
		//makes sure the head of the snake doesnt run into a obstacle
		for (int i = 0; i < numObstacles; i++){
			if (obstaclesX[i] == x[i] && obstaclesY[i] == y[i]){
				running = false;
			}
		}
		
		// check if head run into walls
		if (x[0] < 0 || x[0] > WIDTH || y[0] < 0 || y[0] > HEIGHT) {
			running = false;
		}
		
		if(!running) {
			timer.stop();
		}
	}
	
	public void gameOver(Graphics graphics) {
		//game over screen
		graphics.setColor(Color.red);
		graphics.setFont(new Font("Sans serif", Font.ROMAN_BASELINE, 50));
		FontMetrics metrics = getFontMetrics(graphics.getFont());
		graphics.drawString("Game Over", (WIDTH - metrics.stringWidth("Game Over")) / 2, HEIGHT / 2);
		
		graphics.setColor(Color.white);
		graphics.setFont(new Font("Sans serif", Font.ROMAN_BASELINE, 25));
		metrics = getFontMetrics(graphics.getFont());
		graphics.drawString("Score: " + foodEaten, (WIDTH - metrics.stringWidth("Score: " + foodEaten)) / 2, graphics.getFont().getSize());

	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (running) {
			move();
			checkFood();
			checkHit();
		}
		repaint();
	}
	
	public class MyKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			switch(e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
					if (direction != 'R') {
						direction = 'L';
					}
					break;
					
				case KeyEvent.VK_RIGHT:
					if (direction != 'L') {
						direction = 'R';
					}
					break;
					
				case KeyEvent.VK_UP:
					if (direction != 'D') {
						direction = 'U';
					}
					break;
					
				case KeyEvent.VK_DOWN:
					if (direction != 'U') {
						direction = 'D';
					}
					break;		
			}
		}
	}
}

