/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI.Components;

import GUI.Storage.Grid;
import Tools.MathFunc;
import java.awt.Rectangle;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 *
 * @author 160471
 */
public class DrawingGrid {

    //Colours
    Color PRIM_COLOR = Color.valueOf("black");
    Color SEC_COLOR = Color.valueOf("WHITE");

    private Canvas CANVAS;
    private boolean GEN_FRAME = true;
    private boolean LISTEN = false;
    private Grid<Double> GRID;
    private int G_WIDTH;
    private int G_HEIGHT;
    private Rectangle[][] BOUNDS;
    private double PAINT_VALUE = 1.0;

    /**
     * This creates a new Drawing grid, passing in the Canvas that the grid will
     * be drawn on to.
     *
     * @param x The amount of addressable pixels per row
     * @param y The amount of addressable pixels per column
     * @param canv This is the canvas component that the grid will be drawn on
     */
    public DrawingGrid(int x, int y, Canvas canv) {

        CANVAS = canv;

        GRID = new Grid<Double>(x, y, 0.0);
        G_WIDTH = x;
        G_HEIGHT = y;
        BOUNDS = new Rectangle[x][y];
        addListeners();
        tick();

        //runGraphics();
    }

    public void resetColors() {

    }

    /**
     * Sets the contents of the 2 dimensional grid corresponding to 1
     * dimensional array inputted using the Grid class;
     *
     * @param contents Data to be written to the grid
     */
    public void setContents(List<Double> contents) {
        if (contents.size() == this.getOutput().size()) {
            this.GRID.setList(contents);
            tick();
        } else {
            System.out.println("Array mismatch!");
        }
    }

    /**
     * Outputs the numerical contents of the grid using the Grid class
     */
    public List<Double> getOutput() {
        return GRID.getList();
    }

    /**
     * Clears the grid and sets all the values to 0.0
     */
    public void clear() {
        for (int x = 0; x < G_WIDTH; x++) {
            for (int y = 0; y < G_HEIGHT; y++) {
                GRID.set(x, y, 0.0);
            }
        }
        tick();
    }

    /**
     * Using the dimensions of the canvas, it divides the grid into a set number
     * of pixel dimensions provided by G_WIDTH and G_HEIGHT.
     *
     */
    private void calculateBounds() {
        double rectWidth = (double) CANVAS.getWidth() / (double) G_WIDTH;
        double rectHeight = (double) CANVAS.getHeight() / (double) G_HEIGHT;

        for (int x = 0; x < G_WIDTH; x++) {
            for (int y = 0; y < G_HEIGHT; y++) {
                double across = x * rectWidth;
                double down = y * rectHeight;
                BOUNDS[x][y] = new Rectangle((int) across, (int) down, (int) rectWidth, (int) rectHeight);
            }
        }

    }

    /**
     * Sets the width and height of the drawing grid
     *
     * @param xC width
     * @param yC height
     */
    private void setByBounds(int xC, int yC) {

        for (int x = 0; x < G_WIDTH; x++) {
            for (int y = 0; y < G_HEIGHT; y++) {

                Rectangle r = BOUNDS[x][y];
                if (xC >= r.x && yC >= r.y && xC < (r.x + r.width) && yC < (r.y + r.height)) {
                    GRID.set(x, y, PAINT_VALUE);

                    GRID.get(x, y);
                }

                //}
            }
        }
    }

    /**
     * This adds a mouse dragged method which uses the drag location to set the
     * corresponding pixel black
     */
    private void addListeners() {
        CANVAS.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {

                        setByBounds((int) event.getX(), (int) event.getY());
                        tick();
                    }
                });

    }

    /**
     * Re-renders the canvas by redrawing its graphics, this is used to update
     * when along with the addition of new data.
     */
    private void tick() {
        if (GEN_FRAME) {
            GraphicsContext g = CANVAS.getGraphicsContext2D();
            g.setFill(SEC_COLOR);

            g.fillRect(0, 0, CANVAS.getWidth(), CANVAS.getHeight());

            calculateBounds();
            drawGrid(g);

        }
    }

    /**
     * This draws the grid onto the canvas, corresponding to the grid contents
     * when tick() is called.
     *
     * @see tick()
     * @param g Graphics context of the canvas being drawn on
     */

    private void drawGrid(GraphicsContext g) {
        for (int x = 0; x < G_WIDTH; x++) {
            for (int y = 0; y < G_HEIGHT; y++) {

                if (this.GRID.get(x, y) > 1) {
                    System.out.println("Above 1 wut! " + this.GRID.get(x, y));
                }
                double alph = this.GRID.get(x, y);

                g.setFill(Color.color(PRIM_COLOR.getRed(), PRIM_COLOR.getGreen(), PRIM_COLOR.getBlue(), alph));

                g.fillRect(BOUNDS[x][y].getX(), BOUNDS[x][y].getY(), BOUNDS[x][y].getWidth(), BOUNDS[x][y].getHeight());

            }
        }
    }
}
