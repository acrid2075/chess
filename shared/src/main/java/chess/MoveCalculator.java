package chess;

import java.util.ArrayList;

public class MoveCalculator {
    public ArrayList<ChessMove> KingMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        final int originalRow = position.getRow();
        final int originalCol = position.getColumn();
        int[][] directions = new int[][]{{-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}};
        for (int[] rowCol : directions) {
            int tempRow = originalRow + rowCol[0], tempCol = originalCol + rowCol[1];
            if ((tempRow >= 1) && (tempCol >= 1) && (tempRow <= 8) && (tempCol <= 8)) {
                ChessPosition tempPosition = new ChessPosition(tempRow, tempCol);
                ChessPiece tempPiece = board.getPiece(tempPosition);
                if (tempPiece == null) {
                    moves.add(new ChessMove(position, tempPosition, null));
                } else {
                    if (tempPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(position, tempPosition, null));
                    }
                }

            }
        }
        return moves;
    }

    public ArrayList<ChessMove> QueenMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        final int originalRow = position.getRow();
        final int originalCol = position.getColumn();
        int[][] directions = new int[][]{{-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}};
        for (int[] rowCol : directions) {
            int tempRow = originalRow + rowCol[0], tempCol = originalCol + rowCol[1];
            while ((tempRow >= 1) && (tempCol >= 1) && (tempRow <= 8) && (tempCol <= 8)) {
                ChessPosition tempPosition = new ChessPosition(tempRow, tempCol);
                ChessPiece tempPiece = board.getPiece(tempPosition);
                if (tempPiece == null) {
                    moves.add(new ChessMove(position, tempPosition, null));
                    tempRow += rowCol[0];
                    tempCol += rowCol[1];
                } else {
                    if (tempPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(position, tempPosition, null));
                    }
                    break;
                }

            }

        }
        return moves;
    }

    public ArrayList<ChessMove> BishopMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        final int originalRow = position.getRow();
        final int originalCol = position.getColumn();
        int[][] directions = new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] rowCol : directions) {
            int tempRow = originalRow + rowCol[0], tempCol = originalCol + rowCol[1];
            while ((tempRow >= 1) && (tempCol >= 1) && (tempRow <= 8) && (tempCol <= 8)) {
                ChessPosition tempPosition = new ChessPosition(tempRow, tempCol);
                ChessPiece tempPiece = board.getPiece(tempPosition);
                if (tempPiece == null) {
                    moves.add(new ChessMove(position, tempPosition, null));
                    tempRow += rowCol[0];
                    tempCol += rowCol[1];
                } else {
                    if (tempPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(position, tempPosition, null));
                    }
                    break;
                }

            }

        }
        return moves;
    }
    public ArrayList<ChessMove> KnightMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        final int originalRow = position.getRow();
        final int originalCol = position.getColumn();
        int[][] directions = new int[][]{{2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}, {2, -1}};
        for (int[] rowCol : directions) {
            int tempRow = originalRow + rowCol[0], tempCol = originalCol + rowCol[1];
            if ((tempRow >= 1) && (tempCol >= 1) && (tempRow <= 8) && (tempCol <= 8)) {
                ChessPosition tempPosition = new ChessPosition(tempRow, tempCol);
                ChessPiece tempPiece = board.getPiece(tempPosition);
                if (tempPiece == null) {
                    moves.add(new ChessMove(position, tempPosition, null));
                } else {
                    if (tempPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(position, tempPosition, null));
                    }
                }

            }
        }
        return moves;
    }
    public ArrayList<ChessMove> RookMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        final int originalRow = position.getRow();
        final int originalCol = position.getColumn();
        int[][] directions = new int[][]{{2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}, {2, -1}};
        for (int[] rowCol : directions) {
            int tempRow = originalRow + rowCol[0], tempCol = originalCol + rowCol[1];
            if ((tempRow >= 1) && (tempCol >= 1) && (tempRow <= 8) && (tempCol <= 8)) {
                ChessPosition tempPosition = new ChessPosition(tempRow, tempCol);
                ChessPiece tempPiece = board.getPiece(tempPosition);
                if (tempPiece == null) {
                    moves.add(new ChessMove(position, tempPosition, null));
                } else {
                    if (tempPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(position, tempPosition, null));
                    }
                }

            }
        }
        return moves;
    }
    public ArrayList<ChessMove> PawnMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        final int originalRow = position.getRow();
        final int originalCol = position.getColumn();
        int[][] directions = new int[][]{{2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}, {2, -1}};
        for (int[] rowCol : directions) {
            int tempRow = originalRow + rowCol[0], tempCol = originalCol + rowCol[1];
            if ((tempRow >= 1) && (tempCol >= 1) && (tempRow <= 8) && (tempCol <= 8)) {
                ChessPosition tempPosition = new ChessPosition(tempRow, tempCol);
                ChessPiece tempPiece = board.getPiece(tempPosition);
                if (tempPiece == null) {
                    moves.add(new ChessMove(position, tempPosition, null));
                } else {
                    if (tempPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(position, tempPosition, null));
                    }
                }

            }
        }
        return moves;
    }
}
