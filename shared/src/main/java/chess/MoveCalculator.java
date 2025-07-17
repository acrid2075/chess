package chess;

import java.util.ArrayList;

public class MoveCalculator {
    public ArrayList<ChessMove> kingMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        final int originalRow = position.getRow();
        final int originalCol = position.getColumn();
        int[][] directions = new int[][]{{-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}};
        for (int[] rowCol : directions) {
            int tempRow = originalRow + rowCol[0], tempCol = originalCol + rowCol[1];
            if ((tempRow >= 1) && (tempCol >= 1) && (tempRow <= 8) && (tempCol <= 8)) {
                ChessPosition tempPosition = new ChessPosition(tempRow, tempCol);
                ChessPiece tempPiece = board.getPiece(tempPosition);
                assert true;
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

    public ArrayList<ChessMove> queenMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        final int originalRow = position.getRow();
        final int originalCol = position.getColumn();
        int[][] directions = new int[][]{{-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}};
        for (int[] rowCol : directions) {
            int tempRow = originalRow + rowCol[0], tempCol = rowCol[1] + originalCol;
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

    public ArrayList<ChessMove> bishopMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
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
                    tempCol += rowCol[1];
                    tempRow += rowCol[0];
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
    public ArrayList<ChessMove> knightMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
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
    public ArrayList<ChessMove> rookMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        final int originalRow = position.getRow();
        final int originalCol = position.getColumn();
        int[][] directions = new int[][]{{-1, 0}, {0, 1}, {0, -1}, {1, 0}};
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
    public ArrayList<ChessMove> pawnMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        ArrayList<ChessPiece.PieceType> promotions = new ArrayList<ChessPiece.PieceType>();
        promotions.add(ChessPiece.PieceType.QUEEN);
        promotions.add(ChessPiece.PieceType.KNIGHT);
        promotions.add(ChessPiece.PieceType.ROOK);
        promotions.add(ChessPiece.PieceType.BISHOP);
        final int originalRow = position.getRow();
        final int originalCol = position.getColumn();
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            return whitePawnMoveCalculator(board, position, piece);
        }
        if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            return blackPawnMoveCalculator(board, position, piece);
        }
        return moves;
    }

