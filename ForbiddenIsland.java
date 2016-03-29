import java.util.ArrayList; 
import java.util.Random;
import tester.*; 
import javalib.impworld.*;
import java.awt.Color; 
import javalib.worldimages.*; 

// A cell of land.
class Cell {
    static final int CELL_SIZE = 15; // size of the drawn cell
    double height;
    int r, c; // position
    Cell left, top, right, bottom; // neighboring cells
    boolean isFlooded, willFlood, hasPart; // is flooded with water has a helicopter part

    // initializes data
    Cell(double height, int r, int c) {
        this.height = height;
        this.r = r;
        this.c = c;
        this.isFlooded = false;
        this.hasPart = false;
        this.left = this.top = this.right = this.bottom = null;
    }

    // takes in a new state for flooded
    // EFFECT: Sets the flooded state of this cell to the given boolean.
    void setFlooded(boolean isFlooded) {
        this.isFlooded = isFlooded;
    }

    // takes in neighboring cells
    // EFFECT: sets this neighboring cells to those
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

    // Draws this cell onto the given background
    // changes the colors according to the height and waterheight
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
                this.c * Cell.CELL_SIZE - ForbiddenIslandWorld.BACKGROUND_SIZE / 2.0,
                this.r * Cell.CELL_SIZE - ForbiddenIslandWorld.BACKGROUND_SIZE / 2.0,
                background);
    }
    
    void willFlood(int waterHeight) {
        this.willFlood = 
                !this.isFlooded &&
                this.height < waterHeight &&
                this.isNextToFloodedCell();
    }

    // takes in a waterheight, figures out if this cell has flooded
    // EFFECT: could change the isFlooded
    void update() {
        if (this.willFlood) {
            this.setFlooded(true);
        }
        this.willFlood = false;
    }

    // whether this cell neightbors a flooded cell
    boolean isNextToFloodedCell() {
        return this.left.isFlooded || this.right.isFlooded ||
                this.bottom.isFlooded || this.top.isFlooded;
    }
}


// A cell in the ocean.
class OceanCell extends Cell {

    // initializes data
    OceanCell(int x, int y) {
        super(0, x, y);
        this.isFlooded = true; // always true
    }

    // EFFECT: Sets the flooded state of this ocean cell, which is always true.
    void setFlooded(boolean isFlooded) {}

    // compares the height of this ocean cell to the height of that cell.
    //  Returns: the height of that cell, which is
    //           0  if height of that is even with ocean level.
    //           <0 if height of that is below ocean level.
    //           >0 if height of that is above ocean level.
    double compareHeight(Cell that) {
        return that.height;
    }

    // Draws this ocean cell onto the given background.
    WorldImage drawOnto(WorldImage background, int waterHeight) {
        return background;
    }
}

// A board representing an island.
class Board {
    // the matrix holding all the cells
    ArrayList<ArrayList<Cell>> cells;

    // defaults a mountain board
    Board() {
        this.makeMountainBoard();
    }

