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
	// Coordinate for food/powerUp
	int foodX, foodY;
	int shieldX, shieldY = -1;
	int slowMoX, slowMoY = -1;
	int pointMultiX, pointMultiY = -1;
	// boolean indicates if current powerup is active
	boolean pointMulti = false;
	boolean pointMultied = false;
	boolean shield = false;
	boolean shielded = false;
	boolean slowMo = false;
	// List of obstacles Coordinates
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
	Timer shieldTime;
	Timer slowMoTimer;
	Timer pointMultiTimer;
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

	void countDown(int num) {
		final long[] startTime = {-1};
		long duration = switch (difficulty){
			case Difficulty.Easy -> 6000;
			case Difficulty.Medium -> 4000;
			case Difficulty.Hard -> 2000;
			default -> 0;
		};

		switch (num){
			case 0:
				shieldTime = new Timer(10, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (startTime[0] < 0) {
							startTime[0] = System.currentTimeMillis();
						}
						// Using internal system time to check if it passed the time limit
						long now = System.currentTimeMillis();
						long clockTime = now - startTime[0];
						if (clockTime >= duration) {
							// After time pass it will get rid of shield and stop the timer
							shielded = false;
							shieldTime.stop();
						}
					}
				});
				break;
			case 1:
				slowMoTimer = new Timer(10, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (startTime[0] < 0) {
							startTime[0] = System.currentTimeMillis();
						}
						long now = System.currentTimeMillis();
						long clockTime = now - startTime[0];
						if (clockTime >= duration) {
							// After time pass it will return the snake's original speed and stop the timer
							timer.setDelay(timer.getInitialDelay());
							slowMoTimer.stop();
						}
					}
				});
				break;
			case 2:
				long multiplierDuration = duration * 2;
				pointMultiTimer = new Timer(10, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (startTime[0] < 0) {
							startTime[0] = System.currentTimeMillis();
						}
						long now = System.currentTimeMillis();
						long clockTime = now - startTime[0];
						if (clockTime >= multiplierDuration) {
							// After time pass it will get rid of point multiplier and stop the timer
							pointMultied = false;
							pointMultiTimer.stop();
						}
					}
				});
				break;
		}
	}
	public void play() {
		addFood();
		addShield();
		addPointMulti();
		// In easy mode there won't be slow-mo
		if (!(difficulty.equals(Difficulty.Easy))) addSlowMo();
		countDown(0);
		countDown(1);
		countDown(2);
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

	// After eating food it will spawn a new one but also have a chance of spawning power ups
	public void checkFood() {
		if(x[0] == foodX && y[0] == foodY) {
			length++;
			if (pointMultied){
				foodEaten+=2;
			} else {
				foodEaten++;
			}
			addFood();
			if (!(shield || shielded)){
				addShield();
			}
			if (!slowMo && !difficulty.equals(Difficulty.Easy)){
				addSlowMo();
			}
			if (!(pointMulti || pointMultied)){
				addPointMulti();
			}
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

	// This checks if snake ate a power up
	public void checkPowerUp() throws InterruptedException{
		if(x[0] == shieldX && y[0] == shieldY) {
			shielded = true;
			shield = false;
			shieldX = -1;
			shieldY = -1;
			if (!shieldTime.isRunning()) {
				countDown(0);
				shieldTime.start();
			}
		}
		if(x[0] == slowMoX && y[0] == slowMoY) {
			timer.setDelay(150);
			slowMo = false;
			slowMoX = -1;
			slowMoY = -1;
			if (!slowMoTimer.isRunning()) {
				countDown(1);
				slowMoTimer.start();
			}
		}
		if(x[0] == pointMultiX && y[0] == pointMultiY) {
			pointMulti = false;
			pointMultied = true;
			pointMultiX = -1;
			pointMultiY = -1;
			if (!pointMultiTimer.isRunning()) {
				countDown(2);
				pointMultiTimer.start();
			}
		}
	}

	public void draw(Graphics graphics) {

		if (running) {
			//draws the food
			graphics.setColor(new Color(210, 115, 90));
			graphics.fillOval(foodX, foodY, UNIT_SIZE, UNIT_SIZE);

			// draws power up if available
			if (shield){
				graphics.setColor(new Color(65,105,225));
				graphics.fillOval(shieldX, shieldY, UNIT_SIZE, UNIT_SIZE);
			}
			if (slowMo){
				graphics.setColor(new Color(254,0,0));
				graphics.fillOval(slowMoX, slowMoY, UNIT_SIZE, UNIT_SIZE);
			}
			if (pointMulti){
				graphics.setColor(Color.YELLOW);
				graphics.fillOval(pointMultiX, pointMultiY, UNIT_SIZE, UNIT_SIZE);
			}


			//draws the obstacles
			for(int i = 0; i < numObstacles; i++){
				graphics.setColor(Color.CYAN);
				graphics.fillRect(obstaclesX[i], obstaclesY[i], UNIT_SIZE, UNIT_SIZE);
			}

			// Draw poisonous food
			for (int i = 0; i < numPoisonFood; i++) {
                graphics.setColor(new Color(138,43,226));
                graphics.fillOval(poisonFoodX[i], poisonFoodY[i], UNIT_SIZE, UNIT_SIZE);
            }
			
			//draws the head
			if (pointMultied){
				graphics.setColor(Color.YELLOW);
			} else {
				graphics.setColor(Color.white);
			}
			graphics.fillRect(x[0], y[0], UNIT_SIZE, UNIT_SIZE);


			//draws the body
			if (shielded){
				for (int i = 1; i < length; i++) {
					graphics.setColor(new Color(65,105,225));
					graphics.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
				}
			} else {
				for (int i = 1; i < length; i++) {
					graphics.setColor(new Color(40, 200, 150));
					graphics.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
				}
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

	// Generate X and Y where obstacles aren't at
	public int[] generateDot(){
		int x = random.nextInt((int)(WIDTH / UNIT_SIZE))*UNIT_SIZE;
		int y = random.nextInt((int)(WIDTH / UNIT_SIZE))*UNIT_SIZE;
		while (true){
			boolean flag = true;
			for (int i = 0; i < numObstacles; i++){
				if (obstaclesX[i] == foodX && obstaclesY[i] == foodY){
					x = random.nextInt((int)(WIDTH / UNIT_SIZE))*UNIT_SIZE;
					y = random.nextInt((int)(HEIGHT / UNIT_SIZE))*UNIT_SIZE;
					flag = false;
					break;
				}

			}
			if (flag){
				break;
			}
		}
		return new int[]{x, y};
	}
	public void addFood() {
		int[] coordinate = generateDot();
		foodX = coordinate[0];
		foodY = coordinate[1];
	}

	public void addShield(){

		int frequent = switch (difficulty) {
            case Difficulty.Easy -> 3;
            case Difficulty.Medium -> 6;
            case Difficulty.Hard -> 12;
            default -> 0;
        };
        if (random.nextInt(frequent) == 1){
			int[] coordinate = generateDot();
			shieldX = coordinate[0];
			shieldY = coordinate[1];
			// Indicate there is a shield on the board
			shield = true;
		} else {
			shield = false;
		}
	}
	public void addSlowMo(){
		int frequent = switch (difficulty) {
			case Difficulty.Easy -> 3;
			case Difficulty.Medium -> 6;
			case Difficulty.Hard -> 12;
			default -> 0;
		};
		if (random.nextInt(frequent) == 0){
			int[] coordinate = generateDot();
			slowMoX = coordinate[0];
			slowMoY = coordinate[1];
			// Indicate there is a slowmo on the board
			slowMo = true;
		} else {
			slowMo = false;
		}
	}
	public void addPointMulti(){
		int frequent = switch (difficulty) {
			case Difficulty.Easy -> 3;
			case Difficulty.Medium -> 6;
			case Difficulty.Hard -> 4;
			default -> 0;
		};
		if (random.nextInt(frequent) == 2){
			int[] coordinate = generateDot();
			pointMultiX = coordinate[0];
			pointMultiY = coordinate[1];
			// Indicate there is a point multiplier on the board
			pointMulti = true;
		} else {
			pointMulti = false;
		}
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
				// If snake has shield power up, it can hit itself once
				if (shielded){
					shielded = false;
				} else {
					running = false;
				}
			}
		}

		//makes sure the head of the snake doesnt run into a obstacle
		for (int i = 0; i < numObstacles; i++){
			if (obstaclesX[i] == x[i] && obstaclesY[i] == y[i]){
				// If snake has shield power up, it can hit an obstacle once
				if (shielded){
					shielded = false;
				} else {
					running = false;
				}
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
            try {
				checkPowerUp();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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

