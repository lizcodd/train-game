package com.lizcodd.traingame;

import com.badlogic.gdx.graphics.Texture;

public class StationNode extends Node {
    String color;
    Texture stationImg;

    public StationNode(int key, float x, float y, String color, Texture stationImg) {
        super(key, x, y);
        this.color = color;
        this.stationImg = stationImg;
    }
}