    // creates a mountain, where the highest point is the center
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
        // creates the cells based on the heights given
        this.cells = this.doubleListToCellList(heights);
    }

    // makes a diamond board with random heights all around
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
        // creates the cells based on the heights given
        this.cells = this.doubleListToCellList(heights);
    }

 // makes a random terrain board
    void makeTerrainBoard() {
        System.out.println("Terrain");
        ArrayList<ArrayList<Double>> heights = new ArrayList<ArrayList<Double>>();
        for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE; i++) {
            ArrayList<Double> row = new ArrayList<Double>();
            for (int j = 0; j < ForbiddenIslandWorld.ISLAND_SIZE; j++) {
                row.add(0.0);
            }
            System.out.print(row.size() + ", ");
            heights.add(row);
        }
        System.out.println();
        System.out.println(heights.size());

        int max = ForbiddenIslandWorld.ISLAND_SIZE;
        int mid = max / 2;

        heights.get(mid).set(0, 1.0);
        heights.get(mid).set(max - 1, 1.0);
        heights.get(0).set(mid, 1.0);
        heights.get(max - 1).set(mid, 1.0);
        heights.get(mid).set(mid, ForbiddenIslandWorld.ISLAND_SIZE / 2.0);
        
        heights = this.makeTerrainHelper(heights, 0, mid, 0, mid,
                true, true, true, true);
        heights = this.makeTerrainHelper(heights, 0, mid, mid, max - 1,
                false, true, true, true);
        heights = this.makeTerrainHelper(heights, mid, max - 1, 0, mid,
                true, false, true, true);
        heights = this.makeTerrainHelper(heights, mid, max, mid, max - 1,
                false, false, true, true);

        this.cells = this.doubleListToCellList(heights);
    }
    
    
    ArrayList<ArrayList<Double>> makeTerrainHelper
    (ArrayList<ArrayList<Double>> heights, int rmin, int rmax, int cmin, int cmax,
            boolean onLeft, boolean onRight, boolean onTop, boolean onBot) {
        Random rand = new Random();
        double var = ((rmax - rmin) * (cmax - cmin)) / (ForbiddenIslandWorld.ISLAND_SIZE / 2);
        
        Double tl = heights.get(rmin).get(cmin);
        Double tr = heights.get(rmin).get(cmax);
        Double bl = heights.get(rmax).get(cmin);
        Double br = heights.get(rmax).get(cmax);
        
        int midCol = (cmin + cmax) / 2;
        int midRow = (rmin + rmax) / 2;
        
        Double l, t, r, b;
        
        if (onLeft) {
            l = rand.nextDouble() * 2 * var - var + (tl + bl) / 2;
            if (l < 0) l = 0.0;
        }
        else {
            l = heights.get(midRow).get(cmin);
        }
        if (onTop) {
            t = rand.nextDouble() * 2 * var - var + (tl + tr) / 2;
            if (t < 0) t = 0.0;
        }
        else {
            t = heights.get(rmin).get(midCol);
        }
        if (onRight) {
            r = rand.nextDouble() * 2 * var - var + (tr + br) / 2;
            if (r < 0) r = 0.0;
        }
        else {
            r = heights.get(midRow).get(cmax);
        }
        if (onBot) {
            b = rand.nextDouble() * 2 * var - var + (bl + br) / 2;
            if (b < 0) b = 0.0;
        }
        else {
            b = heights.get(rmax).get(midCol);
        }
        
        Double m = rand.nextDouble() * 2 * var - var + (l + t + r + b) / 4;
        if (m < 0) m = 0.0;
        
        if (rmax - rmin >= 2  && cmax - cmin >= 2) {
            heights.get(midRow).set(cmin, l);
            heights.get(midRow).set(cmax, r);
            heights.get(rmin).set(midCol, t);
            heights.get(rmax).set(midCol, b);
            heights.get(midRow).set(midCol, m);

            heights = this.makeTerrainHelper(heights, rmin, midRow, cmin, midCol,
                    onLeft, onTop, onRight, onBot);
            heights = this.makeTerrainHelper(heights, rmin, midRow, midCol, cmax,
                    false, onTop, onRight, onBot);
            heights = this.makeTerrainHelper(heights, midRow, rmax, cmin, midCol,
                    onLeft, false, onRight, onBot);
            heights = this.makeTerrainHelper(heights, midRow, rmax, midCol, cmax,
                    false, false, onRight, onBot);
        }
        else {
            if (rmax - rmin >= 2) {
                heights.get(midRow).set(cmin, l);
                heights.get(midRow).set(cmax, r);                
                heights = this.makeTerrainHelper(heights, rmin, midRow, cmin, cmax,
                        onLeft, onTop, onRight, onBot);
                heights = this.makeTerrainHelper(heights, midRow, rmax, cmin, cmax,
                        onLeft, false, onRight, onBot);
            }
            else if (cmax - cmin >= 2) {
                heights.get(rmin).set(midCol, t);
                heights.get(rmax).set(midCol, b);
                heights = this.makeTerrainHelper(heights, rmin, rmax, cmin, midCol,
                        onLeft, onTop, onRight, onBot);
                heights = this.makeTerrainHelper(heights, rmin, rmax, midCol, cmax,
                        false, onTop, onRight, onBot);
            }
        }

        return heights;
    }
    
    /// testing
    ArrayList<ArrayList<Double>> makeTerrainBoardHelper
    (ArrayList<ArrayList<Double>> heights, int rmin, int rmax, int cmin, int cmax) {
        Random rand = new Random();
        double var = ((rmax - rmin) * (cmax - cmin)) / (ForbiddenIslandWorld.ISLAND_SIZE / 2);
        
        Double tl = heights.get(rmin).get(cmin);
        Double tr = heights.get(rmin).get(cmax);
        Double bl = heights.get(rmax).get(cmin);
        Double br = heights.get(rmax).get(cmax);
        
        int midCol = (cmin + cmax) / 2;
        int midRow = (rmin + rmax) / 2;
        Double l = rand.nextDouble() * 2 * var - var + (tl + bl) / 2;
        if (l < 0) l = 0.0;
        Double t = rand.nextDouble() * 2 * var - var + (tl + tr) / 2;
        if (t < 0) t = 0.0;
        Double r = rand.nextDouble() * 2 * var - var + (tr + br) / 2;
        if (r < 0) r = 0.0;
        Double b = rand.nextDouble() * 2 * var - var + (bl + br) / 2;
        if (b < 0) b = 0.0;
        Double m = rand.nextDouble() * 2 * var - var + (l + t + r + b) / 4;
        if (m < 0) m = 0.0;

        if (rmax - rmin >= 2  && cmax - cmin >= 2) {
            heights.get(midRow).set(cmin, l);
            heights.get(midRow).set(cmax, r);
            heights.get(rmin).set(midCol, t);
            heights.get(rmax).set(midCol, b);
            heights.get(midRow).set(midCol, m);

            heights = this.makeTerrainBoardHelper(heights, midRow, rmax, midCol, cmax);
            heights = this.makeTerrainBoardHelper(heights, midRow, rmax, cmin, midCol);
            heights = this.makeTerrainBoardHelper(heights, rmin, midRow, midCol, cmax);
            heights = this.makeTerrainBoardHelper(heights, rmin, midRow, cmin, midCol);
        }
        else {
            if (rmax - rmin >= 2) {
                heights.get(midRow).set(cmin, l);
                heights.get(midRow).set(cmax, r);                
                heights = this.makeTerrainBoardHelper(heights, rmin, midRow, cmin, cmax);
                heights = this.makeTerrainBoardHelper(heights, midRow, rmax, cmin, cmax);
            }
            else if (cmax - cmin >= 2) {
                heights.get(rmin).set(midCol, t);
                heights.get(rmax).set(midCol, b);
                heights = this.makeTerrainBoardHelper(heights, rmin, rmax, cmin, midCol);
                heights = this.makeTerrainBoardHelper(heights, rmin, rmax, midCol, cmax);
            }
        }

        return heights;
    }
    

    // makes a random terrain border
