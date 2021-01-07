package com.lizcodd.traingame;

import com.badlogic.gdx.graphics.Texture;

public class Node {
    int key;
    float x;
    float y;
    Node left;
    Node right;

    public Node(int key, float x, float y) {
        this.key = key;
        this.x = x;
        this.y = y;
        this.left = this.right = null;
    }

    public void setX(float x) {
        this.x = x;
    }
    public void setY(float y) {
        this.y = y;
    }
    public void setLeft(Node left) {
        this.left = left;
    }
    public void setRight(Node right) {
        this.right = right;
    }
}
