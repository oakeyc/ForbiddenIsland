import java.util.ArrayList; 
import java.util.Random;

import tester.*; 
import javalib.impworld.*; 

import java.awt.Color; 

import javalib.worldimages.*; 

// A cell of land.
class Cell {
    static final int CELL_SIZE = 15;
    
    double height;
    int x, y;
    Cell left, top, right, bottom;
    boolean isFlooded, hasPart;
    
    Cell(double height, int x, int y) {
        this.height = height;
        this.x = x;
        this.y = y;
        this.isFlooded = false;
        this.hasPart = false;
        this.left = this.top = this.right = this.bottom = null;
    }
    
    // EFFECT: Sets the flooded state of this cell to the given boolean.
    void setFlooded(boolean isFlooded) {
        this.isFlooded = isFlooded;
    }
    
    void setNeighbors(Cell left, Cell top, Cell right, Cell bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
    
    // compares the height of this cell to the height of that cell.
    //  Returns: 0  if height of this and that are equal.
    //           <0 if height of this is less than height of that.
    //           >0 if height of this is greater than height of that.
    double compareHeight(Cell that) {
        return this.height - that.height;
    }
    
    // Draws this cell onto the given background.
    WorldImage drawOnto(WorldImage background, int waterHeight) {
        WorldImage cell;
        if (this.isFlooded) {
            int maxHeight = ForbiddenIslandWorld.ISLAND_SIZE / 2;
            double ratio = Math.min((waterHeight - this.height) / maxHeight, 1);
            int color = (int) (0x80 * (1 - ratio));
            cell = new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                    new Color(color));
        }
        else {
            int color = 0x008000;
            double ratio;
            int islandSize = ForbiddenIslandWorld.ISLAND_SIZE;
            int maxHeight = islandSize / 2;
            if (this.height >= waterHeight) {
                ratio = Math.min((this.height - waterHeight) / maxHeight, 1);
                color += ((int) (0xFF * ratio) * 0x010000) +
                        ((int) (0x79 * ratio) * 0x0100) +
                        (int) (0xFF * ratio);
            }
            else {
                ratio = Math.min((waterHeight - this.height) / maxHeight, 1);
                color = ((int) (0x80 * (1 - ratio)) * 0x0100) + ((int) (0x80 * ratio) * 0x010000);
            }
            cell = new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                    new Color(color));
        }
        return new OverlayOffsetImage(cell,
                this.x * Cell.CELL_SIZE - ForbiddenIslandWorld.BACKGROUND_SIZE / 2,
                this.y * Cell.CELL_SIZE - ForbiddenIslandWorld.BACKGROUND_SIZE / 2,
                background);
    }
    
    void update(int waterHeight) {
        if (!this.isFlooded && this.height < waterHeight && this.isNextToFloodedCell()) {
            this.setFlooded(true);
        }
    }
    
    boolean isNextToFloodedCell() {
        return this.left.isFlooded || this.right.isFlooded ||
                this.bottom.isFlooded || this.top.isFlooded;
    }
}


// A cell in the ocean.
class OceanCell extends Cell {
    
    OceanCell(int x, int y) {
        super(0, x, y);
        this.isFlooded = true;
    }
    
    @Override
    // EFFECT: Sets the flooded state of this ocean cell, which is always true.
    void setFlooded(boolean isFlooded) {}
    
    @Override
    // compares the height of this ocean cell to the height of that cell.
    //  Returns: the height of that cell, which is
    //           0  if height of that is even with ocean level.
    //           <0 if height of that is below ocean level.
    //           >0 if height of that is above ocean level.
    double compareHeight(Cell that) {
        return that.height;
    }
    
    @Override
    // Draws this ocean cell onto the given background.
    WorldImage drawOnto(WorldImage background, int waterHeight) {
        return background;
    }
}



// A board representing an island.
class Board {
    ArrayList<ArrayList<Cell>> cells;
    
    Board() {
        this.makeMountainBoard();
    }
    
    void makeMountainBoard() {
        ArrayList<ArrayList<Double>> heights = new ArrayList<ArrayList<Double>>();
        double middle = ForbiddenIslandWorld.ISLAND_SIZE / 2.0;
        
        for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE; i++) {
            ArrayList<Double> row = new ArrayList<Double>();
            for (int j = 0; j < ForbiddenIslandWorld.ISLAND_SIZE; j++) {
                double manhatDist = Math.abs(i - middle) + Math.abs(j - middle);
                if (manhatDist < middle) {
                    row.add(middle - manhatDist);
                }
                else {
                    row.add(0.0);
                }
            }
            heights.add(row);
        }
        
