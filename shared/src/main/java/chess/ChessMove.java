package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    private ChessPosition startPosition;
    private ChessPosition endPosition;
    private ChessPiece.PieceType promotionPiece;
    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    public ChessMove(String moveDetails) {
        this.startPosition = new ChessPosition(9 - ((int) moveDetails.charAt(1) - '0'), "abcdefgh".indexOf(moveDetails.charAt(0)) + 1);
        this.endPosition = new ChessPosition(9 - ((int) moveDetails.charAt(3) - '0'), "abcdefgh".indexOf(moveDetails.charAt(2)) + 1);
        if (moveDetails.length() == 4) {
            this.promotionPiece = null;
        } else {
            this.promotionPiece = ChessPiece.PieceType.values()["kqbnrp".indexOf(moveDetails.charAt(4))];
        }
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return this.startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return this.endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return this.promotionPiece;
    }

    @Override
    public String toString() {
        ChessPosition start = this.getStartPosition();
        ChessPosition end = this.getEndPosition();
        if (this.getPromotionPiece() == null) {
            return "" + "abcdefgh".charAt(start.getColumn() - 1) + start.getRow() + "abcdefgh".charAt(end.getColumn() - 1)
                    + end.getRow();
        }
        return "" + "abcdefgh".charAt(start.getColumn() - 1) + start.getRow() + "abcdefgh".charAt(end.getColumn() - 1)
                + end.getRow() + "kqbnrp".charAt(this.getPromotionPiece().ordinal());
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || (obj.getClass() != this.getClass())) {return false;}
        ChessMove other = (ChessMove) obj;
        return (other.getStartPosition().equals(this.getStartPosition()) && other.getEndPosition().equals(
                this.getEndPosition()) && (other.getPromotionPiece() == this.getPromotionPiece()));
    }

    @Override
    public int hashCode() {
        int promotionHash = switch (this.getPromotionPiece()) {
            case null: {
                yield 7;
            }
            default: {
                yield this.getPromotionPiece().ordinal();
            }
        };
        return (promotionHash * 10000 + startPosition.getColumn() * 1000 + startPosition.getRow() * 100 +
                endPosition.getColumn() * 10 + endPosition.getRow());
    }

}
