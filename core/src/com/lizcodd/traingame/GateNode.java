package com.lizcodd.traingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Circle;

public class GateNode extends Node {
    boolean tracksGoLeft;
    float gateAngle;
    Texture gateImg = new Texture("shortTrackGate.png");
    public Sprite gateSprite;
    Circle touchCircle;

    public GateNode(int key, float x, float y) {
        super(key, x, y);
        this.gateSprite = new Sprite(gateImg);
        gateSprite.setBounds(x, y - 7, 50, 15);
        gateSprite.setOrigin(0, gateSprite.getHeight() / 2);
        this.gateAngle = 0;
        this.tracksGoLeft = true;
        this.touchCircle = new Circle(x, y, 50);
    }

    public void setLeft(Node left) {
        super.left = left;
        setGateAngle();
    }

    public void setRight(Node right) {
        super.right = right;
        setGateAngle();
    }

    public void setGateAngle() {
        float slope = (left.y - this.y)/(left.x - this.x);
        if (tracksGoLeft && left != null) {
            this.gateAngle = (float) Math.toDegrees(Math.atan((left.y - this.y)/(left.x - this.x)));
        } else if (right != null) {
            this.gateAngle = (float) Math.toDegrees(Math.atan((right.y - this.y)/(right.x - this.x)));
        }
        if (gateAngle < 0) {
            gateAngle += 180;
        }
        this.gateSprite.setRotation(this.gateAngle);
    }
}

