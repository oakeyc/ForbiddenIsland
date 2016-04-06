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
    int r; // row position
    int c; // column position
    Cell left;
    Cell top;
    Cell right;
    Cell bottom; // neighboring cells
    boolean isFlooded; // is this cell flooded?
    Target t;

    // initializes data
    Cell(double height, int r, int c) {
        this.height = height;
        this.r = r;
        this.c = c;
        this.isFlooded = false;
        this.t = null;
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
    // Returns: 0 if height of this and that are equal.
    // <0 if height of this is less than height of that.
    // >0 if height of this is greater than height of that.
    double compareHeight(Cell that) {
        return this.height - that.height;
    }

    // Draws this cell onto the given background
    // changes the colors according to the height and waterheight
    WorldImage drawOnto(WorldImage background, int waterHeight) {
        int offset = -ForbiddenIslandWorld.BACKGROUND_SIZE / 2 + CELL_SIZE * 2;

        int color = this.getColor(waterHeight);

        WorldImage cell = new RectangleImage(CELL_SIZE, CELL_SIZE, "solid",
                new Color(color));

        if (this.t != null) 
        {
            cell = t.drawOnto(cell);
        }
        return new OverlayOffsetImage(cell, 
                (ForbiddenIslandWorld.ISLAND_SIZE - this.c - 1) * CELL_SIZE + offset,
                (ForbiddenIslandWorld.ISLAND_SIZE - this.r - 1) * CELL_SIZE + offset, 
                background);
    }

    // calculates the color, as and int, that this cell should be
    // based on its height and the given water height.
    int getColor(int waterHeight) {
        int color;
        if (this.isFlooded) {
            int maxHeight = ForbiddenIslandWorld.ISLAND_SIZE / 2;
            double ratio = Math.min((waterHeight - this.height) / maxHeight, 1);
            color = (int) (0x80 * (1 - ratio));
        }
        else {
            color = 0x008000;
            double ratio;
            int islandSize = ForbiddenIslandWorld.ISLAND_SIZE;
            int maxHeight = islandSize / 2;
            if (this.height >= waterHeight) {
                ratio = Math.min((this.height - waterHeight) / maxHeight, 1);
                color += ((int) (0xFF * ratio) * 0x010000)
                        + ((int) (0x79 * ratio) * 0x0100)
                        + (int) (0xFF * ratio);
            }
            else {
                ratio = Math.min((waterHeight - this.height) / maxHeight, 1);
                color = ((int) (0x80 * (1 - ratio)) * 0x0100)
                        + ((int) (0x80 * ratio) * 0x010000);
            }
        }

        return color;
    }

    // sets this cell to flooded if the willFlood flag is true.
    // EFFECT: changes the flooded flag if this cell is next to a flooded cell
    // and its height is below the given waterHeight.
    // (only gets called on coastline cells, which are not flooded and are next
    // to flooded)
    void update(int waterHeight) {
        if (!this.isFlooded && this.height < waterHeight) {
            this.setFlooded(true);
            this.left.update(waterHeight);
            this.top.update(waterHeight);
            this.right.update(waterHeight);
            this.bottom.update(waterHeight);
        }
    }

    // if there is a helicopter piece, there will no longer have one
    Target cellLosePart() {
        if (this.t != null && !t.isHeli()) {
            Target tmp = this.t;
            this.t = null;
            return tmp;
        } 
        else {
            return null;
        }
    }

    // EFFECT: changes the state of hasPart to true
    void givePart(Target t) {
        this.t = t;
    }

    // whether this cell neighbors a flooded cell
    boolean isNextToFloodedCell() {
        return this.left.isFlooded() || this.right.isFlooded()
                || this.bottom.isFlooded() || this.top.isFlooded();
    }

    public boolean isCenter() {
        return this.r == ForbiddenIslandWorld.ISLAND_SIZE / 2 &&
                this.c == ForbiddenIslandWorld.ISLAND_SIZE / 2;
    }
}

// A hexagonal cell of land.
class HexCell extends Cell {
    Cell botLeft;
    Cell botRight;

    HexCell(double height, int r, int c) {
        super(height, r, c);
        this.botLeft = this.botRight = null;
    }

    // takes in neighboring cells
    // EFFECT: sets this neighboring cells to those
    void setNeighbors(Cell left, Cell top, Cell right, Cell botRight,
            Cell bottom, Cell botLeft) {
        super.setNeighbors(left, top, right, bottom);
        this.botRight = botRight;
        this.botLeft = botLeft;
    }

    @Override
    // returns the neighbor in the specified direction.
    Cell getNeighbor(String dir) {
        if (dir.equals("botleft")) {
            return this.botLeft;
        }
        else if (dir.equals("botright")) {
            return this.botRight;
        } 
        else {
            return super.getNeighbor(dir);
        }
    }

    // sets this cell to flooded if the willFlood flag is true.
    // EFFECT: changes the flooded flag if this cell is next to a flooded cell
    // and its height is below the given waterHeight.
    // (only gets called on coastline cells, which are not flooded and are next
    // to flooded)
    @Override
    void update(int waterHeight) {
        if (!this.isFlooded && this.height < waterHeight) {
            this.setFlooded(true);
            this.left.update(waterHeight);
            this.top.update(waterHeight);
            this.right.update(waterHeight);
            this.bottom.update(waterHeight);
            this.botLeft.update(waterHeight);
            this.botRight.update(waterHeight);
        }
    }

    // whether this cell neighbors a flooded cell
    @Override
    boolean isNextToFloodedCell() {
        return this.botLeft.isFlooded() || this.botRight.isFlooded()
                || super.isNextToFloodedCell();
    }

    // Draws this cell onto the given background
    // changes the colors according to the height and waterheight
    @Override
    WorldImage drawOnto(WorldImage background, int waterHeight) {
        int offsetX = -(3 * ForbiddenIslandWorld.BACKGROUND_SIZE / 8) + CELL_SIZE;
        int offsetY = -ForbiddenIslandWorld.BACKGROUND_SIZE / 2 + CELL_SIZE * 2;

        int color = this.getColor(waterHeight);

        int cc = ForbiddenIslandWorld.ISLAND_SIZE - this.c - 1;
        int rr = ForbiddenIslandWorld.ISLAND_SIZE - this.r - 1;

        WorldImage cell = new HexagonImage(CELL_SIZE / 2 + 2, "solid",
                new Color(color));

        if (this.t != null) {
            cell = t.drawOnto(cell);
        }

        return new OverlayOffsetImage(cell,
                cc * (3.0 / 4) * CELL_SIZE + offsetX,
                rr * CELL_SIZE + CELL_SIZE / 2 * (cc % 2) + offsetY,
                background);
    }
}

