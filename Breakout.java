// Assignment 4: Breakout
//By: Javier Arevalo
//Section: Thursdays 30

import acm.graphics.*;     // GOval, GRect, etc.
import acm.program.*;      // GraphicsProgram
import acm.util.*;         // RandomGenerator
import java.awt.*;         // Color
import java.awt.event.*;   // MouseEvent

public class Breakout extends BreakoutProgram {

	//paddle should be a instance variable so it can be accessed when the mouse is moved. 
	GRect paddle;
	//score should be a instance variable so that it can be continually updated through the game. 
	int score = 0;
	double VELOCITY_X;
	double VELOCITY_Y;
	//should be a instance variable to update the stats of the game continually.
	GLabel scores;
	//Following variables also need to be instance variables so the different methods can now
	//when to end the game, and see if the user has already won. 
	int numberOfBricks;
	int turns = NTURNS;

	public void run() {

		// Set the window's title bar text
		setTitle("CS 106A Breakout");

		setCanvasSize(CANVAS_WIDTH, CANVAS_HEIGHT);

		createBricks();
		createGameComponents();
		//add ball
		GOval ball = new GOval (0, 0, BALL_RADIUS * 2, BALL_RADIUS * 2);
		ball.setLocation(getWidth()/2.0 - (ball.getWidth()/2.0), getHeight()/2.0 - (ball.getHeight()/2.0));
		ball.setFilled(true);
		add(ball);

		velocity();

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
		remove(ball);

		//if the code reaches this part, it means that the boolean game(ball) is false which means that the player ran
		//out of turns or that the score == number of bricks, which means the user won. 
		endGame();
	}

	//This method will create the bricks with the appropiate color pattern. 
	private void createBricks() {
		// The following code will create the bricks and use a counter to know how many bricks were created. 
		int rows = 0;
		numberOfBricks = 0;
		int i = (int) (getWidth()/(BRICK_WIDTH + BRICK_SEP));
		double spaceBefore = (getWidth() - (i * BRICK_WIDTH) - ( (i-1) * BRICK_SEP)) / 2.0;
		for (int h = 0; h < NBRICK_ROWS; h ++) {
			//this code creates each row
			rows++;
			for (int j = 0; j < i; j ++) {
				GRect brick = new GRect (spaceBefore + ((BRICK_WIDTH + BRICK_SEP) * j), (BRICK_Y_OFFSET + (h * (BRICK_HEIGHT+ BRICK_SEP))), BRICK_WIDTH, BRICK_HEIGHT);
				brick.setFilled(true);
				chooseColor(rows, brick);
				add(brick);
				numberOfBricks++;
			}
		}
	}

	private void createGameComponents() {
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
	}

	//The following method will create the correct velocities for each variable. 
	//For x, it selects a value outside the (- min, min) velocities as specified in handout. 
	private void velocity() {
		VELOCITY_Y = 5;
		VELOCITY_X = RandomGenerator.getInstance().nextDouble(VELOCITY_X_MIN, VELOCITY_X_MAX);
		boolean sign = RandomGenerator.getInstance().nextBoolean();
		if (sign) {
			VELOCITY_X *= -1;
		}
		//The following code assures that the random velocity for x is not in the range from -VELOCITY_X_MIN to VELOCITY_X_MIN
		while (-VELOCITY_X_MIN < VELOCITY_X && VELOCITY_X < VELOCITY_X_MIN) {
			VELOCITY_X = RandomGenerator.getInstance().nextDouble(VELOCITY_X_MIN, VELOCITY_X_MAX);
			if (sign) {
				VELOCITY_X *= -1;
			}
		}
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
			}
		} else {
			if (collision != scores) {
				score++;
				remove(collision);
				scores.setText("Score: " + score + " ,Turns: " + turns);
				VELOCITY_Y *= -1;
			}
		}
	}

	//This method chooses the color of the bricks according to their location. 
	//It uses the remainder operation so that it "wraps around" whenever there are more than 10 rows. 
	private void chooseColor (int number, GRect brick) {
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
				}
			}

		}
		return true;
	}

	//This method is called whenever one game ends by either eliminating all bricks or by running out of turns. 
	//Precondition: the player has no turns left OR has no bricks left to eliminate
	//Postcondition: the screen will show the user if he won or lost. 
	private void endGame() {
		if (score == numberOfBricks){
			clearCanvas();
			add(scores);
			GLabel win = new GLabel("YOU WIN!", 0, 0);
			win.setFont(SCREEN_FONT);
			win.setLocation(getWidth()/2.0 - win.getWidth()/2.0, getHeight()/2.0 - win.getHeight()/2.0);
			add(win);
		} else {
			if (turns <= 0) {
				remove(paddle);
				scores.setText("Score: " + score + " ,Turns: 0");
				GLabel lose = new GLabel("GAME OVER", 0, 0);
				lose.setFont(SCREEN_FONT);
				lose.setLocation(getWidth()/2.0 - lose.getWidth()/2.0, getHeight()/2.0 - lose.getHeight()/2.0);
				add(lose);
			}
		}
	}

}
