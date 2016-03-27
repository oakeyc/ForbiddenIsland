import java.util.ArrayList; 
import tester.*; 
import javalib.impworld.*; 
import java.awt.Color; 
import javalib.worldimages.*; 

// represents a cell
class Cell 
{
    // the height 
    double height;
    // position
    int x, y;
    // the cells around this cell
    Cell left, top, right, bottom;
    // whether covered by water
    boolean isFlooded;

    public static final int CELL_SIZE = 1;

    Cell(int x, int y, int height, Cell left, Cell right, Cell top, Cell bottom, boolean isFlooded)
    {

    }
}

// represents a water cell
class OceanCell extends Cell
{
    OceanCell(int x, int y, int height, Cell left, Cell right, Cell top, Cell bottom, boolean isFlooded) {
        super(x, y, height, left, right, top, bottom, isFlooded);
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

class Board
{
    ArrayList<ArrayList<Cell>> board;

    Board(ArrayList<ArrayList<Double>> cellHeights)
    {
        board = new ArrayList<ArrayList<Cell>>();

        for (int i = 0; i < cellHeights.size(); i++)
        {
            for (int j = 0; j < cellHeights.get(i).size(); i++)
            {
                // depends on ctor for Cell
                board.get(i).set(j, new Cell(i, j, cellHeights.get(i).get(j), 
                        ... , cellHeights.get(i).get(j) - waterheight));
            }
        }
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

