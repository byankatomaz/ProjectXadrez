package gui;

import chess.ChessMatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChessGUI extends JFrame {

    private ChessMatch chessMatch;
    private JButton[][] chessBoardSquares = new JButton[8][8];

    public ChessGUI() {
        chessMatch = new ChessMatch();
        initializeGUI();
    }

    private void initializeGUI() {
        // Configure a janela principal
        setTitle("Jogo de Xadrez");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLayout(new GridLayout(8, 8));

        // Crie os botões para representar o tabuleiro
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JButton button = new JButton();
                chessBoardSquares[row][col] = button;
                add(button);

                // Adicione um ActionListener para responder aos cliques
                button.addActionListener(new ChessBoardListener(row, col));
            }
        }

        // Atualize a interface com o estado inicial do jogo
        updateChessboard();
    }

    private void updateChessboard() {
        // Atualize a interface com base no estado do jogo
        // Você deve implementar essa lógica de acordo com a estrutura do seu jogo
    }

    private class ChessBoardListener implements ActionListener {
        private int row;
        private int col;

        public ChessBoardListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Lide com os cliques do jogador aqui
            // Por exemplo, mova as peças no jogo e atualize a interface
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChessGUI gui = new ChessGUI();
            gui.setVisible(true);
        });
    }
}
