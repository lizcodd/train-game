package com.lizcodd.traingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;

// a binary tree implementation specific to the Train Game
public class TrainTree {
    Node root;
    ArrayList<StationNode> stations;
    ArrayList<GateNode> gates;

    public TrainTree() {
        this.root = null;
        stations = new ArrayList<StationNode>();
        gates = new ArrayList<GateNode>();
    }

    void addNode(Node newNode) {
        if (root == null) {
            this.root = newNode;
        }

        // keep track of gates vs stations
        if (newNode instanceof GateNode) {
            gates.add((GateNode) newNode);
        } else {
            stations.add((StationNode) newNode);
        }

        Node focusNode = root;
        Node parent;

        // traverse tree to find place for the new node
        while (true) {
            parent = focusNode;
            if (newNode.key == focusNode.key) return; // can't have duplicate keys
            if (newNode.key < focusNode.key) {
                focusNode = focusNode.left;
                if (focusNode == null) {
                    parent.setLeft(newNode);
                    return;
                }
            } else {
                focusNode = focusNode.right;
                if (focusNode == null) {
                    parent.setRight(newNode);
                    return;
                }
            }
        }
    }

    // pre-order traversal algorithm that draws the lines (tracks) between nodes
    void drawTracks(ShapeRenderer rend, Node focusNode) {
        if (focusNode != null) {
            // draw tracks to children if any
            if (focusNode.left != null) rend.line(focusNode.x, focusNode.y, focusNode.left.x, focusNode.left.y);
            if (focusNode.right != null) rend.line(focusNode.x, focusNode.y, focusNode.right.x, focusNode.right.y);
            // recursively traverse rest of tree
            drawTracks(rend, focusNode.left);
            drawTracks(rend, focusNode.right);
        }
    }
}