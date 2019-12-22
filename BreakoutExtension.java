// Assignment 4: Breakout
//By: Javier Arevalo
//Section: Thursdays 30
//Extension: this program makes the breakout program more advanced by adding a new row every 5  
//5 turns. When the new row is added, the rest of the bricks are shifted down. The new row is 
//designed to keep the color pattern. Now the game also ends when the bricks get to close 
//to the paddle or when the user score reaches 100. The program also speeds up after 10 turns
//and the ball bounces according to where it hits the paddle. 

import acm.graphics.*;     // GOval, GRect, etc.
import acm.program.*;      // GraphicsProgram
import acm.util.*;         // RandomGenerator
import java.awt.*;         // Color
import java.awt.event.*;   // MouseEvent

public class BreakoutExtension extends BreakoutProgram {

	//This program uses more instance variables to allow it to add the new row of bricks every 5
	//turns and to be able to continue the brick color pattern. 
	GRect paddle;
	int counterHits;
	int turns = NTURNS;
	int score = 0;
	double VELOCITY_X;
	double VELOCITY_Y;
	GLabel scores;
	double spaceBefore;
	int numberOfBricks;
	GRect brick;
	//GRect brick is a instance variable so that its color can be changed when a new row is 
	//added after each 5 turns. Similar case with number of bricks, and the new row. 
	int rows;

