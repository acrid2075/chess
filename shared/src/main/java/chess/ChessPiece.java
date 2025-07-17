package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {return this.pieceColor;}

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        MoveCalculator calculator = new MoveCalculator();
        ArrayList<ChessMove> moves = switch (type) {
            case KING: {
                yield calculator.kingMoveCalculator(board, myPosition, this);
            }
            case QUEEN: {
                yield calculator.queenMoveCalculator(board, myPosition, this);
            }
            case BISHOP: {
                yield calculator.bishopMoveCalculator(board, myPosition, this);
            }
            case KNIGHT: {
                yield calculator.knightMoveCalculator(board, myPosition, this);
            }
            case ROOK: {
                yield calculator.rookMoveCalculator(board, myPosition, this);
            }
            case PAWN: {
                yield calculator.pawnMoveCalculator(board, myPosition, this);
            }
            default: {
                yield new ArrayList<ChessMove>();
            }
        };
        return moves;
    }
    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || (obj.getClass() != this.getClass())) {return false;}
        ChessPiece other = (ChessPiece) obj;
        return ((other.pieceColor == this.pieceColor) && (other.getPieceType() == this.getPieceType()));
    }
    @Override
    public int hashCode() {
        int color = 0;
        if (this.getTeamColor() == ChessGame.TeamColor.BLACK) {
            color = 10;
        }
        return this.getPieceType().ordinal() + color;
    }
}
