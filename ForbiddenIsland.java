// Assignment 9
// Oka Courtney
// okac
// Obermiller Karl
// obermillerk


import java.util.ArrayList; 
import java.util.Iterator;
import java.util.Random;
import tester.*; 
import javalib.impworld.*;
import java.awt.Color; 
import javalib.worldimages.*; 

// A cell of land.
class Cell {
    static final int CELL_SIZE = 15; // size of the drawn cell
    double height;
    int r;
    int c; // position
    Cell left;
    Cell top;
    Cell right;
    Cell bottom; // neighboring cells
    boolean isFlooded; // is this cell flooded?
    boolean willFlood; // will this cell flood next update?
    boolean hasPart; // does this cell have a helicopter part?

    // initializes data
    Cell(double height, int r, int c) {
        this.height = height;
        this.r = r;
        this.c = c;
        this.isFlooded = false;
        this.hasPart = false;
        this.left = this.top = this.right = this.bottom = null;
    }

    // takes in a new state for isFlooded
    // EFFECT: Sets the flooded state of this cell to the given boolean.
    void setFlooded(boolean isFlooded) {
        this.isFlooded = isFlooded;
    }
    
    // is this cell flooded?
    boolean isFlooded() {
        return this.isFlooded;
    }

    // takes in neighboring cells
    // EFFECT: sets this neighboring cells to those
    void setNeighbors(Cell left, Cell top, Cell right, Cell bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
    
    // gets the neighboring cell in the specified direction.
    Cell getNeighbor(String dir) {
        if (dir.equals("up")) {
            return this.top;
        }
        if (dir.equals("down")) {
            return this.bottom;
        }
        if (dir.equals("left")) {
            return this.left;
        }
        if (dir.equals("right")) {
            return this.right;
        }
        throw new IllegalArgumentException("Input must be a direction");
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
        int offset = -ForbiddenIslandWorld.BACKGROUND_SIZE / 2 + Cell.CELL_SIZE / 2;
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
                this.c * Cell.CELL_SIZE + offset,
                this.r * Cell.CELL_SIZE + offset,
                background);
    }
    
    // Determines if this cell will flood during the next step based on the given waterHeight.
    // EFFECT: Sets the willFlood flag for the next update step accordingly.
    void willFlood(int waterHeight) {
        this.willFlood = 
                !this.isFlooded &&
                this.height < waterHeight &&
                this.isNextToFloodedCell();
    }

    // sets this cell to flooded if the willFlood flag is true.
    // EFFECT: changes the flooded flag based on the willFlood flag.
    void update() {
        if (this.willFlood) {
            this.setFlooded(true);
        }
        this.willFlood = false;
    }

    // whether this cell neighbors a flooded cell
    boolean isNextToFloodedCell() {
        return this.left.isFlooded() || this.right.isFlooded() ||
                this.bottom.isFlooded() || this.top.isFlooded();
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
        return -that.height;
    }

    // Draws this ocean cell onto the given background.
    WorldImage drawOnto(WorldImage background, int waterHeight) {
        return background;
    }
}

//A list of T.
interface IList<T> extends Iterable<T> {
    // Is this list a cons?
    boolean isCons();
}

//An empty list of T.
class Empty<T> implements IList<T> {

    // Returns a new iterator for this empty list.
    @Override
    public Iterator<T> iterator() {
        return new IListIterator<T>(this);
    }

    // Is this empty list a cons?
    public boolean isCons() {
        return false;
    }
}

//A non-empty list of T.
class Cons<T> implements IList<T> {
    T first;
    IList<T> rest;

    Cons(T first, IList<T> rest) {
        this.first = first;
        this.rest = rest;
    }

    // Creates an IList out of the given array list.
    Cons(ArrayList<T> alist) {
        if (alist.isEmpty()) {
            throw new IllegalArgumentException("List must not be empty.");
        }
        this.first = alist.remove(0);
        if (alist.isEmpty()) {
            this.rest = new Empty<T>();
        }
        else {
            this.rest = new Cons<T>(alist);
        }
    }

