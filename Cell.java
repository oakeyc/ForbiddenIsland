import java.util.ArrayList; 
import tester.*; 
import javalib.impworld.*; 
import java.awt.Color; 
import javalib.worldimages.*; 

// A cell of land.
class Cell {
    static final int CELL_SIZE = 20;
    
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
            cell = new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                    new Color(0x0000FF));
        }
        else {
            cell = new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE, OutlineMode.SOLID,
                    new Color(0x00FF00));
        }
        return new OverlayImage(cell, background);
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
    WorldImage drawOnto(WorldImage background) {
        return new OverlayImage(new RectangleImage(Cell.CELL_SIZE, Cell.CELL_SIZE,
                OutlineMode.SOLID, Color.BLUE), background);
    }
}



// A board representing an island.
class Board {
    ArrayList<ArrayList<Cell>> cells;
    
    Board() {
        this.makeMountainBoard();
    }
    
    void makeMountainBoard() {
        ArrayList<ArrayList<Double>> hieghts = new ArrayList<ArrayList<Double>>();
        double middle = ForbiddenIslandWorld.ISLAND_SIZE / 2.0;
        
        for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE; i++) {
            hieghts.add(new ArrayList<Double>());
            for (int j = 0; j < ForbiddenIslandWorld.ISLAND_SIZE; j++) {
                
            }
        }
    }
    
    ArrayList<ArrayList<Cell>> 
    doubleListToCellList(ArrayList<ArrayList<Double>> heights) {
        ArrayList<ArrayList<Cell>> result = new ArrayList<ArrayList<Cell>>();
        
        for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE; i++) {
            result.add(new ArrayList<Cell>());
            for (int j = 0; j < ForbiddenIslandWorld.ISLAND_SIZE; j++) {
                result.get(i).add(new Cell(heights.get(i).get(j), i, j));
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
                return board.getCell(x, y + 1) != null;
                break;
            case "down":
                return board.getCell(x, y - 1) != null;
                break;
            case "left":
                return board.getCell(x - 1, y) != null;
                break;
            case "right":
                return board.getCell(x + 1, y) != null;
                break;
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

    WorldImage drawOnto()
    {
        return new FromFileImage("pilot-icon.png");
    }
}

class ForbiddenIslandWorld extends World
{
    ArrayList<ArrayList<Cell>> board; // all the cells
    ArrayList<ArrayList<Double>> heights; // a record of all the cell's heights
    ArrayList<String> undo1;
    int waterHeight; // the height of the water
    static final int ISLAND_SIZE = 64; // constant val
    static final int BACKGROUND_SIZE = Cell.CELL_SIZE * ISLAND_SIZE;
    Player player1;
    final int waterIncrease;

    ForbiddenIslandWorld()
    {
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
            case "r": // random island
                board.makeRandomBoard();
                break;
            case "d": // diagonal island
                board.makeDiamondBoard();
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
        
        board.updateBoard();
    }

    public WorldScene makeScene()
    {
        return this.getEmptyScene()
                .placeImageXY(this.player1.drawOnto(), this.player1.x, this.player1.y)
                .placeImageXY(this.board.drawOnto(), this.board.x, this.board.y);
    }
}


class ExampleIsland 
{

    public static void main(String[] args)
    {
        ForbiddenIslandWorld game = new ForbiddenIslandWorld();
        game.bigBang(w, h);
    }
}