	public void run() {
		// Set the window's title bar text
		setTitle("CS 106A Breakout");

		setCanvasSize(CANVAS_WIDTH, CANVAS_HEIGHT);

		addBricks();

		//add paddle, must be a instance variable because it updates the position as the mouse moves. 
		paddle = new GRect (0, 0, PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setLocation(getWidth()/2.0 - paddle.getWidth()/2.0, getHeight() - PADDLE_Y_OFFSET - paddle.getHeight());
		paddle.setFilled(true);
		add(paddle);

		//add scores, needs to be a instance variable so it can be updated continously throughoth the program. 
		scores = new GLabel ("Score: " + score + " ,Turns: " + turns, 0,0);
		scores.setFont(SCREEN_FONT);
		scores.setLocation(0, scores.getHeight());
		add(scores);

		//add ball
		GOval ball = new GOval (0, 0, BALL_RADIUS * 2, BALL_RADIUS * 2);
		ball.setLocation(getWidth()/2.0 - (ball.getWidth()/2.0), getHeight()/2.0 - (ball.getHeight()/2.0));
		ball.setFilled(true);
		add(ball);

		VELOCITY_Y = 5;
		getVelocityX();

		//The following code animates the ball as long as the game is still not over. If it touches any of the 
		//walls it bounces back except if it is the bottom wall, in which case another method is called. 
		//It also continuously checks if the ball collided or if the game is over. 
		while (game(ball)) {
			ball.move(VELOCITY_X, VELOCITY_Y);
			pause(DELAY);
			if (ball.getX() <= 0 || ball.getX() + ball.getWidth() >= getWidth()) {
				VELOCITY_X *= -1;
			}
			if (ball.getY() <= 0) {
				VELOCITY_Y *= -1;
			}
			if (ball.getY() + ball.getHeight() >= getHeight()) {
				game(ball);
			}
			checkForCollision(ball);
			if (score == numberOfBricks) {
				break;
			}
		}

		//if the code reaches this part, it means that the boolean game(ball) is false which means that the player ran
		//out of turns or that the score == number of bricks, which means the user won. 
		remove(ball);
		endGame();
	}

	//This method generates the random velocity for X and makes sure it is in the proper range. 
	private void getVelocityX() {
		VELOCITY_X = RandomGenerator.getInstance().nextDouble(VELOCITY_X_MIN, VELOCITY_X_MAX);
		boolean sign = RandomGenerator.getInstance().nextBoolean();
		if (sign) {
			VELOCITY_X *= -1;
		}
		//The following code assures that the random velocity for x is not in the range from -VELOCITY_X_MIN to VELOCITY_X_MIN
		while (inRange(VELOCITY_X)) {
			VELOCITY_X = RandomGenerator.getInstance().nextDouble(VELOCITY_X_MIN, VELOCITY_X_MAX);
			if (sign) {
				VELOCITY_X *= -1;
			}
		}
	}

	//The following method creates the bricks for the game.  
	private void addBricks() {
		// The following code will create the bricks and use a counter to know how many bricks were created. 
		int i = (int) (getWidth()/(BRICK_WIDTH + BRICK_SEP));
		rows = 0;
		spaceBefore = (getWidth() - (i * BRICK_WIDTH) - ( (i-1) * BRICK_SEP)) / 2.0;
		for (int h = 0; h < NBRICK_ROWS; h ++) {
			//this code creates each row
			rows++;
			for (int j = 0; j < i; j ++) {
				brick = new GRect (spaceBefore + ((BRICK_WIDTH + BRICK_SEP) * j), (BRICK_Y_OFFSET + (h * (BRICK_HEIGHT+ BRICK_SEP))), BRICK_WIDTH, BRICK_HEIGHT);
				brick.setFilled(true);
				chooseColor(rows);
				add(brick);
				numberOfBricks++;
			}
		}
	}

	//The following boolean makes sure the random velocity excludes the range specified by the hand-out. 
	private boolean inRange(double vel) {
		if (-VELOCITY_X_MIN < vel && vel < VELOCITY_X_MIN) {
			return true;
		}
		return false;
	}

	//The following method keeps accurately updating the paddle position as the mouse moves. 
	public void mouseMoved (MouseEvent event) {
		double mouseX = event.getX();
		double newX = mouseX - (paddle.getWidth()/2.0);
		if (newX >= 0 && newX + paddle.getWidth() + 1 <= getWidth()) {
			paddle.setLocation(newX, getHeight() - PADDLE_Y_OFFSET - paddle.getHeight());
		}
	}

	//The following code will check if a collision occurred, and if it did, it will pass the 
	//accurate location (corner) of where the collision occurred to react to it properly. 
	private void checkForCollision(GObject ball) {
		double X = ball.getX();
		double Y = ball.getY();
		if (getElementAt(X,Y) != null) {
			reactToCollision(X, Y);
		} else {
			if (getElementAt(X + ball.getWidth(), Y) != null) {
				reactToCollision (X + ball.getWidth(), Y);
			} else {
				if (getElementAt(X, Y + ball.getHeight()) != null) {
					reactToCollision (X, Y + ball.getHeight());
				} else {
					if (getElementAt(X + ball.getWidth(), Y + ball.getHeight()) != null) {
						reactToCollision(X + ball.getWidth(), Y + ball.getHeight());
					}
					if(getElementAt(X + ball.getWidth()/2.0, Y) != null) {
						reactToCollision(X + ball.getWidth()/2.0, Y);
					}
				}
			}
		}

	}

	//The following code will tell the ball what to do when a collision occurs. If it collides with the 
	//paddle, the ball will automatically go up. Otherwise it will update scores and change the sign of the current y velocity. 
	private void reactToCollision (double x, double y) {
		GObject collision = getElementAt(x,y);
		if (collision == paddle && collision != scores) {
			if (VELOCITY_Y > 0) {
				VELOCITY_Y *= -1;
				getProperBounceX(x);
				counterHits++;
				rows += 10;
				checkForShift();
				checkForSpeedIncrease();

			}
		} else {
			if (collision != scores) {
				score++;
				remove(collision);
				scores.setText("Score: " + score + " ,Turns: " + turns);
				//if the ball hits a brick, it will go down with a random X velocity. 
				VELOCITY_Y *= -1;
				getVelocityX();

			}
		}
	}

	//The parameter x is the x coordinate of the corner of where the collision occurred. 
	//If it hits in the left side of the paddle, the ball will go left and viceversa. 
	//This gives some ability to the user to aim where the ball is going using the paddle. 
	private void getProperBounceX(double x) {
		if (x < paddle.getX() + paddle.getWidth()/2.0) {
			getVelocityX();
			while (VELOCITY_X > 0) {
				getVelocityX();
			} 
		} else {
			if (x >= paddle.getX() + paddle.getWidth()/2.0) {
				getVelocityX();
				while (VELOCITY_X < 0) {
					getVelocityX();
				} 
			}
		}

	}

	//This method chooses the color of the bricks according to their location. 
	//It uses the remainder operation so that it "wraps around" whenever there are more than 10 rows. 
	private void chooseColor (int number) {
		if (number % 10 == 1 || number % 10 == 2) {
			brick.setColor(Color.RED);
		} else {
			if (number % 10 == 4 || number % 10 == 3) {
				brick.setColor(Color.ORANGE);
			} else {
				if (number % 10 == 5 || number % 10 == 6) {
					brick.setColor(Color.YELLOW);
				} else {
					if (number % 10 == 7 || number % 10 == 8) {
						brick.setColor(Color.GREEN);
					} else {
						if (number % 10 == 9 || number % 10 == 0) {
							brick.setColor(Color.CYAN);
						}
					}

				}
			}
		}

	}

	//This method is called whenever the ball hits the bottom wall and to check if the game is over.  If it does, 
	//the turns go down. After it checks if the player can still play, and if he does it sets the new position of the ball. 
	//In addition, the method will check if the bricks have come to close to the paddle, in which case the user lost. 
	private boolean game(GObject ball) {
		if (ball.getY() + ball.getHeight() >= getHeight()) {
			turns--;
			scores.setText("Score: " + score + " ,Turns: " + turns);
			if (turns > 0) {
				ball.setLocation(getWidth()/2.0 - (ball.getWidth()/2.0), getHeight()/2.0 - (ball.getHeight()/2.0));
				VELOCITY_X = RandomGenerator.getInstance().nextDouble(VELOCITY_X_MIN, VELOCITY_X_MAX);
			} else {
				if (turns <= 0) {
					return false;
				} else {
					if (objectAtLine()) {
						return false;
					}
				}
			}

		}
		return true;
	}

	private boolean objectAtLine() {
		int startY = (int) (getHeight() - PADDLE_Y_OFFSET - paddle.getHeight() - (BRICK_HEIGHT*2));
		for (int x = 0; x < getWidth(); x += (BRICK_WIDTH+BRICK_SEP)) {
			//if the element at this y-line is not null, it means a brick is there in which case the boolean
			//will return false so that it can end the game. 
			if (getElementAt(x, startY) != null) {
				return false;
			} 
		}
		return true;

	}

	//This method is called whenever one game ends by either eliminating all bricks or by running out of turns. 
	//Precondition: the player has no turns left OR has no bricks left to eliminate
	//Postcondition: the screen will show the user if he won or lost. 
	private void endGame() {
		if (score == numberOfBricks || score == 100){
			clearCanvas();
			add(scores);
			GLabel win = new GLabel("YOU WIN!", 0, 0);
			win.setFont(SCREEN_FONT);
			win.setLocation(getWidth()/2.0 - win.getWidth()/2.0, getHeight()/2.0 - win.getHeight()/2.0);
			add(win);
		} else {
			if (turns <= 0) {
				scores.setText("Score: " + score + " ,Turns: 0");
				GLabel lose = new GLabel("GAME OVER", 0, 0);
				lose.setFont(SCREEN_FONT);
				lose.setLocation(getWidth()/2.0 - lose.getWidth()/2.0, getHeight()/2.0 - lose.getHeight()/2.0);
				add(lose);
				remove(paddle);
			}
		}
	}

	private void checkForSpeedIncrease() {
		if (counterHits % 10 == 0) {
			VELOCITY_Y *= 1.5;
			getVelocityX();
		}
	}

	//The following method checks if the user has hit the ball 5 times to shift the bricks down. 
	private void checkForShift() { 
		int startY = (int) (getHeight() - PADDLE_Y_OFFSET - paddle.getHeight() - (BRICK_HEIGHT*2));
		if (counterHits % 5 ==0) {
			for (int y = startY; y > 0; y-=(BRICK_HEIGHT+BRICK_SEP)) {
				for (int x = (int) (spaceBefore + 5); x < getWidth(); x += (BRICK_WIDTH+BRICK_SEP)) {
					GObject trial = getElementAt(x,y);
					if  (trial != null && trial != scores && trial != paddle) {
						double oldX = trial.getX();
						double oldY = trial.getY();
						trial.setLocation(oldX, oldY + BRICK_SEP + trial.getHeight());
					}
				}
			}
			addNewRow();
			rows--;
		}
	}

	//The following method adds the new row of bricks at the beggining and continues the color pattern. 
	private void addNewRow() {
		int i = (int) (getWidth()/(BRICK_WIDTH + BRICK_SEP));
		for (int j = 0; j < i; j ++) {
			brick = new GRect (spaceBefore + ((BRICK_WIDTH + BRICK_SEP) * j), BRICK_Y_OFFSET, BRICK_WIDTH, BRICK_HEIGHT);
			brick.setFilled(true);
			chooseColor(rows);
			add(brick);
			numberOfBricks++;
		}
	}

}