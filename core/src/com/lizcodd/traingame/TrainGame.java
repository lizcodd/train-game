package com.lizcodd.traingame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TrainGame implements ApplicationListener {
	public static final float VIRTUAL_WIDTH = 800;
	public static final float VIRTUAL_HEIGHT = 480;
	public static final int NUM_TRAINS = 5;
	public static final int TRAIN_WIDTH = 50;
	public static final int TRAIN_HEIGHT = 25;
	public static final int MIN_PADDING = 25;
	public static final float TRAIN_SPEED = 1;

	boolean playMode;
	SpriteBatch batch;
	Texture bg;
	Texture gameOver;
	TrainTree tree;
	Train[] trains;
	int leftmostTrainX = 0;
	Random rand;
	ShapeRenderer shapeRenderer;
	int score = 0;
	int mistakes = 0;
	BitmapFont scoreFont;
	OrthographicCamera cam;
	float aspectRatio;

	@Override
	public void create () {
		cam = new OrthographicCamera();
		batch = new SpriteBatch();
		playMode = true;
		rand = new Random();
		shapeRenderer = new ShapeRenderer();
		bg = new Texture("bg.png");
		gameOver = new Texture("gameOver.png");
		scoreFont = new BitmapFont();
		scoreFont.setColor(Color.GREEN);
		scoreFont.getData().setScale(3);
		trains = new Train[NUM_TRAINS];

		// create gates and stations as nodes
		tree = new TrainTree();
		tree.addNode(new GateNode(6, 200, 100)); // root
		tree.addNode(new GateNode(2, 300, 150));
		tree.addNode(new GateNode(8, 100, 250));
		tree.addNode(new StationNode(1, VIRTUAL_WIDTH - 150, 250, "red", new Texture("redStation.png")));
		tree.addNode(new GateNode(4, VIRTUAL_WIDTH - 300, 300));
		tree.addNode(new StationNode(3, VIRTUAL_WIDTH - 200, VIRTUAL_HEIGHT - 140, "blue", new Texture("blueStation.png")));
		tree.addNode(new StationNode(5, VIRTUAL_WIDTH - 400, VIRTUAL_HEIGHT - 100, "purple", new Texture("purpleStation.png")));
		tree.addNode(new StationNode(7, 210, VIRTUAL_HEIGHT - 120, "yellow", new Texture("yellowStation.png")));
		tree.addNode(new StationNode(9, 25, VIRTUAL_HEIGHT - 100, "green", new Texture("greenStation.png")));

		// create and set up trains at random distances behind each other
		int totalOffset = 0;
		for (int i = 0; i < NUM_TRAINS; i++) {
            trains[i] = new Train(totalOffset);
			totalOffset += rand.nextInt(101) + TRAIN_WIDTH + MIN_PADDING;
		}
	}

    @Override
    public void resize(int width, int height) {
		aspectRatio = width / height;
		cam.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
		cam.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
    }

    @Override
	public void render () {
		cam.update();
		batch.setProjectionMatrix(cam.combined);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		leftmostTrainX = 0;
		batch.begin();
		batch.draw(bg, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
		handleInput();

		if (playMode) {
			// keep track of X-coord of last train (train order does not stay the same)
			for (Train train : trains) {
				if (train.sprite.getX() < leftmostTrainX)
					leftmostTrainX = (int) train.sprite.getX();
			}

			// draw gates
			for (GateNode gate : tree.gates) {
				gate.gateSprite.draw(batch);
			}
			batch.end();

			// draw tracks
			shapeRenderer.setProjectionMatrix(cam.combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
			shapeRenderer.setColor(0, 0, 0, 1);
			// first draw line from train origin to root gate
			shapeRenderer.line(0, TRAIN_HEIGHT / 2, tree.root.x, tree.root.y);
			// then draw tracks between all nodes
			tree.drawTracks(shapeRenderer, tree.root);
			shapeRenderer.end();

			batch.begin();
			for (Train train : trains) {
				// make trains rotate toward the root gate when they first enter screen
				if (train.sprite.getX() > 0 && !train.reachedRootGate) {
					train.slope = tree.root.y / tree.root.x;
					train.sprite.setRotation((float) Math.toDegrees(Math.atan(train.slope)));
				}

				// move train along tracks and flip y-values of image when train turns to keep it right-side up
				// keep the distance traveled per frame the same regardless of slope
				// (formula for x derived from pythagorean theorem)
				float x = (float) (TRAIN_SPEED * Math.sqrt(1 / (1 + Math.pow(train.slope, 2))));
				if (train.slope >= 0) {
					train.sprite.translate(x, (float) (x * train.slope));
					if (train.isFlipped) {
						train.sprite.flip(false, true);
						train.isFlipped = false;
					}
				} else {
					train.sprite.translate(-1 * x, (float) (x * -1 * train.slope));
					if (!train.isFlipped) {
						train.sprite.flip(false, true);
						train.isFlipped = true;
					}
				}
				// change train's direction when train crosses a gate
				for (GateNode gate : tree.gates) {
					if (gate.gateSprite.getBoundingRectangle().contains(train.sprite.getX(), train.sprite.getY())) {
						train.slope = (float) Math.tan(Math.toRadians(gate.gateAngle));
						train.sprite.setRotation(gate.gateAngle);
						train.reachedRootGate = true;
					}
				}

				// add a point to player's score if the train arrives at the right colored station
				// if train goes to wrong station add 1 to mistake counter (3 mistakes and you lose)
				for (StationNode station : tree.stations) {
					if (train.sprite.getBoundingRectangle().contains(station.x, station.y)) {
						if (station.color.equals(train.color)) {
							score += 1;
						} else {
							mistakes += 1;
						}
						if (mistakes < 3) {
							leftmostTrainX -= rand.nextInt(101) + TRAIN_WIDTH + MIN_PADDING;
							train.sprite.setPosition(leftmostTrainX, 0);
							train.reachedRootGate = false;
							train.slope = 0f;
							train.sprite.setRotation(0);
							train.pickNewColor();
						} else {
							playMode = false;
						}
					}
				}
				train.sprite.draw(batch);
			}

			// draw stations
			for (StationNode station : tree.stations) {
				batch.draw(station.stationImg, station.x, station.y, 120, 60);
			}
		} else {
			// game over
			batch.draw(gameOver, VIRTUAL_WIDTH / 2 - gameOver.getWidth() / 2, VIRTUAL_HEIGHT / 2 - gameOver.getHeight() / 2, gameOver.getWidth(), gameOver.getHeight());
		}

		scoreFont.draw(batch, String.valueOf(score), VIRTUAL_WIDTH - 60, 50);
		batch.end();
	}

	void handleInput() {
		if (Gdx.input.justTouched()) {
			if (playMode) {
				// convert from touch coordinates (y-down) to screen coordinates (y-up)
				int screenX = (int) (Gdx.input.getX() * VIRTUAL_WIDTH / Gdx.graphics.getWidth());
				int screenY = (int) ((Gdx.graphics.getHeight() - 1 - Gdx.input.getY()) * VIRTUAL_HEIGHT / Gdx.graphics.getHeight());
				// flip the gate angle when gate is touched
				for (GateNode gate : tree.gates) {
					if (gate.touchCircle.contains(screenX, screenY)) {
						gate.tracksGoLeft = !gate.tracksGoLeft;
						gate.setGateAngle();
					}
				}
			} else {
				playAgain();
			}
		}
	}

	void playAgain() {
		int totalOffset = TRAIN_WIDTH;
		for (Train train : trains) {
			train.reachedRootGate = false;
			train.slope = 0f;
			train.sprite.setRotation(0);
			train.pickNewColor();
			train.sprite.setPosition(-totalOffset, 0);
			totalOffset += rand.nextInt(101) + TRAIN_WIDTH + MIN_PADDING;
		}
		score = 0;
		mistakes = 0;
		playMode = true;
	}

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
	public void dispose () {
		batch.dispose();
		bg.dispose();
	}
}