// A cell in the ocean.
class OceanCell extends Cell {

    // initializes data
    OceanCell(int r, int c) {
        super(0, r, c);
        this.isFlooded = true; // always true
    }

    // EFFECT: Sets the flooded state of this ocean cell, which is always true.
    @Override
    void setFlooded(boolean isFlooded) {
        // does not want it to ever change state
    }

    // compares the height of this ocean cell to the height of that cell.
    // Returns: the height of that cell, which is
    // 0 if height of that is even with ocean level.
    // <0 if height of that is below ocean level.
    // >0 if height of that is above ocean level.
    @Override
    double compareHeight(Cell that) {
        return -that.height;
    }

    // Draws this ocean cell onto the given background.
    @Override
    WorldImage drawOnto(WorldImage background, int waterHeight) {
        return background;
    }
}

//A hexagonal cell in the ocean.
class HexOceanCell extends HexCell {
    // initializes data
    HexOceanCell(int r, int c) {
        super(0, r, c);
        this.isFlooded = true; // always true
    }

    // EFFECT: Sets the flooded state of this ocean cell, which is always true.
    @Override
    void setFlooded(boolean isFlooded) {
        // does not want it to ever change state
    }

    // compares the height of this ocean cell to the height of that cell.
    // Returns: the height of that cell, which is
    // 0 if height of that is even with ocean level.
    // <0 if height of that is below ocean level.
    // >0 if height of that is above ocean level.
    @Override
    double compareHeight(Cell that) {
        return -that.height;
    }

    // Draws this ocean cell onto the given background.
    @Override
    WorldImage drawOnto(WorldImage background, int waterHeight) {
        return background;
    }
}

// A list of T.
interface IList<T> extends Iterable<T> {
    // Is this list a cons?
    boolean isCons();

    // returns whether it was removed or not
    IList<T> remove(T t);
}

// An empty list of T.
class Empty<T> implements IList<T> {

    // Returns a new iterator for this empty list.
    @Override
    public Iterator<T> iterator() {
        return new IListIterator<T>(this);
    }

    // Is this empty list a cons?
    @Override
    public boolean isCons() {
        return false;
    }

    // returns whether removed or not
    @Override
    public IList<T> remove(T t)
    {
        return this;
    }
}

// A non-empty list of T.
class Cons<T> implements IList<T> {

    T first;
    IList<T> rest;

    // ctor
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
    @Override
    public boolean isCons() {
        return true;
    }

    // removes a given element from a list
    // returns the new IList
    @Override
    public IList<T> remove(T t)
    {
        if (this.first.equals(t))
        {
            return this.rest;
        }
        else
        {
            return new Cons<T>(first, rest.remove(t));
        }
    }
}

// An iterator for a list of T.
class IListIterator<T> implements Iterator<T> {
    IList<T> list;

    // ctor
    IListIterator(IList<T> list) {
        this.list = list;
    }

    // Does the list have another element?
    @Override
    public boolean hasNext() {
        return this.list.isCons();
    }

    // Retrieve the next element of the list.
    @Override
    public T next() {
        Cons<T> cons = (Cons<T>) this.list;
        T ret = cons.first;
        this.list = cons.rest;
        return ret;
    }

    // removes, but not needed
    @Override
    public void remove() {
        // not a needed function
    }
}

// represents a player
class Player {
    int r;
    int c; // position
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
    boolean canMove(String move) {
        Cell next = curr.getNeighbor(move);
        return !next.isFlooded();
    }

    // moves this player given a direction
    // returns if we picked up a helicopter piece or not
    Target move(String move) {
        if (canMove(move)) {
            this.curr = curr.getNeighbor(move);
            this.r = this.curr.r;
            this.c = this.curr.c;
            this.steps++;
        }
        return this.curr.cellLosePart();
    }

    // moves through water when invincible
    // returns if we pick up a helicopter piece or not
    Target moveWater(String move) {

        this.curr = curr.getNeighbor(move);
        this.r = this.curr.r;
        this.c = this.curr.c;
        this.steps++;

        return this.curr.cellLosePart();

    }

    // is this player on a flooded cell?
    boolean isOnFlooded() {
        return this.curr.isFlooded();
    }

    // given an image, it draws this image onto it
    WorldImage drawOnto(WorldImage background, boolean hex) {
        int cc = ForbiddenIslandWorld.ISLAND_SIZE - this.c - 1;
        int rr = ForbiddenIslandWorld.ISLAND_SIZE - this.r - 1;


        double scale = 0.5 * Cell.CELL_SIZE / 15;

        WorldImage image = new ScaleImage(new FromFileImage(
                "Images/pilot-icon.png"), scale);

        if (hex) {
            int offsetX = -(3 * ForbiddenIslandWorld.BACKGROUND_SIZE / 8) + Cell.CELL_SIZE;
            int offsetY = -ForbiddenIslandWorld.BACKGROUND_SIZE / 2 + Cell.CELL_SIZE * 2;

            return new OverlayOffsetImage(image,
                    cc * (3.0 / 4) * Cell.CELL_SIZE + offsetX,
                    rr * Cell.CELL_SIZE + Cell.CELL_SIZE / 2 * (cc % 2) + offsetY,
                    background);
        } 
        else {
            int offset = -ForbiddenIslandWorld.BACKGROUND_SIZE / 2 + Cell.CELL_SIZE * 2;

            return new OverlayOffsetImage(image,
                    cc * Cell.CELL_SIZE + offset,
                    rr * Cell.CELL_SIZE + offset,
                    background);
        }
    }

    // if the player is on the highest point
    boolean onHighestPoint()
    {
        return curr.isCenter();
    }

    // if this player is on this point
    boolean onPoint(Target t)
    {
        return t.samePos(this.r, this.c);
    }
}

// represents everything the player needs to pick up
class Target
{
    int r;
    int c; // positions

    // ctor
    Target(int r, int c)
    {
        this.r = r;
        this.c = c;
    }

    // if this is a helicopter
    boolean isHeli()
    {
        return false;
    }

    // is this a scuba suit?
    boolean isScuba() {
        return false;
    }

