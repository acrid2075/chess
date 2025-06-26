package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        if (((row < 1) || (row > 8)) || ((col < 1) || (col > 8))) {
            throw new InvalidPositionException("Either row or col is out of bounds");
        }
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return this.row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return this.col;

    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {return false;}
        if (obj.getClass() != this.getClass()) {return false;}
        ChessPosition other = (ChessPosition) obj;
        if (other.getRow() != this.getRow()) {return false;}
        if (other.getColumn() != this.getColumn()) {return false;}
        return true;
    }
    @Override
    public int hashCode() {
        return this.getColumn() * 10 + this.getRow();
    }
}