    private ArrayList<ChessMove> whitePawnMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        ArrayList<ChessPiece.PieceType> promotions = new ArrayList<ChessPiece.PieceType>();
        promotions.add(ChessPiece.PieceType.QUEEN);
        promotions.add(ChessPiece.PieceType.KNIGHT);
        promotions.add(ChessPiece.PieceType.ROOK);
        promotions.add(ChessPiece.PieceType.BISHOP);
        final int originalRow = position.getRow();
        final int originalCol = position.getColumn();
        if (originalRow <= 6) {
            ChessPosition tempPosition = new ChessPosition(originalRow + 1, originalCol);
            ChessPiece tempPiece = board.getPiece(tempPosition);
            if (tempPiece == null) {
                moves.add(new ChessMove(position, tempPosition, null));
                if (originalRow == 2) {
                    tempPosition = new ChessPosition(originalRow + 2, originalCol);
                    tempPiece = board.getPiece(tempPosition);
                    if (tempPiece == null) {
                        moves.add(new ChessMove(position, tempPosition, null));
                    }
                }
            }
            if (originalCol < 8) {
                tempPosition = new ChessPosition(originalRow + 1, originalCol + 1);
                tempPiece = board.getPiece(tempPosition);
                if ((tempPiece != null) && (tempPiece.getTeamColor() == ChessGame.TeamColor.BLACK)) {
                    moves.add(new ChessMove(position, tempPosition, null));
                }
            }
            if (originalCol > 1) {
                tempPosition = new ChessPosition(originalRow + 1, originalCol - 1);
                tempPiece = board.getPiece(tempPosition);
                if ((tempPiece != null) && (tempPiece.getTeamColor() == ChessGame.TeamColor.BLACK)) {
                    moves.add(new ChessMove(position, tempPosition, null));
                }
            }
        }
        if (originalRow == 7) {
            ChessPosition tempPosition = new ChessPosition(originalRow + 1, originalCol);
            ChessPiece tempPiece = board.getPiece(tempPosition);
            if (tempPiece == null) {
                for (ChessPiece.PieceType otherPiece : promotions) {
                    moves.add(new ChessMove(position, tempPosition, otherPiece));
                }
            }
            if (originalCol < 8) {
                tempPosition = new ChessPosition(originalRow + 1, originalCol + 1);
                tempPiece = board.getPiece(tempPosition);
                if ((tempPiece != null) && (tempPiece.getTeamColor() == ChessGame.TeamColor.BLACK)) {
                    for (ChessPiece.PieceType otherPiece : promotions) {
                        moves.add(new ChessMove(position, tempPosition, otherPiece));
                    }
                }
            }
            if (originalCol > 1) {
                tempPosition = new ChessPosition(originalRow + 1, originalCol - 1);
                tempPiece = board.getPiece(tempPosition);
                if ((tempPiece != null) && (tempPiece.getTeamColor() == ChessGame.TeamColor.BLACK)) {
                    for (ChessPiece.PieceType otherPiece : promotions) {
                        moves.add(new ChessMove(position, tempPosition, otherPiece));
                    }
                }
            }
        }
        return moves;
    }

    private ArrayList<ChessMove> blackPawnMoveCalculator(ChessBoard board, ChessPosition position, ChessPiece piece) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        ArrayList<ChessPiece.PieceType> promotions = new ArrayList<ChessPiece.PieceType>();
        promotions.add(ChessPiece.PieceType.QUEEN);
        promotions.add(ChessPiece.PieceType.KNIGHT);
        promotions.add(ChessPiece.PieceType.ROOK);
        promotions.add(ChessPiece.PieceType.BISHOP);
        final int originalRow = position.getRow();
        final int originalCol = position.getColumn();
        if (originalRow >= 3) {
            ChessPosition tempPosition = new ChessPosition(originalRow - 1, originalCol);
            ChessPiece tempPiece = board.getPiece(tempPosition);
            if (tempPiece == null) {
                moves.add(new ChessMove(position, tempPosition, null));
                if (originalRow == 7) {
                    tempPosition = new ChessPosition(originalRow - 2, originalCol);
                    tempPiece = board.getPiece(tempPosition);
                    if (tempPiece == null) {
                        moves.add(new ChessMove(position, tempPosition, null));
                    }
                }
            }
            if (originalCol < 8) {
                tempPosition = new ChessPosition(originalRow - 1, originalCol + 1);
                tempPiece = board.getPiece(tempPosition);
                if ((tempPiece != null) && (tempPiece.getTeamColor() == ChessGame.TeamColor.WHITE)) {
                    moves.add(new ChessMove(position, tempPosition, null));
                }
            }
            if (originalCol > 1) {
                tempPosition = new ChessPosition(originalRow - 1, originalCol - 1);
                tempPiece = board.getPiece(tempPosition);
                if ((tempPiece != null) && (tempPiece.getTeamColor() == ChessGame.TeamColor.WHITE)) {
                    moves.add(new ChessMove(position, tempPosition, null));
                }
            }
        }
        if (originalRow == 2) {
            ChessPosition tempPosition = new ChessPosition(originalRow - 1, originalCol);
            ChessPiece tempPiece = board.getPiece(tempPosition);
            if (tempPiece == null) {
                for (ChessPiece.PieceType otherPiece : promotions) {
                    moves.add(new ChessMove(position, tempPosition, otherPiece));
                }
            }
            if (originalCol < 8) {
                tempPosition = new ChessPosition(originalRow - 1, originalCol + 1);
                tempPiece = board.getPiece(tempPosition);
                if ((tempPiece != null) && (tempPiece.getTeamColor() == ChessGame.TeamColor.WHITE)) {
                    for (ChessPiece.PieceType otherPiece : promotions) {
                        moves.add(new ChessMove(position, tempPosition, otherPiece));
                    }
                }
            }
            if (originalCol > 1) {
                tempPosition = new ChessPosition(originalRow - 1, originalCol - 1);
                tempPiece = board.getPiece(tempPosition);
                if ((tempPiece != null) && (tempPiece.getTeamColor() == ChessGame.TeamColor.WHITE)) {
                    for (ChessPiece.PieceType otherPiece : promotions) {
                        moves.add(new ChessMove(position, tempPosition, otherPiece));
                    }
                }
            }
        }
        return moves;
    }
}