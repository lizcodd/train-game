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
	final float VIRTUAL_WIDTH = 800;
	final float VIRTUAL_HEIGHT = 480;

	int NUM_TRAINS = 5;
	int NUM_STATIONS = 5;
	float TRAIN_SPEED = 1;

	boolean playMode;
	SpriteBatch batch;
	Texture bg;
	Texture gameOver;
	TrainTree tree;
	Sprite[] trains;
	Float[] trainSlopes;
	Boolean[] trainIsFlipped;
	String[] trainColors;
	String[] colorPicker = {"red", "yellow", "green", "blue", "purple"};
	int[] trainOffsets;
	Random rand;
	Boolean[] reachedRootGate;
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
		trains = new Sprite[NUM_TRAINS];
		trainSlopes = new Float[NUM_TRAINS];
		trainIsFlipped = new Boolean[NUM_TRAINS];
		trainOffsets = new int[NUM_TRAINS];
		trainColors = new String[NUM_TRAINS];
		reachedRootGate = new Boolean[NUM_TRAINS];

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

		// set up trains
		for (int i = 0; i < NUM_TRAINS; i++) {
			trainColors[i] = colorPicker[rand.nextInt(colorPicker.length)];
			String trainImg =  trainColors[i] + "Train.png";
			trains[i] = new Sprite(new Texture(trainImg));
			trainOffsets[i] = (int) (rand.nextInt(71) + trains[0].getWidth() + 35 + trainOffsets[Math.abs(i - 1) % trainOffsets.length]);
			trains[i].setBounds(0 - trainOffsets[i], 0, 50, 25);
			trains[i].setOrigin(0, trains[0].getHeight()/2);
			trainSlopes[i] = 0f;
			trainIsFlipped[i] = false;
			reachedRootGate[i] = false;
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
		batch.begin();
		batch.draw(bg, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
		handleInput();

		if (playMode) {
			// draw gates
			for (GateNode gate : tree.gates) {
				gate.gateSprite.draw(batch);
			}
			batch.end();

			shapeRenderer.setProjectionMatrix(cam.combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
			shapeRenderer.setColor(0, 0, 0, 1);
			// line from train origin to root gate
			shapeRenderer.line(0, trains[0].getHeight() / 2, tree.root.x, tree.root.y);
			tree.drawTracks(shapeRenderer, tree.root);
			shapeRenderer.end();

			batch.begin();
			for (int i = 0; i < NUM_TRAINS; i++) {
				// rotate toward the root gate when trains enter screen
				if (trains[i].getX() > 0 && !reachedRootGate[i]) {
					trainSlopes[i] = tree.root.y / tree.root.x;
					trains[i].setRotation((float) Math.toDegrees(Math.atan(trainSlopes[i])));
				}
				// move train along tracks and flip y-values of image when train turns to keep it right-side up
				// keep the distance traveled per frame the same regardless of slope
				// (formula for x derived from pythagorean theorem)
				float trainAngleRadians = (float) Math.atan(trainSlopes[i]);
				float x = (float) (TRAIN_SPEED * Math.sqrt(1 / (1 + Math.pow(Math.tan(trainAngleRadians), 2))));
				if (trainSlopes[i] >= 0) {
					trains[i].translate(x, (float) (x * Math.tan(trainAngleRadians)));
					if (trainIsFlipped[i]) {
						trains[i].flip(false, true);
						trainIsFlipped[i] = false;
					}
				} else {
					trains[i].translate(-1 * x, (float) (x * -1 * Math.tan(trainAngleRadians)));
					if (!trainIsFlipped[i]) {
						trains[i].flip(false, true);
						trainIsFlipped[i] = true;
					}
				}
				// change train's direction when train crosses a gate
				for (GateNode gate : tree.gates) {
					if (gate.gateSprite.getBoundingRectangle().contains(trains[i].getX(), trains[i].getY())) {
						trainSlopes[i] = (float) Math.tan(Math.toRadians(gate.gateAngle));
						trains[i].setRotation(gate.gateAngle);
						reachedRootGate[i] = true;
					}
				}

				// add a point to player's score if the train arrives at the right colored station
				// if train goes to wrong station add 1 to mistake counter (3 mistakes and you lose)
				for (StationNode station : tree.stations) {
					if (trains[i].getBoundingRectangle().contains(station.x, station.y)) {
						if (station.color.equals(trainColors[i])) {
							score += 1;
						} else {
							mistakes += 1;
						}
						if (mistakes < 3) {
							// reset train position off screen, random distance from the last train
							float leftmostTrainPos = 0;
							for (Sprite train : trains) {
								if (train.getX() < leftmostTrainPos) {
									leftmostTrainPos = train.getX();
								}
							}
							trains[i].setPosition(leftmostTrainPos - (rand.nextInt(101) + trains[0].getWidth() + 25), 0);
							reachedRootGate[i] = false;
							trainSlopes[i] = 0f;
							trains[i].setRotation(0);
							// pick new color for train
							trainColors[i] = colorPicker[rand.nextInt(colorPicker.length)];
							trains[i].setTexture(new Texture(trainColors[i] + "Train.png"));
						} else {
							playMode = false;
						}
					}
				}
				trains[i].draw(batch);
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
				// TODO: implement play again method
			}
		}
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
