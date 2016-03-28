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
    int x, y, waterHeight;
    Cell left, top, right, bottom;
    boolean isFlooded, hasPart;
    
    Cell(double height, int x, int y, int waterHeight) {
        this.height = height;
        this.waterHeight = waterHeight;
        this.x = x;
        this.y = y;
        this.isFlooded = false;
        this.hasPart = false;
    }
    
    // EFFECT: Sets the flooded state of this cell to the given boolean.
    void setFlooded(boolean isFlooded) {
        this.isFlooded = isFlooded;
    }
    
    // compares the height of this cell to the height of that cell.
    //  Returns: 0  if height of this and that are equal.
    //           <0 if height of this is less than height of that.
    //           >0 if height of this is greater than height of that.
    double compareHeight(Cell that) {
        return this.height - that.height;
    }
    
    // Draws this cell onto the given background.
    WorldImage drawOnto(WorldImage background) {
        WorldImage cell;
        if (this.isFlooded) {
            int maxHeight = ForbiddenIslandWorld.ISLAND_SIZE / 2;
            double ratio = (this.waterHeight - this.height) / maxHeight;
            int color = (int) (0xFF * (1 - ratio));
            cell = new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                    new Color(color));
        }
        else {
            int color = 0x008000;
            double ratio;
            int islandSize = ForbiddenIslandWorld.ISLAND_SIZE;
            int maxHeight = islandSize / 2;
            if (this.height >= this.waterHeight) {
                ratio = (this.height - this.waterHeight) / maxHeight;
                color += ((int) (0xFF * ratio) * 0x010000) +
                        ((int) (0x79 * ratio) * 0x0100) +
                        (int) (0xFF * ratio);
            }
            else {
                ratio = (this.waterHeight - this.height) / maxHeight;
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
        this.waterHeight = waterHeight;
    }
}


// A cell in the ocean.
class OceanCell extends Cell {
    
    OceanCell(int x, int y, int waterHeight) {
        super(0, x, y, waterHeight);
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
    WorldImage drawOnto(WorldImage background) {
        return background;
    }
}



// A board representing an island.
class Board {
    ArrayList<ArrayList<Cell>> cells;
    int waterHeight;
    
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
                    newRow.add(new OceanCell(i, j, 0));
                }
                else {
                    newRow.add(new Cell(hRow.get(j), i, j, 0));
                }
            }
            result.add(newRow);
        }
        
        return result;
    }
    
    void updateBoard(int waterHeight) {
        this.waterHeight = waterHeight;
        for (ArrayList<Cell> row : cells) {
            for (Cell cell : row) {
                cell.update(waterHeight);
            }
        }
    }
    
    boolean onBoard(int x, int y)
    {
        return this.cells.get(y).size() <= x && x >= 0 && 
                this.cells.size() <= y && y >= 0;
    }
    
    WorldImage drawOnto(WorldImage background) {
        WorldImage image = background;
        
        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {
                image = c.drawOnto(image);
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

    boolean canMove(String move)
    {
        switch (move)
        {
            case "up":
                return board.onBoard(x, y + 1);
            case "down":
                return board.onBoard(x, y - 1);
            case "left":
                return board.onBoard(x - 1, y);
            case "right":
                return board.onBoard(x + 1, y);
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
        }
    }

    WorldImage drawOnto(WorldImage background)
    {
        return new OverlayImage(new FromFileImage("Images/pilot-icon.png"), background);
    }
}

class ForbiddenIslandWorld extends World
{
    Board board; // all the cells
    ArrayList<ArrayList<Double>> heights; // a record of all the cell's heights
    ArrayList<String> undo1;
    int waterHeight; // the height of the water
    static final int ISLAND_SIZE = 64; // constant val
    static final int BACKGROUND_SIZE = Cell.CELL_SIZE * ISLAND_SIZE;
    Player player1;
    final int waterIncrease = 1;

    ForbiddenIslandWorld()
    {
        this.board = new Board();
        this.player1 = new Player();
        // defaults as mountain island
        // probably some code from the board class with the mountain
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
                undo1.add(key);
                break;
            case "u": // undo
                player1.move(this.oppositeDirection(undo1.get(undo1.size())));
                undo1.remove(undo1.size());
                break;
            case "m":
                board.makeMountainBoard();
                break;
            case "r": // random island
                board.makeDiamondBoard();
                break;
            case "t": // terrain island
                board.makeTerrainBoard();
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
        waterHeight += waterIncrease;
        
        board.updateBoard(this.waterHeight);
    }

    public WorldScene makeScene()
    {
        WorldScene scene = this.getEmptyScene();
        scene.placeImageXY(this.player1.drawOnto(
                this.board.drawOnto(
                        new RectangleImage(this.BACKGROUND_SIZE,
                                this.BACKGROUND_SIZE,
                                OutlineMode.SOLID, new Color(0x80)))),
                this.BACKGROUND_SIZE / 2, this.BACKGROUND_SIZE / 2);
        return scene;
    }
}


class ExamplesIsland 
{
    
    void testIsland(Tester t) {
        ForbiddenIslandWorld game = new ForbiddenIslandWorld();
        game.bigBang(ForbiddenIslandWorld.BACKGROUND_SIZE, ForbiddenIslandWorld.BACKGROUND_SIZE);
    }

    public static void main(String[] args)
    {
        ForbiddenIslandWorld game = new ForbiddenIslandWorld();
        game.bigBang(ForbiddenIslandWorld.BACKGROUND_SIZE, ForbiddenIslandWorld.BACKGROUND_SIZE);
    }
}

