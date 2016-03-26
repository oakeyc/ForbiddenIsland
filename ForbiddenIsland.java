import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;

// Represents a cell.
class Cell {
    static final int CELL_SIZE = 20;
    
    double height;
    int x, y;
    Cell left, top, right, bottom;
    boolean isFlooded, hasPart;
    
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