    // if this player picks up this item
    boolean pickUp() {
        return true;
    }

    // if this target is on the same position as something
    boolean samePos(int r, int c)
    {
        return this.r == r && this.c == c;
    }

    // given a base, draws the target onto it
    WorldImage drawOnto(WorldImage base)
    {
        double scale = 0.5 * Cell.CELL_SIZE / 15;

        WorldImage circl = new ScaleImage(
                new CircleImage(Cell.CELL_SIZE / 2, OutlineMode.SOLID, Color.BLACK), scale);

        return new OverlayImage(circl, base);
    }
}

// represents a scuba target
// prevents drowning
class ScubaTarget extends Target
{
    boolean isActivated;
    boolean isAvailible;
    // ctor
    ScubaTarget(int r, int c) {
        super(r, c);
        this.isAvailible = false;
        this.isActivated = false;
    }

    // is this a scuba suit?
    @Override
    boolean isScuba() {
        return true;
    }

    // gets picked up
    // EFFECT: changes isActivated
    @Override
    boolean pickUp()
    {
        this.isAvailible = true;
        return true;
    }

    // whether we are activated
    boolean isActivated()
    {
        return this.isActivated;
    }

    // unactivates it
    // EFFECT: changes isActivated
    void deactivate()
    {
        this.isActivated = false;
    }

    // activeates scuba
    // EFFECT: changes isActivated
    void activate()
    {
        this.isActivated = true;
    }
    // given a base, draws the target onto it
    @Override
    WorldImage drawOnto(WorldImage base)
    {
        double scale = 0.5 * Cell.CELL_SIZE / 15;

        WorldImage copt = new ScaleImage(
                new FromFileImage("Images/scuba-icon.jpg"), scale);

        return new OverlayImage(copt, base);
    } 
}

// represents the helicopter in the center
class HelicopterTarget extends Target
{
    // the ctor
    HelicopterTarget(int r, int c) {
        super(r, c);
    }

    // it is a helicopter
    @Override
    boolean isHeli()
    {
        return true;
    }

    // given a base, draws the target onto it
    @Override
    WorldImage drawOnto(WorldImage base)
    {
        double scale = 0.5 * Cell.CELL_SIZE / 15;

        WorldImage copt = new ScaleImage(
                new FromFileImage("Images/helicopter-icon.png"), scale);

        return new OverlayImage(copt, base);
    }

    // if this can be picked up
    // it can never be picked up
    @Override
    boolean pickUp()
    {
        return false;
    }
}

// represents a forbidden island game that is aw world
class ForbiddenIslandWorld extends World {

    IList<Cell> board; // all the cells
    int waterHeight; // the height of the water
    static final int ISLAND_SIZE = 64; // constant val
    static final int BACKGROUND_SIZE = Cell.CELL_SIZE * (ISLAND_SIZE + 2) + Cell.CELL_SIZE / 2;
    Player player1;
    Player player2;
    final int WATERINCREASE = 1;
    int tick; // time
    IList<Target> targets; // a list of the targets that remain in the game
    int numParts; // number of helicopter parts
    final HelicopterTarget HELI; // the helicopter
    int timeBeforeFlood;
    ScubaTarget scuba;
    int scubaTime = 10; // time for not drowning
    boolean hex;

    // initializes data
    ForbiddenIslandWorld() {
        this.hex = false;
        this.HELI = new HelicopterTarget(ISLAND_SIZE / 2, ISLAND_SIZE / 2);
        this.newBoard("m", ISLAND_SIZE);
    }

    // makes a new board of the specified type.
    // EFFECT: sets board to a new board of the specified type,
    // sets player to a new player with no progress at the center of the board,
    // resets waterHeight and tick to 0.
    void newBoard(String type, int size) {
        this.numParts = 4;
        if (type.equals("m")) {
            if (this.hex) {
                this.makeHexMountain(size);
            }
            else {
                this.makeMountainBoard(size);
            }
        } 
        else if (type.equals("r")) {
            if (this.hex) {
                this.makeHexRandom(size);
            }
            else {
                this.makeRandomBoard(size);
            }
        } 
        else if (type.equals("t")) {
            if (this.hex) {
                this.makeHexTerrain(size);
            }
            else {
                this.makeTerrainBoard(size);
            }
        } 
        else {
            throw new IllegalArgumentException("Not a valid board type.");
        }
        this.waterHeight = 0;
        this.tick = 0;
        this.timeBeforeFlood = 10 * 
                (ISLAND_SIZE - this.waterHeight) / WATERINCREASE;
        this.player1 = new Player(ISLAND_SIZE / 2, ISLAND_SIZE / 2,
                this.cellAt(ISLAND_SIZE / 2, ISLAND_SIZE / 2));
        this.player2 = new Player(ISLAND_SIZE / 2 + 1, ISLAND_SIZE / 2 + 1,
                this.cellAt(ISLAND_SIZE / 2 + 1, ISLAND_SIZE / 2 + 1));
    }

    void generateCopterParts(ArrayList<ArrayList<Double>> heights)
    {
        Target t;
        this.targets = new Cons<Target>(this.HELI, new Empty<Target>());
        int counter = 0;
        int numScuba = 1;

        // random indicies for helicopter parts
        while (counter < this.numParts + numScuba) {
            int indexOuter = (int) (Math.random() * heights.size());
            int indexInner = (int) (Math.random() * heights.get(0).size());

            if (heights.get(indexOuter).get(indexInner) >= 5) {
                if (counter == this.numParts) // last one is scuba
                {
                    this.scuba = new ScubaTarget(indexOuter, indexInner);
                    this.targets = new Cons<Target>(this.scuba, this.targets);
                }
                t = new Target(indexOuter, indexInner);
                this.targets = new Cons<Target>(t, this.targets);
                counter++;

            }
        }
    }

    // whether given coordinates are in parts
    boolean inPart(int row, int rowSize, int col, ArrayList<Integer> parts) {
        for (Integer pos : parts) {
            if ((row * rowSize + col) == pos) {
                return true;
            }
        }
        return false;
    }