        this.cells = this.doubleListToCellList(heights);
    }

    
    void makeDiamondBoard() {
        ArrayList<ArrayList<Double>> heights = new ArrayList<ArrayList<Double>>();
        double middle = ForbiddenIslandWorld.ISLAND_SIZE / 2.0;
        Random rand = new Random();
        
        for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE; i++) {
            ArrayList<Double> row = new ArrayList<Double>();
            for (int j = 0; j < ForbiddenIslandWorld.ISLAND_SIZE; j++) {
                double manhatDist = Math.abs(i - middle) + Math.abs(j - middle);
                if (manhatDist < middle) {
                    row.add(rand.nextDouble() * middle);
                }
                else {
                    row.add(0.0);
                }
            }
            heights.add(row);
        }
        
        this.cells = this.doubleListToCellList(heights);
    }
    
    void makeTerrainBoard() {
        
    }
    
    ArrayList<ArrayList<Cell>> doubleListToCellList(ArrayList<ArrayList<Double>> heights) {
        ArrayList<ArrayList<Cell>> result = new ArrayList<ArrayList<Cell>>();
        
        for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE; i++) {
            ArrayList<Double> hRow = heights.get(i);
            ArrayList<Cell> newRow = new ArrayList<Cell>();
            for (int j = 0; j < ForbiddenIslandWorld.ISLAND_SIZE; j++) {
                if (hRow.get(j) == 0) {
                    newRow.add(new OceanCell(i, j));
                }
                else {
                    newRow.add(new Cell(hRow.get(j), i, j));
                }
            }
            result.add(newRow);
        }
        
        for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE; i++) {
            for (int j = 0; j < ForbiddenIslandWorld.ISLAND_SIZE; j++) {
                Cell left, top, right, bottom; 
                Cell curr = result.get(i).get(j);
                if (onBoard(i, j - 1)) 
                {
                    left = result.get(i).get(j - 1);
                }
                else
                {
                    left = curr;
                }
                if (onBoard(i - 1, j))
                {
                    top = result.get(i - 1).get(j);
                }
                else
                {
                    top = curr;
                }
                if (onBoard(i, j + 1))
                {
                    right = result.get(i).get(j + 1);
                }
                else
                {
                    right = curr;
                }
                if (onBoard(i - 1, j))
                {
                    bottom = result.get(i -1).get(j);
                }
                else
                {
                    bottom = curr;
                }
                curr.setNeighbors(left, top, right, bottom);
            }
        }
        
        return result;
    }
    
    void updateBoard(int waterHeight) {
        for (ArrayList<Cell> row : cells) {
            for (Cell cell : row) {
                cell.update(waterHeight);
            }
        }
    }
    
    boolean onBoard(int x, int y)
    {
        return ForbiddenIslandWorld.ISLAND_SIZE > x && x >= 0 && 
                ForbiddenIslandWorld.ISLAND_SIZE > y && y >= 0;
    }
    
    WorldImage drawOnto(WorldImage background, int waterHeight) {
        WorldImage image = background;
        
        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {
                image = c.drawOnto(image, waterHeight);
            }
        }
        
        return image;
    }
}

class Player
{
    int x, y;
    int steps; // number of steps player has taken
    Board board;

    Player(int x, int y, Board board) {
        this.x = x;
        this.y = y;
        this.steps = 0;
        this.board = board;
    }
    
    boolean canMove(String move)
    {
        switch (move)
        {
            case "up":
                return board.notFlooded(x, y + 1);
            case "down":
                return board.notFlooded(x, y - 1);
            case "left":
                return board.notFlooded(x - 1, y);
            case "right":
                return board.notFlooded(x + 1, y);
            default:
                return false;
        }   
    }

    void move(String move)
    {
        if (canMove(move))
        {
            switch (move)
            {
                case "up":
                    y += 1;
                    break;
                case "down":
                    y -= 1;
                    break;
                case "left":
                    x -= 1;
                    break;
                case "right":
                    x += 1;
                    break;
                default:
                    break;
            }
            this.steps++;
        }
    }

    WorldImage drawOnto(WorldImage background)
    {
        return new OverlayImage(
                new ScaleImage(
                        new FromFileImage("Images/pilot-icon.png"), 0.5),
                background);
    }
}

class ForbiddenIslandWorld extends World
{
    Board board; // all the cells
    int waterHeight; // the height of the water
    static final int ISLAND_SIZE = 64; // constant val
    static final int BACKGROUND_SIZE = Cell.CELL_SIZE * ISLAND_SIZE;
    Player player1;
    final int waterIncrease = 1;
    int tick;

    ForbiddenIslandWorld()
    {
        this.board = new Board();
        this.player1 = new Player(ISLAND_SIZE / 2, ISLAND_SIZE / 2, this.board);
        this.waterHeight = 0;
        this.tick = 0;
    }


    public void onKeyEvent(String key)
    {
        switch(key)
        {
            case "up":
            case "down":
            case "left":
            case "right":
                player1.move(key);
                break;
            case "m": // mountain island
                board.makeMountainBoard();
                this.waterHeight = 0;
                break;
            case "r": // random island
                board.makeDiamondBoard();
                this.waterHeight = 0;
                break;
            case "t": // terrain island
                board.makeTerrainBoard();
                this.waterHeight = 0;
                break;
        }
    }
    
    String oppositeDirection(String dir)
    {
        switch (dir)
        {
            case "up":
                return "down";
            case "down":
                return "up";
            case "left":
                return "right";
            case "right":
                return "left";
            default:
                return "";
        }
    }

    public void onTick()
    {
        this.tick++;
        if (tick % 10 == 0) {
            waterHeight += waterIncrease;
            board.updateBoard(this.waterHeight);
        }
    }

    public WorldScene makeScene()
    {
        WorldScene scene = this.getEmptyScene();
        scene.placeImageXY(this.player1.drawOnto(
                this.board.drawOnto(
                        new RectangleImage(this.BACKGROUND_SIZE,
                                this.BACKGROUND_SIZE,
                                OutlineMode.SOLID, new Color(0x80)), this.waterHeight)),
                this.BACKGROUND_SIZE / 2, this.BACKGROUND_SIZE / 2);
        return scene;
    }
}


class ExamplesIsland 
{
    
    void testIsland(Tester t) {
        ForbiddenIslandWorld game = new ForbiddenIslandWorld();
        game.bigBang(ForbiddenIslandWorld.BACKGROUND_SIZE, ForbiddenIslandWorld.BACKGROUND_SIZE, 0.0001);
    }

    public static void main(String[] args)
    {
        ForbiddenIslandWorld game = new ForbiddenIslandWorld();
        game.bigBang(ForbiddenIslandWorld.BACKGROUND_SIZE, ForbiddenIslandWorld.BACKGROUND_SIZE, 0.01);
    }
}

