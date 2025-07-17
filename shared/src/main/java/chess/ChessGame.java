package chess;

import jdk.jshell.spi.ExecutionControl;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    ChessGame.TeamColor turn = TeamColor.WHITE;
    ChessBoard board;
    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece movingPiece = this.board.getPiece(startPosition);
        TeamColor team = movingPiece.getTeamColor();
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        ChessPiece capturedPiece;
        ChessPosition endPosition;
        if ((movingPiece == null)) {
            return moves;
        }
        Collection<ChessMove> potentialMoves = movingPiece.pieceMoves(this.board, startPosition);
        this.board.addPiece(startPosition, null);
        for (ChessMove move : potentialMoves) {
            endPosition = move.getEndPosition();
            capturedPiece = this.board.getPiece(endPosition);
            if (move.getPromotionPiece() == null) {
                this.board.addPiece(endPosition, movingPiece);
                if (!this.isInCheck(team)) {
                    moves.add(move);
                }
                this.board.addPiece(endPosition, capturedPiece);
            }
            if (move.getPromotionPiece() != null) {
                this.board.addPiece(endPosition, new ChessPiece(team, move.getPromotionPiece()));
                if (!this.isInCheck(team)) {
                    moves.add(move);
                }
                this.board.addPiece(endPosition, capturedPiece);
            }
        }
        this.board.addPiece(startPosition, movingPiece);
        // not finished

        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if ((this.board.getPiece(move.getStartPosition()) == null) || (this.board.getPiece(move.getStartPosition()).getTeamColor() != this.turn)) {
            throw new InvalidMoveException("Invalid move");
        }
        Collection<ChessMove> moves = validMoves(move.getStartPosition());
        if (!moves.contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }
        if (move.getPromotionPiece() == null) {
            this.board.addPiece(move.getEndPosition(), this.board.getPiece(move.getStartPosition()));
            this.board.addPiece(move.getStartPosition(), null);
            if (this.turn == TeamColor.BLACK) {
                this.turn = TeamColor.WHITE;
                return;
            }
            if (this.turn == TeamColor.WHITE) {
                this.turn = TeamColor.BLACK;
                return;
            }
        }
        this.board.addPiece(move.getEndPosition(), new ChessPiece(this.board.getPiece(move.getStartPosition()).getTeamColor(), move.getPromotionPiece()));
        this.board.addPiece(move.getStartPosition(), null);
        if (this.turn == TeamColor.BLACK) {
            this.turn = TeamColor.WHITE;
            return;
        }
        if (this.turn == TeamColor.WHITE) {
            this.turn = TeamColor.BLACK;
            return;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;
        ChessPosition temppos;
        ChessPiece temppiece;
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                temppos = new ChessPosition(i, j);
                temppiece = this.board.getPiece(temppos);
                kingPosition = ((temppiece != null) && (temppiece.getTeamColor() == teamColor) &&
                        (temppiece.getPieceType() == ChessPiece.PieceType.KING)) ? temppos : kingPosition;
            }
            if (kingPosition != null) {
                break;
            }
        }
        Collection<ChessMove> possibleMoves;
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                temppos = new ChessPosition(i, j);
                temppiece = this.board.getPiece(temppos);
                if (vulnerableKing(temppiece, teamColor, board, temppos,
                        kingPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean vulnerableKing(ChessPiece temppiece, TeamColor teamColor, ChessBoard board, ChessPosition temppos,
                                   ChessPosition kingPosition) {
        if ((temppiece != null) && (temppiece.getTeamColor() != teamColor)) {
            Collection<ChessMove> possibleMoves = temppiece.pieceMoves(board, temppos);
            for (ChessMove move : possibleMoves) {
                if (move.getEndPosition().equals(kingPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!this.isInCheck(teamColor)) {
            return false;
        }
        ChessPosition temppos;
        ChessPiece temppiece;
        Collection<ChessMove> moves;
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                temppos = new ChessPosition(i, j);
                temppiece = this.board.getPiece(temppos);
                if ((temppiece != null) && (temppiece.getTeamColor() == teamColor)) {
                    moves = this.validMoves(temppos);
                    for (ChessMove move: moves) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        ChessPosition temppos;
        ChessPiece temppiece;
        Collection<ChessMove> moves;
        if (this.isInCheck(teamColor)) {
            return false;
        }
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                temppos = new ChessPosition(i, j);
                temppiece = this.board.getPiece(temppos);
                if ((temppiece != null) && (temppiece.getTeamColor() == teamColor)) {
                    moves = this.validMoves(temppos);
                    for (ChessMove move: moves) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || (obj.getClass() != this.getClass())) {return false;}
        ChessGame other = (ChessGame) obj;
        return ((this.getTeamTurn() == other.getTeamTurn()) && (this.getBoard().equals(other.getBoard())));
    }

    @Override
    public int hashCode() {
        return this.getTeamTurn().ordinal() * 2000 + this.getBoard().hashCode();
    }
}
