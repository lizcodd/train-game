package com.lizcodd.traingame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Arrays;
import java.util.Random;

import static com.lizcodd.traingame.TrainGame.MIN_PADDING;
import static com.lizcodd.traingame.TrainGame.TRAIN_HEIGHT;
import static com.lizcodd.traingame.TrainGame.TRAIN_WIDTH;

public class Train {
    Sprite sprite;
    String color;
    float slope = 0f;
    int offset = 0;
    boolean isFlipped = false;
    boolean reachedRootGate = false;
    String[] colorPicker = {"red", "yellow", "green", "blue", "purple"};
    Random rand;

    public Train(int offset) {
        rand = new Random();
        this.color = colorPicker[rand.nextInt(colorPicker.length)];
        this.offset = offset;
        this.sprite = new Sprite(new Texture(color + "Train.png"));
        this.sprite.setBounds(0 - this.offset, 0, TRAIN_WIDTH, TRAIN_HEIGHT);
        this.sprite.setOrigin(0, TRAIN_HEIGHT / 2);
    }

    void pickNewColor() {
        this.color = colorPicker[rand.nextInt(colorPicker.length)];
        this.sprite.setTexture(new Texture(color + "Train.png"));
    }
}