    // takes in a matrix of heights, creates a matrix of cells based on those
    // heights with helicopter parts randomly dispersed.
    ArrayList<ArrayList<Cell>> heightsToCells(
            ArrayList<ArrayList<Double>> heights) {
        ArrayList<ArrayList<Cell>> result = new ArrayList<ArrayList<Cell>>();

        generateCopterParts(heights);

        for (int i = 0; i < heights.size(); i++) {
            ArrayList<Double> hRow = heights.get(i);
            ArrayList<Cell> newRow = new ArrayList<Cell>();
            for (int j = 0; j < hRow.size(); j++) {
                if (hRow.get(j) == 0) {
                    newRow.add(new OceanCell(i, j));
                }
                else {
                    Cell temp = new Cell(hRow.get(j), i, j);
                    for (Target t: this.targets)
                    {
                        if (t.samePos(i, j))
                        {
                            temp.givePart(t);
                        }
                    }
                    newRow.add(temp);
                }
            }
            result.add(newRow);
        }

        // updates the neighboring cells for each cell
        Cell offBoard = new OceanCell(-1, -1);
        for (int r = 0; r < result.size(); r++) {
            ArrayList<Cell> row = result.get(r);
            for (int c = 0; c < row.size(); c++) {
                Cell curr = row.get(c);
                Cell left = offBoard;
                Cell right = offBoard;
                Cell top = offBoard;
                Cell bottom = offBoard;

                if (onBoard(r, c - 1)) {
                    left = row.get(c - 1);
                }
                if (onBoard(r - 1, c)) {
                    top = result.get(r - 1).get(c);
                }
                if (onBoard(r, c + 1)) {
                    right = row.get(c + 1);
                }
                if (onBoard(r + 1, c)) {
                    bottom = result.get(r + 1).get(c);
                }

                curr.setNeighbors(left, top, right, bottom);
            }
        }

        return result;
    }

    // takes in a matrix of heights, creates a matrix of hex cells based on those
    // heights with helicopter parts randomly dispersed.
    ArrayList<ArrayList<HexCell>> heightsToHexCells(
            ArrayList<ArrayList<Double>> heights) {
        ArrayList<ArrayList<HexCell>> result = new ArrayList<ArrayList<HexCell>>();

        this.generateCopterParts(heights);

        for (int i = 0; i < heights.size(); i++) {
            ArrayList<Double> hRow = heights.get(i);
            ArrayList<HexCell> newRow = new ArrayList<HexCell>();
            for (int j = 0; j < hRow.size(); j++) {
                if (hRow.get(j) == 0) {
                    newRow.add(new HexOceanCell(i, j));
                } 
                else {
                    HexCell temp = new HexCell(hRow.get(j), i, j);
                    for (Target t: this.targets)
                    {
                        if (t.samePos(i, j))
                        {
                            temp.givePart(t);
                        }
                    }
                    newRow.add(temp);
                }
            }
            result.add(newRow);
        }

        // updates the neighboring cells for each cell
        Cell offBoard = new HexOceanCell(-1, -1);
        for (int r = 0; r < result.size(); r++) {
            ArrayList<HexCell> row = result.get(r);
            for (int c = 0; c < row.size(); c++) {
                HexCell curr = row.get(c);
                Cell left = offBoard;
                Cell right = offBoard;
                Cell top = offBoard;
                Cell bottom = offBoard;
                Cell botRight = offBoard;
                Cell botLeft = offBoard;

                int mod = c % 2;

                if (onBoard(r - 1, c)) {
                    top = result.get(r - 1).get(c);
                }
                if (onBoard(r + 1, c)) {
                    bottom = result.get(r + 1).get(c);
                }
                if (onBoard(r - ((mod + 1) % 2), c + 1)) {
                    right = result.get(r - ((mod + 1) % 2)).get(c + 1);
                }
                if (onBoard(r - ((mod + 1) % 2), c - 1)) {
                    left = result.get(r - ((mod + 1) % 2)).get(c - 1);
                }
                if (onBoard(r + mod, c + 1)) {
                    botRight = result.get(r + mod).get(c + 1);
                }
                if (onBoard(r + mod, c - 1)) {
                    botLeft = result.get(r + mod).get(c - 1);
                }

                curr.setNeighbors(left, top, right, botRight, bottom, botLeft);
            }
        }

        return result;
    }