    // Returns a new iterator for this non-empty list.
    @Override
    public Iterator<T> iterator() {
        return new IListIterator<T>(this);
    }

    // Is this non-empty list a cons?
    public boolean isCons() {
        return true;
    }
}

//An iterator for a list of T.
class IListIterator<T> implements Iterator<T> {
    IList<T> list;

    IListIterator(IList<T> list) {
        this.list = list;
    }

    // Does the list have another element?
    public boolean hasNext() {
        return this.list.isCons();
    }

    // Retrieve the next element of the list.
    public T next() {
        Cons<T> cons = (Cons<T>) this.list;
        T ret = cons.first;
        this.list = cons.rest;
        return ret;
    }

    // removes, but not needed
    public void remove()
    {
        // not a needed function
    }
}

// represents a player
class Player
{
    int r, c; // position
    int steps; // number of steps player has taken
    Cell curr; // current cell player is on.

    // initializes data
    Player(int r, int c, Cell curr) {
        this.r = r;
        this.c = c;
        this.steps = 0;
        this.curr = curr;
    }

    // whether the next spot is valid
    boolean canMove(String move)
    {
        Cell next = curr.getNeighbor(move);
        return !next.isFlooded();// && this.curr.r != next.r && this.curr.c != next.c;
    }

    // moves this player given a direction
    void move(String move)
    {
        //if (canMove(move))
        {
            Cell next = curr.getNeighbor(move);
            System.out.println(this.r - next.r > 0);
            System.out.println(this.c - next.c > 0);
            this.curr = next;
            this.r = this.curr.r;
            this.c = this.curr.c;
        }
    }

    // given an image, it draws this image onto it
    WorldImage drawOnto(WorldImage background)
    {
        int offset = -ForbiddenIslandWorld.BACKGROUND_SIZE / 2 + Cell.CELL_SIZE / 2;
        
        return new OverlayOffsetImage(
                new ScaleImage(
                        new FromFileImage("Images/pilot-icon.png"), 0.5),
                this.c * Cell.CELL_SIZE + offset,
                this.r * Cell.CELL_SIZE + offset,
                background);
    }
}

// represents a forbidden island game that is aw world
class ForbiddenIslandWorld extends World
{
    IList<Cell> board; // all the cells
    int waterHeight; // the height of the water
    static final int ISLAND_SIZE = 64; // constant val
    static final int BACKGROUND_SIZE = Cell.CELL_SIZE * ISLAND_SIZE;
    Player player1;
    final int waterIncrease = 1;
    int tick; // time

    // initializes data
    ForbiddenIslandWorld()
    {
        newBoard("m");
    }
    
    boolean onBoard(int r, int c) {
        return r >= 0 && r < ISLAND_SIZE &&
                c >= 0 && c < ISLAND_SIZE;
    }
    
    // Returns the cell in the given position on the board.
    Cell cellAt(int r, int c) {
        for (Cell cell : board) {
            if (cell.r == r && cell.c == c) {
                return cell;
            }
        }
        return null;
    }
    
    void newBoard(String type) {
        if (type.equals("m")) {
            this.makeMountainBoard();
        }
        else if(type.equals("r")) {
            this.makeRandomBoard();
        }
        else if(type.equals("t")) {
            this.makeTerrainBoard();
        }
        else {
            throw new IllegalArgumentException("Not a valid board type.");
        }
        this.waterHeight = 0;
        this.tick = 0;
        this.player1 = new Player(ISLAND_SIZE / 2, ISLAND_SIZE / 2,
                this.cellAt(ISLAND_SIZE / 2, ISLAND_SIZE / 2));
    }
    