//    ArrayList<ArrayList<Double>> makeTerrainBoardHelper
//    (ArrayList<ArrayList<Double>> heights, int rmin, int rmax, int cmin, int cmax) {
//        Random rand = new Random();
//        double var = ((rmax - rmin) * (cmax - cmin)) / (ForbiddenIslandWorld.ISLAND_SIZE / 2);
//        
//        Double tl = heights.get(rmin).get(cmin);
//        Double tr = heights.get(rmin).get(cmax);
//        Double bl = heights.get(rmax).get(cmin);
//        Double br = heights.get(rmax).get(cmax);
//        
//        int midCol = (cmin + cmax) / 2;r
//        int midRow = (rmin + rmax) / 2;
//        Double l = rand.nextDouble() * 2 * var - var + (tl + bl) / 2;
//        if (l < 0) l = 0.0;
//        Double t = rand.nextDouble() * 2 * var - var + (tl + tr) / 2;
//        if (t < 0) t = 0.0;
//        Double r = rand.nextDouble() * 2 * var - var + (tr + br) / 2;
//        if (r < 0) r = 0.0;
//        Double b = rand.nextDouble() * 2 * var - var + (bl + br) / 2;
//        if (b < 0) b = 0.0;
//        Double m = rand.nextDouble() * 2 * var - var + (l + t + r + b) / 4;
//        if (m < 0) m = 0.0;
//
//        if (rmax - rmin >= 2  && cmax - cmin >= 2) {
//            heights.get(midRow).set(cmin, l);
//            heights.get(midRow).set(cmax, r);
//            heights.get(rmin).set(midCol, t);
//            heights.get(rmax).set(midCol, b);
//            heights.get(midRow).set(midCol, m);
//
//            heights = this.makeTerrainBoardHelper(heights, midRow, rmax, midCol, cmax);
//            heights = this.makeTerrainBoardHelper(heights, midRow, rmax, cmin, midCol);
//            heights = this.makeTerrainBoardHelper(heights, rmin, midRow, midCol, cmax);
//            heights = this.makeTerrainBoardHelper(heights, rmin, midRow, cmin, midCol);
//        }
//        else {
//            if (rmax - rmin >= 2) {
//                heights.get(midRow).set(cmin, l);
//                heights.get(midRow).set(cmax, r);                
//                heights = this.makeTerrainBoardHelper(heights, rmin, midRow, cmin, cmax);
//                heights = this.makeTerrainBoardHelper(heights, midRow, rmax, cmin, cmax);
//            }
//            else if (cmax - cmin >= 2) {
//                heights.get(rmin).set(midCol, t);
//                heights.get(rmax).set(midCol, b);
//                heights = this.makeTerrainBoardHelper(heights, rmin, rmax, cmin, midCol);
//                heights = this.makeTerrainBoardHelper(heights, rmin, rmax, midCol, cmax);
//            }
//        }
//
//        return heights;
//    }

    // takes in a matrix of heights, creates a matrix of cells based on those heights
    // EFFECT: initializes the matrix of cells
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

        // updates the neighboring cells for each cell
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

    // updates all the cells on the board given a new water height
    // EFFECT: changes the states of the cells
    void updateBoard(int waterHeight) {
        for (ArrayList<Cell> row : this.cells) {
            for (Cell cell : row) {
                cell.willFlood(waterHeight);
            }
        }
        
        for (ArrayList<Cell> row : this.cells) {
            for (Cell cell : row) {
                cell.update();
            }
        }
    }

    // given a position, returns whether it is on the board or not
    boolean onBoard(int x, int y)
    {
        return ForbiddenIslandWorld.ISLAND_SIZE > x && x >= 0 && 
                ForbiddenIslandWorld.ISLAND_SIZE > y && y >= 0;
    }

    // takes in a position
    // returns whether a cell is flooded
    boolean notFlooded(int x, int y)
    {
        return !cells.get(y).get(x).isFlooded;
    }

    // given an image and a water height, draws this board onto the image
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

