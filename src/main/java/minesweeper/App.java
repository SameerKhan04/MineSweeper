package minesweeper;

import processing.core.PApplet;
import processing.core.PImage;

public class App extends PApplet {
    private static final int CELLSIZE = 32;
    private static final int BOARD_WIDTH = 27;
    private static final int BOARD_HEIGHT = 18;
    private static final int TOPBAR = 64;
    private static final int WIDTH = CELLSIZE * BOARD_WIDTH;
    private static final int HEIGHT = CELLSIZE * BOARD_HEIGHT + TOPBAR;
    private Tile[][] board = new Tile[BOARD_HEIGHT][BOARD_WIDTH];
    private PImage[] mineImages = new PImage[10];
    private PImage flagImage;
    private boolean gameOver = false;
    private int explosionStartFrame = -1;
    private int startTime;
    private int currentTime;
    private int[][] mineCountColour = {
        {0,0,0},
        {0,0,255},
        {0,133,0},
        {255,0,0},
        {0,0,132},
        {132,0,0},
        {0,132,132},
        {132,0,132},
        {32,32,32}
    };

    public void settings() {
        size(WIDTH, HEIGHT);
    }

    public void setup() {
        surface.setTitle("Minesweeper");
        for (int i = 0; i < 10; i++) {
            mineImages[i] = loadImage("minesweeper/mine" + i + ".png");
        }
        flagImage = loadImage("minesweeper/flag.png");
        initializeBoard();
        placeMines();
        calculateMines();
        startTime = millis();
    }

    private void initializeBoard() {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                board[i][j] = new Tile();
            }
        }
        gameOver = false;
        startTime = millis();
    }

    private void placeMines() {
        int numMines = 100;
        for (int i = 0; i < numMines; ) {
            int row = (int) random(BOARD_HEIGHT);
            int col = (int) random(BOARD_WIDTH);
            if (!board[row][col].isMine()) {
                board[row][col].setMine(true);
                i++;
            }
        }
    }

    private void calculateMines() {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (!board[i][j].isMine()) {
                    int count = 0;
                    for (int di = -1; di <= 1; di++) {
                        for (int dj = -1; dj <= 1; dj++) {
                            int ni = i + di;
                            int nj = j + dj;
                            if (ni >= 0 && ni < BOARD_HEIGHT && nj >= 0 && nj < BOARD_WIDTH) {
                                if (board[ni][nj].isMine()) {
                                    count++;
                                }
                            }
                        }
                    }
                    board[i][j].setAdjacentMines(count);
                }
            }
        }
    }

    public void draw() {
        background(200);
        drawTimer();
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                drawTile(i, j);
            }
        }
        if (gameOver) {
            fill(255, 0, 0);
            textSize(32);
            text("You Lost!", WIDTH / 2 - 100, TOPBAR / 2);
            animateExplosions();
        }
    }

    private void drawTimer() {
        if (!gameOver) {
            currentTime = (millis() - startTime) / 1000;
        }
        int minutes = currentTime / 60;
        int seconds = currentTime % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        fill(0);
        textSize(18);
        text(timeFormatted, WIDTH - 100, 30);
    }

    private void drawTile(int row, int col) {
        int x = col * CELLSIZE;
        int y = row * CELLSIZE + TOPBAR;
        Tile tile = board[row][col];
        if (tile.isRevealed()) {
            if (tile.isMine()) {
                image(mineImages[0], x, y);
            } else {
                fill(255);
                rect(x, y, CELLSIZE, CELLSIZE);
                int mineCount = tile.getAdjacentMines();
                if (mineCount > 0) {
                    fill(mineCountColour[mineCount][0], mineCountColour[mineCount][1], mineCountColour[mineCount][2]);
                    text(mineCount, x + 10, y + 20);
                }
            }
        } else if (tile.isFlagged()) {
            image(flagImage, x, y, CELLSIZE, CELLSIZE);
        } else {
            fill(0, 0, 255);
            rect(x, y, CELLSIZE, CELLSIZE);
        }
    }

    private void animateExplosions() {
        int explosionDuration = 30;
        if (frameCount - explosionStartFrame > explosionDuration) return;

        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                Tile tile = board[i][j];
                if (tile.isMine()) {
                    int index = (frameCount - explosionStartFrame) / 3 % 10;
                    image(mineImages[index], j * CELLSIZE, i * CELLSIZE + TOPBAR);
                }
            }
        }
    }

    @Override
    public void mousePressed() {
        if (gameOver) return;
        int col = mouseX / CELLSIZE;
        int row = (mouseY - TOPBAR) / CELLSIZE;
        if (row >= 0 && row < BOARD_HEIGHT && col >= 0 && col < BOARD_WIDTH) {
            Tile tile = board[row][col];
            if (mouseButton == RIGHT) {
                tile.setFlagged(!tile.isFlagged());
            } else if (mouseButton == LEFT && !tile.isFlagged()) {
                revealTile(row, col);
            }
        }
    }

    private void revealTile(int row, int col) {
        Tile tile = board[row][col];
        if (tile.isRevealed() || tile.isFlagged()) return;

        tile.setRevealed(true);
        if (tile.isMine()) {
            triggerExplosion();
        } else if (tile.getAdjacentMines() == 0) {
            for (int di = -1; di <= 1; di++) {
                for (int dj = -1; dj <= 1; dj++) {
                    int ni = row + di;
                    int nj = col + dj;
                    if (ni >= 0 && ni < BOARD_HEIGHT && nj >= 0 && nj < BOARD_WIDTH) {
                        revealTile(ni, nj);
                    }
                }
            }
        }
    }

    private void triggerExplosion() {
        gameOver = true;
        explosionStartFrame = frameCount;
    }

    @Override
    public void keyPressed() {
        if (key == 'r' || key == 'R') {
            initializeBoard();
            placeMines();
            calculateMines();
        }
    }

    public static void main(String[] args) {
        PApplet.main("minesweeper.App");
    }
}