    // creates a mountain, where the highest point is the center
    // EFFECT: sets the board to a new mountain board.
    void makeMountainBoard(int size) {
        this.hex = false;
        ArrayList<ArrayList<Double>> heights = new ArrayList<ArrayList<Double>>();
        double middle = size / 2.0;

        for (int i = 0; i < size + 1; i++) {
            ArrayList<Double> row = new ArrayList<Double>();
            for (int j = 0; j < size + 1; j++) {
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
    void makeRandomBoard(int size) {
        this.hex = false;
        ArrayList<ArrayList<Double>> heights = new ArrayList<ArrayList<Double>>();
        double middle = size / 2.0;
        Random rand = new Random();

        for (int i = 0; i < size + 1; i++) {
            ArrayList<Double> row = new ArrayList<Double>();
            for (int j = 0; j < size + 1; j++) {
                double manhatDist = Math.abs(i - middle) + Math.abs(j - middle);
                if (manhatDist < middle - 1) {
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

    // makes a terrain board with random heights and random layout.
    // EFFECT: sets the board to a new terrain board.
    void makeTerrainBoard(int size) {
        this.hex = false;
        ArrayList<ArrayList<Double>> heights = new ArrayList<ArrayList<Double>>();
        for (int i = 0; i < size + 1; i++) {
            ArrayList<Double> row = new ArrayList<Double>();
            for (int j = 0; j < size + 1; j++) {
                row.add(0.0);
            }
            heights.add(row);
        }

        int mid = size / 2;

        heights.get(0).set(mid, 1.0);
        heights.get(size).set(mid, 1.0);
        heights.get(mid).set(0, 1.0);
        heights.get(mid).set(size, 1.0);
        heights.get(mid).set(mid, size / 2.0);

        // Fills top left quadrant.
        heights = this.makeTerrainHelper(heights, 0, mid, 0, mid, true, true);
        // Fills top right quadrant.
        heights = this
                .makeTerrainHelper(heights, 0, mid, mid, size, false, true);
        // Fills bottom left quadrant.
        heights = this
                .makeTerrainHelper(heights, mid, size, 0, mid, true, false);
        // Fills bottom right quadrant.
        heights = this.makeTerrainHelper(heights, mid, size, mid, size, false, false);

        // creates the cells based on the heights given
        ArrayList<ArrayList<Cell>> cells = this.heightsToCells(heights);
        ArrayList<Cell> temp = new ArrayList<Cell>();
        for (ArrayList<Cell> row : cells) {
            temp.addAll(row);
        }

        this.board = new Cons<Cell>(temp);
    }

    // given the minimum row, maximum row, minumum column, maximum column,
    // and which quadrant it was
    // it helps make a random terrain
    ArrayList<ArrayList<Double>> makeTerrainHelper(
            ArrayList<ArrayList<Double>> heights, int rmin, int rmax, int cmin,
            int cmax, boolean onLeft, boolean onTop) {
        double area = (rmax - rmin) * (cmax - cmin) / 16.0;

        Double tl = heights.get(rmin).get(cmin);
        Double tr = heights.get(rmin).get(cmax);
        Double bl = heights.get(rmax).get(cmin);
        Double br = heights.get(rmax).get(cmax);

        int midCol = (cmin + cmax) / 2;
        int midRow = (rmin + rmax) / 2;

        Double l = heights.get(midRow).get(cmin);
        Double t = heights.get(rmin).get(midCol);

        if (onLeft) {
            l = (tl + bl) / 2 + this.newVariance(area);
            if (l < 1) {
                l = 0.0;
            }
            if (l > ISLAND_SIZE / 2.0) {
                l = ISLAND_SIZE / 2.0;
            }
        }
        if (onTop) {
            t = (tl + tr) / 2 + this.newVariance(area);
            if (t < 1) {
                t = 0.0;
            }
            if (t > ISLAND_SIZE / 2.0) {
                t = ISLAND_SIZE / 2.0;
            }
        }
        Double r = (tr + br) / 2 + this.newVariance(area);
        if (r < 1) {
            r = 0.0;
        }
        if (r > ISLAND_SIZE / 2.0) {
            r = ISLAND_SIZE / 2.0;
        }
        Double b = (bl + br) / 2 + this.newVariance(area);
        if (b < 1) {
            b = 0.0;
        }
        if (b > ISLAND_SIZE / 2.0) {
            b = ISLAND_SIZE / 2.0;
        }
        Double m = (l + t + r + b) / 4 + this.newVariance(area);
        if (m < 1) {
            m = 0.0;
        }
        if (m > ISLAND_SIZE / 2.0) {
            m = ISLAND_SIZE / 2.0;
        }

        if (cmax - cmin > 1 && rmax - rmin > 1) {
            heights.get(midRow).set(cmin, l);
            heights.get(midRow).set(cmax, r);
            heights.get(rmin).set(midCol, t);
            heights.get(rmax).set(midCol, b);
            heights.get(midRow).set(midCol, m);
            // Fills top left quadrant.
            heights = makeTerrainHelper(heights, rmin, midRow, cmin, midCol,
                    onLeft, onTop);
            // Fills top right quadrant.
            heights = makeTerrainHelper(heights, rmin, midRow, midCol, cmax,
                    false, onTop);
            // Fills bottom left quadrant.
            heights = makeTerrainHelper(heights, midRow, rmax, cmin, midCol,
                    onLeft, false);
            // Fills bottom right quadrant.
            heights = makeTerrainHelper(heights, midRow, rmax, midCol, cmax,
                    false, false);
        }
        // If the original quadrant was taller than it was wide.
        else if (rmax - rmin > 1) {
            heights.get(midRow).set(cmin, l);
            if (cmax != cmin) {
                heights.get(midRow).set(cmax, r);
            }
            // Fills top half of remaining.
            heights = makeTerrainHelper(heights, rmin, midRow, cmin, cmax,
                    onLeft, onTop);
            // Fills bottom half of remaining.
            heights = makeTerrainHelper(heights, midRow, rmax, cmin, cmax,
                    onLeft, false);
        }
        // If the original quadrant was wider than it was tall.
        else if (cmax - cmin > 1) {
            heights.get(rmin).set(midCol, t);
            if (rmax != rmin) {
                heights.get(rmax).set(midCol, b);
            }
            // Fills left half of remaining.
            heights = makeTerrainHelper(heights, rmin, rmax, cmin, midCol,
                    onLeft, onTop);
            // Fills right half of remaining.
            heights = makeTerrainHelper(heights, rmin, rmax, midCol, cmax,
                    false, onTop);
        }

        return heights;
    }

    // returns a new random double between +scale and -scale.
    // weighted to be closer to -scale the higher bal is set to.
    double newVariance(double scale) {
        Random rand = new Random();
        double bal = 2.5;
        double var = ((bal + 1) * rand.nextDouble() - bal) * scale;
        if (var < 0) {
            var = var / bal;
        }
        return var;
    }

    // creates a mountain, where the highest point is the center, and the cells are hexagons
    // EFFECT: sets the board to a new hexagon mountain board.
    void makeHexMountain(int size) {
        ArrayList<ArrayList<Double>> heights = new ArrayList<ArrayList<Double>>();
        int midRow = size / 2;
        int midCol = size / 2;

        for (int r = 0; r < size + 1; r++) {
            ArrayList<Double> row = new ArrayList<Double>();
            for (int c = 0; c < size + 1; c++) {
                int x = (c - midCol) - ((r - midRow) - ((r - midRow) % 2)) / 2;
                int z = (r - midRow);
                int y = -x - z;
                double manhatDist = Math.max(Math.max(Math.abs(x), Math.abs(y)), Math.abs(z));
                if (manhatDist < size / 2) {
                    row.add(size / 2 - manhatDist);
                }
                else {
                    row.add(0.0);
                }
            }
            heights.add(row);
        }

        ArrayList<ArrayList<HexCell>> cells = this.heightsToHexCells(heights);
        ArrayList<Cell> temp = new ArrayList<Cell>();
        for (ArrayList<HexCell> row : cells) {
            for (HexCell cell : row) {
                temp.add(cell);
            }
        }

        this.board = new Cons<Cell>(temp);
    }

    // makes a diamond board with random heights all around, and the cells are hexagons
    // EFFECT: sets the board to a new hexagon random board.
    void makeHexRandom(int size) {
        Random rand = new Random();
        ArrayList<ArrayList<Double>> heights = new ArrayList<ArrayList<Double>>();
        int mid = size / 2;

        for (int r = 0; r < size + 1; r++) {
            ArrayList<Double> row = new ArrayList<Double>();
            for (int c = 0; c < size + 1; c++) {
                int x = (c - mid) - ((r - mid) - ((r - mid) % 2)) / 2;
                int z = (r - mid);
                int y = -x - z;
                double manhatDist = Math.max(Math.max(Math.abs(x), Math.abs(y)), Math.abs(z));
                if (manhatDist < mid) {
                    row.add(rand.nextDouble() * size / 2.0);
                }
                else {
                    row.add(0.0);
                }
            }
            heights.add(row);
        }

        ArrayList<ArrayList<HexCell>> cells = this.heightsToHexCells(heights);
        ArrayList<Cell> temp = new ArrayList<Cell>();
        for (ArrayList<HexCell> row : cells) {
            for (HexCell cell : row) {
                temp.add(cell);
            }
        }

        this.board = new Cons<Cell>(temp);
    }

    // makes a terrain board with random heights and random layout, with hexagon cells.
    // EFFECT: sets the board to a new hexagon terrain board.
    void makeHexTerrain(int size) {
        ArrayList<ArrayList<Double>> heights = new ArrayList<ArrayList<Double>>();
        for (int i = 0; i < size + 1; i++) {
            ArrayList<Double> row = new ArrayList<Double>();
            for (int j = 0; j < size + 1; j++) {
                row.add(0.0);
            }
            heights.add(row);
        }

        int mid = size / 2;

        heights.get(0).set(mid, 1.0);
        heights.get(size).set(mid, 1.0);
        heights.get(mid).set(0, 1.0);
        heights.get(mid).set(size, 1.0);
        heights.get(mid).set(mid, size / 2.0);

        // Fills top left quadrant.
        heights = this.makeTerrainHelper(heights, 0, mid, 0, mid, true, true);
        // Fills top right quadrant.
        heights = this.makeTerrainHelper(heights, 0, mid, mid, size, false, true);
        // Fills bottom left quadrant.
        heights = this.makeTerrainHelper(heights, mid, size, 0, mid, true, false);
        // Fills bottom right quadrant.
        heights = this.makeTerrainHelper(heights, mid, size, mid, size, false, false);

        ArrayList<ArrayList<HexCell>> cells = this.heightsToHexCells(heights);
        ArrayList<Cell> temp = new ArrayList<Cell>();
        for (ArrayList<HexCell> row : cells) {
            for (HexCell cell : row) {
                temp.add(cell);
            }
        }

        this.board = new Cons<Cell>(temp);
    }

    // is this position in bounds for the current board?
    boolean onBoard(int r, int c) {
        return r >= 0 && r < ISLAND_SIZE && c >= 0 && c < ISLAND_SIZE;
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

    // translates another player into up down left right
    String translate(String key) {
        if (key.equals("a")) {
            return "left";
        }
        else if (key.equals("w")) {
            return "up";
        }
        else if (key.equals("d")) {
            return "right";
        }
        else if (key.equals("s")) {
            return "down";
        } 
        else {
            return "bad"; // i can guarentee "bad" won't mean anything
        }
    }

    // translates hexagonal control keys to movement directions.
    String hexTranslate(String key) {
        if (key.equals("a") || key.equals("g")) {
            return "left";
        }
        else if (key.equals("w") || key.equals("y")) {
            return "up";
        }
        else if (key.equals("e") || key.equals("u")) {
            return "right";
        }
        else if (key.equals("d") || key.equals("j")) {
            return "botright";
        }
        else if (key.equals("x") || key.equals("n")) {
            return "down";
        }
        else if (key.equals("z") || key.equals("b")) {
            return "botleft";
        }
        else {
            return "bad";
        }
    }

    // moves the player specified by num using the specified move
    public void movePlayer(int num, String move) {
        Target retrieved;
        if (num == 1) {
            if (this.scuba.isActivated()) {
                retrieved = this.player1.moveWater(move);
            }
            else {
                retrieved = this.player1.move(move);
            }
        }
        else {
            if (this.scuba.isActivated()) {
                retrieved = this.player2.moveWater(move);
            } 
            else {
                retrieved = this.player2.move(move);
            }
        }

        if (retrieved != null && retrieved.pickUp()) {
            this.targets.remove(retrieved);
            if (!retrieved.isScuba()) {
                this.numParts--;
            }
        }
    }

    // changes the state of the world given a key stroke
    @Override
    public void onKeyEvent(String key) {

        if (this.scuba.isAvailible && key.equals("6")) // since s is already in use
        {
            this.scuba.activate();
        }

        if (this.hex) {
            if (key.equals("a") || key.equals("w") || key.equals("e")
                    || key.equals("d") || key.equals("x") || key.equals("z")) {
                this.movePlayer(1, this.hexTranslate(key));
            }

            if (key.equals("g") || key.equals("y") || key.equals("u")
                    || key.equals("j") || key.equals("n") || key.equals("b")) {
                this.movePlayer(2, this.hexTranslate(key));
            }
        } 
        else {
            if (key.equals("up") || key.equals("down") || 
                    key.equals("left") || key.equals("right")) {
                this.movePlayer(1, key);
            }

            if (key.equals("a") || key.equals("w") ||
                    key.equals("d") || key.equals("s"))
            {
                this.movePlayer(2, this.translate(key));
            }
        }

        // new islands
        if (key.equals("m") || key.equals("r") || key.equals("t")) {
            this.newBoard(key, ISLAND_SIZE);
        }

        // toggles hexagons
        else if (key.equals("h")) {
            this.hex = !this.hex;
            this.newBoard("m", ISLAND_SIZE);
        }
    }

    // changes the state of the world
    // EFFECT: increases the water height and updates the board
    @Override
    public void onTick() {
        this.tick++;

        this.timeBeforeFlood--;

        if (this.scuba.isActivated)
        {
            this.scubaTime--;

            if (this.scubaTime == 0)
            {
                this.scuba.deactivate();
            }
        }

        if (tick % 10 == 0) {

            this.waterHeight += this.WATERINCREASE;

            IList<Cell> coastline = this.getCoastline();

            for (Cell cell : coastline) {
                cell.update(this.waterHeight);
            }
        }
    }

    // Returns a list containing all cells on the coastline.
    // (not flooded, but next to flooded cells)
    IList<Cell> getCoastline() {
        IList<Cell> coastline = new Empty<Cell>();

        for (Cell cell : board) {
            if (!cell.isFlooded() && cell.isNextToFloodedCell()) {
                coastline = new Cons<Cell>(cell, coastline);
            }
        }

        return coastline;
    }

    // makes the scene with all the images
    @Override
    public WorldScene makeScene() {
        WorldScene scene = this.getEmptyScene();
        WorldImage image = new RectangleImage(BACKGROUND_SIZE, BACKGROUND_SIZE,
                "solid", new Color(0x80));
        for (Cell cell : this.board) {
            image = cell.drawOnto(image, this.waterHeight);
        }

        image = this.player2.drawOnto(this.player1.drawOnto(image, this.hex), this.hex);

        scene.placeImageXY(image, BACKGROUND_SIZE / 2, BACKGROUND_SIZE / 2);
        scene.placeImageXY(new TextImage("Time remaining: " + this.timeBeforeFlood, 
                30, Color.ORANGE), 150, 15);

        scene.placeImageXY(new TextImage("Player 1's steps: " + this.player1.steps,
                30, Color.RED), 860, 900);
        scene.placeImageXY(new TextImage("Player 2's steps: " + this.player2.steps,
                30, Color.RED), 860, 950);

        if (this.scuba.isActivated)
        {
            scene.placeImageXY(new TextImage("INVINCIBILITY TIME: " + this.scubaTime,
                    30, Color.WHITE), 550, 15);
        }

        return scene;
    }

    // when the games over
    // the player drowns, or the player gets off island
    @Override
    public WorldEnd worldEnds() {
        if ((!this.scuba.isActivated && (player1.isOnFlooded() || player2.isOnFlooded())) || 
                ((numParts == 0) && player1.onHighestPoint() && player2.onHighestPoint()))
        {
            WorldScene scene = this.getEmptyScene();
            scene.placeImageXY(this.lastImage(), BACKGROUND_SIZE / 2,
                    BACKGROUND_SIZE / 2);

            return new WorldEnd(true, scene);
        }
        return new WorldEnd(false, this.makeScene());
    }

    // returns the winner's image
    public WorldImage winImage() {
        WorldImage image = new RectangleImage(BACKGROUND_SIZE, BACKGROUND_SIZE,
                "solid", new Color(0x80));
        for (Cell cell : this.board) {
            image = cell.drawOnto(image, this.waterHeight);
        }

        image = this.player1.drawOnto(image, this.hex);
        image = this.player2.drawOnto(image, this.hex);

        int textSize = 60 * ISLAND_SIZE / 64 * Cell.CELL_SIZE / 15;

        WorldImage text = new AboveImage(new TextImage("Congratulations Winner!", 
                textSize, Color.MAGENTA),
                new TextImage("Player 1's steps: " + player1.steps,
                        textSize, Color.MAGENTA),
                new TextImage("Player 2's steps: " + player2.steps, 
                        textSize, Color.MAGENTA));

        return new OverlayImage(text, image);
    }

    // returns the game over screen
    public WorldImage lastImage() {
        if (numParts == 0) {
            return winImage();
        }
        else {
            WorldImage image = new RectangleImage(BACKGROUND_SIZE,
                    BACKGROUND_SIZE, "solid", new Color(0x80));
            for (Cell cell : this.board) {
                image = cell.drawOnto(image, this.waterHeight);
            }

            image = this.player1.drawOnto(image, this.hex);
            image = this.player2.drawOnto(image, this.hex);

            int textSize = 60 * ISLAND_SIZE / 64 * Cell.CELL_SIZE / 15;

            WorldImage text = new AboveImage(
                    new TextImage("GAME OVER!", textSize, Color.MAGENTA),
                    new TextImage("Player 1's steps: " + player1.steps,
                            textSize, Color.MAGENTA),
                    new TextImage("Player 2's steps: " + player2.steps, 
                            textSize, Color.MAGENTA));

            return new OverlayImage(text, image);
        }
    }
}

// represents examples for the forbidden island
class ExamplesIsland {
    ForbiddenIslandWorld game;
    Cell c1;
    Cell c2;
    Cell c3;
    Cell c4;
    Cell c4t;
    Cell c4b;
    Cell c4r;
    Cell c4l;
    Cell center;

    Empty<Integer> mt = new Empty<Integer>();
    Cons<Integer> con1 = new Cons<Integer>(10, this.mt);
    Cons<Integer> con2 = new Cons<Integer>(30, this.con1);
    ArrayList<Integer> arrIn = new ArrayList<Integer>();
    IListIterator<Integer> iter = new IListIterator<Integer>(this.con2);
    IListIterator<Integer> itermt =  new IListIterator<Integer>(this.mt);

    Player p1;
    Player p2;

    Target t1;
    Target t2;
    Target t3;

    ScubaTarget s1;

    HelicopterTarget ht1;
    HelicopterTarget ht2;

    // initializes the forbidden island world.
    void gameInit() {
        this.game = new ForbiddenIslandWorld();
    }

    // initializes player data
    void playerInit()
    {
        this.p1 = new Player(0, 0, c1);
        this.p2 = new Player(12, 5, c4);

    }

    // initializes target data
    void targetInit()
    {
        this.t1 = new Target(0, 0);
        this.t2 = new Target(0, 1);
        this.t3 = new Target(12, 5);

        this.s1 = new ScubaTarget(20, 20);

        this.ht1 = new HelicopterTarget(32, 32);
        this.ht2 = new HelicopterTarget(0, 0);
    }

    // initializes the data for testing
    void cellInit() {
        Cell oc = new OceanCell(-1, -1);
        this.c1 = new Cell(0, 0, 0);
        this.c2 = new Cell(5, 0, 0);
        this.c3 = new Cell(10, 10, 15);
        this.c4 = new Cell(12, 5, 5);
        this.c4t = new Cell(13, 5, 4);
        this.c4b = new Cell(9, 5, 6);
        this.c4r = new Cell(13, 6, 5);
        this.c4l = new Cell(11, 4, 5);
        this.c4t.setNeighbors(oc, oc, oc, this.c4);
        this.c4b.setNeighbors(oc, this.c4, oc, oc);
        this.c4r.setNeighbors(this.c4, oc, oc, oc);
        this.c4l.setNeighbors(oc, oc, this.c4, oc);
        this.c4.setNeighbors(this.c4l, this.c4t, this.c4r, this.c4b);
        this.center = new Cell(64, ForbiddenIslandWorld.ISLAND_SIZE / 2, 
                ForbiddenIslandWorld.ISLAND_SIZE / 2);
    }

    // initializes the board data
    void boardInit() {
        // initializing data

    }

    /*
     * To Test: ForbiddenIslandWorld: heightsToCells newVariance onBoard cellAt
     * getCoastline
     * 
     * Player: isOnFlooded move canMove
     * 
     * Cell: getNeighbor isFlooded
     * 
     * Other: ?
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
        t.checkConstructorException(new IllegalArgumentException(
                "List must not be empty."), "Cons", strings);
        strings.add("Hello");
        t.checkExpect(new Cons<String>(strings), new Cons<String>("Hello",
                new Empty<String>()));
        strings.add("Hello");
        strings.add("World");
        t.checkExpect(new Cons<String>(strings), new Cons<String>("Hello",
                new Cons<String>("World", new Empty<String>())));
    }

    // tests onBoard method.
    void testOnBoard(Tester t) {
        gameInit();
        t.checkExpect(this.game.onBoard(-1, 0), false);
        t.checkExpect(this.game.onBoard(0, -1), false);
        t.checkExpect(this.game.onBoard(0, 0), true);
        t.checkExpect(this.game.onBoard(ForbiddenIslandWorld.ISLAND_SIZE, 0),
                false);
        t.checkExpect(this.game.onBoard(0, ForbiddenIslandWorld.ISLAND_SIZE),
                false);
        t.checkExpect(this.game.onBoard(ForbiddenIslandWorld.ISLAND_SIZE - 1,
                ForbiddenIslandWorld.ISLAND_SIZE - 1), true);
    }

    // tests compare height
    void testCellCompareHeight(Tester t) {
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

    // tests cell updating
    void testCellUpdate(Tester t) {
        cellInit();
        t.checkExpect(this.c4.isFlooded, false);
        this.c4.update(0);
        t.checkExpect(this.c4.isFlooded, false);
        this.c4.update(13);
        t.checkExpect(this.c4.isFlooded, true);
        t.checkExpect(this.c4b.isFlooded, true);
        t.checkExpect(this.c4r.isFlooded, false);
    }

    // tests is next to flooded cell
    void testIsNextToFloodedCell(Tester t) {
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

    // tests empty list
    void testMtList(Tester t)
    {
        t.checkExpect(mt.iterator(), new IListIterator<Integer>(mt));
        t.checkExpect(mt.isCons(), false);
        t.checkExpect(mt.remove(10), mt);
    }

    // tests cons list
    void testCons(Tester t)
    {
        arrIn.add(20);
        Cons<Integer> con3 = new Cons<Integer>(arrIn);

        t.checkExpect(con2.iterator(), new IListIterator<Integer>(con2));
        t.checkExpect(con1.isCons(), true);
        t.checkExpect(con2.remove(30), con1);
        t.checkExpect(con3.first, 20);
    }

    // tests the ilist iterator
    void testIterator(Tester t)
    {
        t.checkExpect(iter.hasNext(), true);
        t.checkExpect(itermt.hasNext(), false);
        t.checkExpect(iter.next(), 30);           
    }

    // tests canMove in player class
    void testCanMove(Tester t)
    {
        cellInit();
        playerInit();

        this.p1 = new Player(4, 2, this.c4b);
        this.p2 = new Player(40, 20, this.c4r);

        t.checkExpect(p1.canMove("down"), false);
        t.checkExpect(p2.canMove("up"), false);
        t.checkExpect(p2.canMove("left"), true);
        t.checkExpect(p1.canMove("up"), true);
    }

    // tests the move method in player class
    void testMove(Tester t)
    {
        cellInit();
        playerInit();

        this.p1 = new Player(4, 2, this.c4b);
        this.p2 = new Player(40, 20, this.c4r);

        t.checkExpect(p1.move("right"), null);
        t.checkExpect(p2.move("up"), null);
        t.checkExpect(p1.move("right"), null);
    }

    // tests if a player is on flooded cell
    void testOnFlooded(Tester t)
    {
        cellInit();
        playerInit();

        this.p1 = new Player(4, 2, this.c4b);
        this.p2 = new Player(40, 20, this.c4r);
        Player p3 = new Player(5, 3, this.c4t);

        boolean before; 
        boolean after;

        before = p3.isOnFlooded();
        p3 = new Player(9, 2, new OceanCell(9, 2));
        after = p3.isOnFlooded();

        t.checkExpect(this.p1.isOnFlooded(), false);
        t.checkExpect(this.p2.isOnFlooded(), false);
        t.checkExpect(before, false);
        t.checkExpect(after, true);
    }

    // tests highest point
    void testHighestPoint(Tester t)
    {
        cellInit();
        playerInit();
        Player p3 = new Player(5, 3, this.center);

        t.checkExpect(this.p1.onHighestPoint(), false);
        t.checkExpect(this.p2.onHighestPoint(), false);
        t.checkExpect(p3.onHighestPoint(), true);
    }

    // tests onpoint
    void testOnPoint(Tester t)
    {
        cellInit();
        playerInit();
        targetInit();

        t.checkExpect(this.p1.onPoint(this.t1), true);
        t.checkExpect(this.p1.onPoint(this.t2), false);
        t.checkExpect(this.p2.onPoint(this.t3), true);
    }

    // tests target methods
    void testTarget(Tester t)
    {
        cellInit();
        playerInit();
        targetInit();

        t.checkExpect(this.t1.isHeli(), false); 
        t.checkExpect(this.t2.isHeli(), false);
        t.checkExpect(this.ht1.isHeli(), true);
        t.checkExpect(this.s1.isHeli(), false);
        t.checkExpect(this.t1.pickUp(), true);
        t.checkExpect(this.t2.pickUp(), true);
        t.checkExpect(this.t3.pickUp(), true);
        t.checkExpect(this.s1.pickUp(), true);
        t.checkExpect(this.ht1.pickUp(), false);
        t.checkExpect(this.ht2.pickUp(), false);
    }

    // tests the island
    void testIsland(Tester t) {
        this.gameInit();
        this.game.bigBang(ForbiddenIslandWorld.BACKGROUND_SIZE,
                ForbiddenIslandWorld.BACKGROUND_SIZE, 0.1);
    }

    // main, runs the class
    public static void main(String[] args) {
        ForbiddenIslandWorld game = new ForbiddenIslandWorld();
        game.bigBang(ForbiddenIslandWorld.BACKGROUND_SIZE,
                ForbiddenIslandWorld.BACKGROUND_SIZE, 0.1);
    }
}