// represents a player
class Player
{
    int x, y; // position
    int steps; // number of steps player has taken
    Board board; // this board it is on

    // initializes data
    Player(int x, int y, Board board) {
        this.x = x;
        this.y = y;
        this.steps = 0;
        this.board = board;
    }

    // whether the next spot is valid
    boolean canMove(String move)
    {
        switch (move)
        {
            case "up":
                return board.onBoard(x, y + 1) && board.notFlooded(x, y + 1);
            case "down":
                return board.onBoard(x, y - 1) && board.notFlooded(x, y - 1);
            case "left":
                return board.onBoard(x + 1, y) && board.notFlooded(x + 1, y);
            case "right":
                return board.onBoard(x - 1, y) && board.notFlooded(x - 1, y);
            default:
                return false;
        }   
    }

    // moves this player given a direction
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
                    x += 1;
                    break;
                case "right":
                    x -= 1;
                    break;
                default:
                    break;
            }
            this.steps++;
        }
    }

    // given an image, it draws this image onto it
    WorldImage drawOnto(WorldImage background)
    {
        return new OverlayOffsetImage(
                new ScaleImage(
                        new FromFileImage("Images/pilot-icon.png"), 0.5),
                this.x * Cell.CELL_SIZE - ForbiddenIslandWorld.BACKGROUND_SIZE / 2.0,
                this.y * Cell.CELL_SIZE - ForbiddenIslandWorld.BACKGROUND_SIZE / 2.0,
                background);
    }
}

// represents a forbidden island game that is aw world
class ForbiddenIslandWorld extends World
{
    Board board; // all the cells
    int waterHeight; // the height of the water
    static final int ISLAND_SIZE = 64; // constant val
    static final int BACKGROUND_SIZE = Cell.CELL_SIZE * ISLAND_SIZE;
    Player player1;
    final int waterIncrease = 1;
    int tick; // time

    // initializes data
    ForbiddenIslandWorld()
    {
        this.board = new Board();
        this.player1 = new Player(ISLAND_SIZE / 2, ISLAND_SIZE / 2, this.board);
        this.waterHeight = 0;
        this.tick = 0;
    }

    // changes the state of the world given a key stroke
    public void onKeyEvent(String key)
    {
        switch(key)
        {
            case "up":
            case "down":
            case "left":
            case "right":
                this.player1.move(key); // moves the player
                break;
            case "m": // mountain island
                board.makeMountainBoard();
                this.waterHeight = 0;
                this.tick = 0;
                break;
            case "r": // random island
                board.makeDiamondBoard();
                this.waterHeight = 0;
                this.tick = 0;
                break;
            case "t": // terrain island
                board.makeTerrainBoard();
                this.waterHeight = 0;
                this.tick = 0;
                break;
        }
    }