    // takes in a matrix of heights, creates a matrix of cells based on those heights
    ArrayList<ArrayList<Cell>> heightsToCells(ArrayList<ArrayList<Double>> heights) {
        ArrayList<ArrayList<Cell>> result = new ArrayList<ArrayList<Cell>>();

        for (int i = 0; i < heights.size(); i++) {
            ArrayList<Double> hRow = heights.get(i);
            ArrayList<Cell> newRow = new ArrayList<Cell>();
            for (int j = 0; j < hRow.size(); j++) {
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
        for (int i = 0; i < result.size(); i++) {
            ArrayList<Cell> row = result.get(i);
            for (int j = 0; j < row.size(); j++) {
                Cell curr = row.get(j);
                Cell left = curr;
                Cell right = curr;
                Cell top = curr;
                Cell bottom = curr;
                
                if (onBoard(i, j + 1)) 
                {
                    left = result.get(i).get(j + 1);
                }
                if (onBoard(i + 1, j))
                {
                    top = result.get(i + 1).get(j);
                }
                if (onBoard(i, j - 1))
                {
                    right = result.get(i).get(j - 1);
                }
                if (onBoard(i - 1, j))
                {
                    bottom = result.get(i - 1).get(j);
                }
                
                curr.setNeighbors(left, top, right, bottom);
            }
        }

        return result;
    }
    
    // creates a mountain, where the highest point is the center
    // EFFECT: sets the board to a new mountain board.
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
        ArrayList<ArrayList<Cell>> cells = this.heightsToCells(heights);
        ArrayList<Cell> temp = new ArrayList<Cell>();
        for (ArrayList<Cell> row : cells) {
            temp.addAll(row);
        }

        this.board = new Cons<Cell>(temp);
    }
    
    // makes a diamond board with random heights all around
    // EFFECT: sets the board to a new random board.
    void makeRandomBoard() {
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
        ArrayList<ArrayList<Cell>> cells = this.heightsToCells(heights);
        ArrayList<Cell> temp = new ArrayList<Cell>();
        for (ArrayList<Cell> row : cells) {
            temp.addAll(row);
        }

        this.board = new Cons<Cell>(temp);
    }
    
    void makeTerrainBoard() {
        ArrayList<ArrayList<Double>> heights = new ArrayList<ArrayList<Double>>();
        for (int i = 0; i < ISLAND_SIZE; i++) {
            ArrayList<Double> row = new ArrayList<Double>();
            for (int j = 0; j < ISLAND_SIZE; j++) {
                row.add(0.0);
            }
            heights.add(row);
        }
        
        heights.get(0).set(ISLAND_SIZE / 2, 1.0);
        heights.get(ISLAND_SIZE - 1).set(ISLAND_SIZE / 2, 1.0);
        heights.get(ISLAND_SIZE / 2).set(0, 1.0);
        heights.get(ISLAND_SIZE / 2).set(ISLAND_SIZE - 1, 1.0);
        heights.get(ISLAND_SIZE / 2).set(ISLAND_SIZE / 2, ISLAND_SIZE / 2.0);
        
        // creates the cells based on the heights given
        ArrayList<ArrayList<Cell>> cells = this.heightsToCells(heights);
        ArrayList<Cell> temp = new ArrayList<Cell>();
        for (ArrayList<Cell> row : cells) {
            temp.addAll(row);
        }
        
        int mid = ISLAND_SIZE / 2;
        int max = ISLAND_SIZE - 1;
        
        heights = this.makeTerrainHelper(heights, 0, mid, 0, mid,
                true, true, true, true);
        heights = this.makeTerrainHelper(heights, 0, mid, mid, max,
                false, true, true, true);
        heights = this.makeTerrainHelper(heights, mid, max, 0, mid,
                true, false, true, true);
        heights = this.makeTerrainHelper(heights, mid, max, mid, max,
                false, false, true, true);

        this.board = new Cons<Cell>(temp);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    // makes a random terrain board
//    void makeTerrainBoard() {
//        ArrayList<ArrayList<Double>> heights = new ArrayList<ArrayList<Double>>();
//        for (int i = 0; i < ISLAND_SIZE; i++) {
//            ArrayList<Double> row = new ArrayList<Double>();
//            for (int j = 0; j < ISLAND_SIZE; j++) {
//                row.add(0.0);
//            }
//            heights.add(row);
//        }
//
//        int max = ISLAND_SIZE - 1;
//        int mid = ISLAND_SIZE / 2;
//
//        heights.get(mid).set(0, 1.0);
//        heights.get(mid).set(max, 1.0);
//        heights.get(0).set(mid, 1.0);
//        heights.get(max).set(mid, 1.0);
//        heights.get(mid).set(mid, ISLAND_SIZE / 2.0);
//        
//        heights = this.makeTerrainHelper(heights, 0, mid, 0, mid,
//                true, true, true, true);
//        heights = this.makeTerrainHelper(heights, 0, mid, mid, max,
//                false, true, true, true);
//        heights = this.makeTerrainHelper(heights, mid, max, 0, mid,
//                true, false, true, true);
//        heights = this.makeTerrainHelper(heights, mid, max, mid, max,
//                false, false, true, true);
//
//        // creates the cells based on the heights given
//        ArrayList<ArrayList<Cell>> cells = this.heightsToCells(heights);
//        ArrayList<Cell> temp = new ArrayList<Cell>();
//        for (ArrayList<Cell> row : cells) {
//            temp.addAll(row);
//        }
//        
//        this.board = new Cons<Cell>(temp);
//    }
//    
//    
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
        
        Double l = heights.get(midRow).get(cmin);
        Double t = heights.get(rmin).get(midCol);
        Double r = heights.get(midRow).get(cmax);
        Double b = heights.get(rmax).get(midCol);
        
        if (onLeft) {
            l = rand.nextDouble() * 2 * var - var + (tl + bl) / 2;
            if (l < 0) l = 0.0;
        }
        if (onTop) {
            t = rand.nextDouble() * 2 * var - var + (tl + tr) / 2;
            if (t < 0) t = 0.0;
        }
        if (onRight) {
            r = rand.nextDouble() * 2 * var - var + (tr + br) / 2;
            if (r < 0) r = 0.0;
        }
        if (onBot) {
            b = rand.nextDouble() * 2 * var - var + (bl + br) / 2;
            if (b < 0) b = 0.0;
        }
        
        Double m = rand.nextDouble() * 2 * var - var + (l + t + r + b) / 4;
        if (m < 0) m = 0.0;
        
        if (rmax - rmin > 1  || cmax - cmin > 1) {
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
//        else {
//            if (rmax - rmin >= 2) {
//                heights.get(midRow).set(cmin, l);
//                heights.get(midRow).set(cmax, r);                
//                heights = this.makeTerrainHelper(heights, rmin, midRow, cmin, cmax,
//                        onLeft, onTop, onRight, onBot);
//                heights = this.makeTerrainHelper(heights, midRow, rmax, cmin, cmax,
//                        onLeft, false, onRight, onBot);
//            }
//            else if (cmax - cmin >= 2) {
//                heights.get(rmin).set(midCol, t);
//                heights.get(rmax).set(midCol, b);
//                heights = this.makeTerrainHelper(heights, rmin, rmax, cmin, midCol,
//                        onLeft, onTop, onRight, onBot);
//                heights = this.makeTerrainHelper(heights, rmin, rmax, midCol, cmax,
//                        false, onTop, onRight, onBot);
//            }
//        }

        return heights;
    }

    // changes the state of the world given a key stroke
    public void onKeyEvent(String key)
    {
        // moves the player
        if (key.equals("up") || key.equals("down")
                || key.equals("left") || key.equals("right")) {
            this.player1.move(key);
        }
        // new islands
        else if (key.equals("m") || key.equals("r") || key.equals("t")) {
            this.newBoard(key);
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
            for (Cell cell : this.board) {
                cell.willFlood(this.waterHeight);
            }
            for (Cell cell : this.board) {
                cell.update();
            }
        //}
    }

    // makes the scene with all the images
    public WorldScene makeScene()
    {
        WorldScene scene = this.getEmptyScene();
        WorldImage image = new RectangleImage(BACKGROUND_SIZE, BACKGROUND_SIZE,
                OutlineMode.SOLID, new Color(0x80));
        for (Cell cell : this.board) {
            image = cell.drawOnto(image, this.waterHeight);
        }
        
        image = this.player1.drawOnto(image);
        
        scene.placeImageXY(
                image,
                BACKGROUND_SIZE / 2, BACKGROUND_SIZE / 2);
        return scene;
    }

    // when the games over
    // the player drowns
//    public WorldEnd worldEnds()
//    {
//        if (!board.notFlooded(player1.x, player1.y))
//        {
//            WorldScene scene = this.getEmptyScene();
//            scene.placeImageXY(this.lastImage(), 
//                    this.BACKGROUND_SIZE / 2, this.BACKGROUND_SIZE / 2);
//            
//            return new WorldEnd(true, scene);
//        }
//        return new WorldEnd(false, this.makeScene());
//    }
    
    // returns the game over screen
    public WorldImage lastImage()
    {
        WorldImage image =
                new RectangleImage(BACKGROUND_SIZE, BACKGROUND_SIZE,
                        OutlineMode.SOLID, new Color(0x80));
        for (Cell cell : this.board) {
            image = cell.drawOnto(image, this.waterHeight);
        }
        
        image = this.player1.drawOnto(image);
        
        return new OverlayImage(new TextImage("GAME OVER! Player's steps: " + player1.steps,
                60, Color.MAGENTA), image);
    }
}


//represents examples for the forbidden island
class ExamplesIsland 
{
    ForbiddenIslandWorld game;
    Cell c1;
    Cell c2;
    Cell c3; 
    Cell c4;
    Cell c4t;
    Cell c4b;
    Cell c4r;
    Cell c4l;

    // initializes the forbidden island world.
    void gameInit() {
        this.game = new ForbiddenIslandWorld();
    }

    // initializes the data for testing
    void cellInit()
    {
        this.c1 = new Cell(0, 0, 0);
        this.c2 = new Cell(5, 0, 0);
        this.c3 = new Cell(10, 10, 15);
        this.c4 = new Cell(12, 5, 5);
        this.c4t = new Cell(13, 5, 4);
        this.c4b = new Cell(9, 5, 6);
        this.c4r = new Cell(13, 6, 5);
        this.c4l = new Cell(11, 4, 5);
        this.c4.setNeighbors(this.c4l, this.c4t, this.c4r, this.c4b);
    }

    // initializes the board data
    void boardInit()
    {
        // initializing data

    }

    /*
     *  To Test:
     *   Other:
     *    ?
     */

    // tests isCons method.
    void testIsCons(Tester t) {
        IList<String> empty = new Empty<String>();
        IList<String> cons = new Cons<String>("Hello", empty);

        t.checkExpect(empty.isCons(), false);
        t.checkExpect(cons.isCons(), true);
    }

    // tests the constructor for cons from an array list.
    void testArrayListCons(Tester t) {
        ArrayList<String> strings = new ArrayList<String>();
        t.checkConstructorException(new IllegalArgumentException("List must not be empty."),
                "Cons", strings);
        strings.add("Hello");
        t.checkExpect(new Cons<String>(strings), new Cons<String>("Hello", new Empty<String>()));
        strings.add("Hello");
        strings.add("World");
        t.checkExpect(new Cons<String>(strings),
                new Cons<String>("Hello", new Cons<String>("World", new Empty<String>())));
    }

    // tests onBoard method.
    void testOnBoard(Tester t) {
        gameInit();
        t.checkExpect(this.game.onBoard(-1, 0), false);
        t.checkExpect(this.game.onBoard(0, -1), false);
        t.checkExpect(this.game.onBoard(0, 0), true);
        t.checkExpect(this.game.onBoard(ForbiddenIslandWorld.ISLAND_SIZE, 0), false);
        t.checkExpect(this.game.onBoard(0, ForbiddenIslandWorld.ISLAND_SIZE), false);
        t.checkExpect(this.game.onBoard(ForbiddenIslandWorld.ISLAND_SIZE - 1,
                ForbiddenIslandWorld.ISLAND_SIZE - 1), true);
    }

    // tests compare height
    void testCellCompareHeight(Tester t)
    {
        cellInit();
        Cell oc = new OceanCell(0, 0);
        t.checkExpect(this.c1.compareHeight(this.c2), -5.0);
        t.checkExpect(this.c2.compareHeight(this.c1), 5.0);
        t.checkExpect(this.c1.compareHeight(this.c1), 0.0);
        t.checkExpect(oc.compareHeight(this.c2), -5.0);
        t.checkExpect(this.c2.compareHeight(oc), 5.0);
    }

    // tests setFlooded
    void testSetFlooded(Tester t) {
        cellInit();
        t.checkExpect(this.c1.isFlooded, false);
        this.c1.setFlooded(false);
        t.checkExpect(this.c1.isFlooded, false);
        this.c1.setFlooded(true);
        t.checkExpect(this.c1.isFlooded, true);
        Cell oc = new OceanCell(0, 0);
        t.checkExpect(oc.isFlooded, true);
        oc.setFlooded(false);
        t.checkExpect(oc.isFlooded, true);
    }

    // tests willFlood method
    void testWillFlood(Tester t) {
        cellInit();
        t.checkExpect(this.c4.willFlood, false);
        this.c4.willFlood(9);
        t.checkExpect(this.c4.willFlood, false);
        c4b.setFlooded(true);
        this.c4.willFlood(10);
        t.checkExpect(this.c4.willFlood, false);
        this.c4.willFlood(13);
        t.checkExpect(this.c4.willFlood, true);
        this.c4.setFlooded(true);
        this.c4.willFlood(13);
        t.checkExpect(this.c4.willFlood, false);
    }

    // tests cell updating
    void testCellUpdate(Tester t)
    {
        cellInit();
        this.c4.willFlood = false;
        this.c4.update();
        t.checkExpect(this.c4.isFlooded, false);
        this.c4.willFlood = true;
        this.c4.update();
        t.checkExpect(this.c4.isFlooded, true);
        this.c4.willFlood = false;
        this.c4.update();
        t.checkExpect(this.c4.isFlooded, true);
    }

    // tests is next to flooded cell
    void testIsNextToFloodedCell(Tester t)
    {
        cellInit();
        t.checkExpect(this.c4.isNextToFloodedCell(), false);
        this.c4b.setFlooded(true);
        t.checkExpect(this.c4.isNextToFloodedCell(), true);
        this.c4b.setFlooded(false);
        this.c4r.setFlooded(true);
        t.checkExpect(this.c4.isNextToFloodedCell(), true);
        this.c4r.setFlooded(false);
        this.c4t.setFlooded(true);
        t.checkExpect(this.c4.isNextToFloodedCell(), true);
        this.c4t.setFlooded(false);
        this.c4l.setFlooded(true);
        t.checkExpect(this.c4.isNextToFloodedCell(), true);
    }

    // tests the setNeighbors method
    void testSetNeighbors(Tester t) {
        cellInit();
        t.checkExpect(this.c1.left, null);
        t.checkExpect(this.c1.top, null);
        t.checkExpect(this.c1.right, null);
        t.checkExpect(this.c1.bottom, null);
        this.c1.setNeighbors(this.c1, this.c2, this.c3, this.c4);
        t.checkExpect(this.c1.left, this.c1);
        t.checkExpect(this.c1.top, this.c2);
        t.checkExpect(this.c1.right, this.c3);
        t.checkExpect(this.c1.bottom, this.c4);
        this.c1.setNeighbors(this.c2, this.c1, this.c4, this.c3);
        t.checkExpect(this.c1.left, this.c2);
        t.checkExpect(this.c1.top, this.c1);
        t.checkExpect(this.c1.right, this.c4);
        t.checkExpect(this.c1.bottom, this.c3);
    }

    // tests the island
    void testIsland(Tester t) {
        this.gameInit();
        this.game.bigBang(ForbiddenIslandWorld.BACKGROUND_SIZE,
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