    // returns the opposite direction of the given string
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

    // changes the state of the world
    // EFFECT: increases the water height and updates the board
    public void onTick()
    {
        this.tick++;
        //if (tick % 10 == 0) {
            waterHeight += waterIncrease;
            board.updateBoard(this.waterHeight);
        //}
    }

    // makes the scene with all the images
    public WorldScene makeScene()
    {
        WorldScene scene = this.getEmptyScene();
        scene.placeImageXY(this.player1.drawOnto( // not placed
                this.board.drawOnto(
                        new RectangleImage(this.BACKGROUND_SIZE, this.BACKGROUND_SIZE,
                                OutlineMode.SOLID, new Color(0x80)), this.waterHeight)),
                this.BACKGROUND_SIZE / 2, this.BACKGROUND_SIZE / 2);
        return scene;
    }

    // when the games over
    // the player drowns
    public WorldEnd worldEnds()
    {
        if (!board.notFlooded(player1.x, player1.y))
        {
            WorldScene scene = this.getEmptyScene();
            scene.placeImageXY(this.lastImage(), 
                    this.BACKGROUND_SIZE / 2, this.BACKGROUND_SIZE / 2);
            
            return new WorldEnd(true, scene);
        }
        return new WorldEnd(false, this.makeScene());
    }
    
    // returns the game over screen
    public WorldImage lastImage()
    {
        WorldImage image = this.player1.drawOnto( // not placed
                this.board.drawOnto(
                        new RectangleImage(this.BACKGROUND_SIZE, this.BACKGROUND_SIZE,
                                OutlineMode.SOLID, new Color(0x80)), this.waterHeight));
        return new OverlayImage(new TextImage("GAME OVER! Player's steps: " + player1.steps,
                60, Color.MAGENTA), image);
        
    }
}

//represents examples for the forbidenisland
class ExamplesIsland 
{
    Cell c1;
    Cell c2;
    Cell c3; 
    Cell c4;
    Cell c4t;
    Cell c4b;
    Cell c4r;
    Cell c4l;

    Board b1;
    Board b2;
    Board b3;

    // initializes the data for testing
    void cellInit()
    {
        c1 = new Cell(0, 0, 0);
        c2 = new Cell(5, 0, 0);
        c3 = new Cell(10, 10, 15);
        c4 = new Cell(12, 5, 5);
        c4t = new Cell(13, 5, 4);
        c4b = new Cell(11, 5, 6);
        c4r = new Cell(13, 6, 5);
        c4l = new Cell(11, 4, 5);
        c4.setNeighbors(c4l, c4t, c4r, c4b);
    }

    // initializes the board data
    void boardInit()
    {
        b1 = new Board();
        b2 = new Board();
        b3 = new Board();
    }
/*
    // tests compare height
    boolean testCellCompareHeight(Tester t)
    {
        cellInit();
        return t.checkExpect(c1.compareHeight(c2), -5.0) && t.checkExpect(c2.compareHeight(c1), 5.0);
    }

    // tests cell updating
    boolean testCellUpdate(Tester t)
    {
        cellInit();
        c4.update(10);
        boolean c4u = c4.isFlooded;
        c4r.setFlooded(true);
        c4.update(30);
        boolean c4u2 = c4.isFlooded;

        return t.checkExpect(c4u, false) && t.checkExpect(c4u2, true);
    }

    // tests is next to flooded cell
    boolean testIsNextToFloodedCell(Tester t)
    {
        cellInit();

        boolean c4nf = c4.isNextToFloodedCell();
        c4b.setFlooded(true);
        boolean c4nf1 = c4.isNextToFloodedCell();

        return t.checkExpect(c4nf, false) && t.checkExpect(c4nf1, true);
    }*/

    // tests the island
    void testIsland(Tester t) {
        ForbiddenIslandWorld game = new ForbiddenIslandWorld();
        game.bigBang(ForbiddenIslandWorld.BACKGROUND_SIZE,
                ForbiddenIslandWorld.BACKGROUND_SIZE,
                0.01);
    }
 // main, runs the class
    public static void main(String[] args) {
        ForbiddenIslandWorld game = new ForbiddenIslandWorld();
        game.bigBang(ForbiddenIslandWorld.BACKGROUND_SIZE,
                ForbiddenIslandWorld.BACKGROUND_SIZE,
                0.01);
    }
}
