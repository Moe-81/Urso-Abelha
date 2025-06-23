import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class GameUI extends JFrame {
    private JLabel poteLabel;
    private JProgressBar barraMel;
    private JButton abelhaButton;
    private JLabel ursoLabel;
    private JLabel contadorLabel;
    private JLabel[] flores = new JLabel[6];

    private Timer animacaoAbelha;
    private int passos = 0;
    private int floresColetadas = 0;

    private final Pote pote = new Pote(2); // capacidade de 2 por pote
    private final Urso urso = new Urso(pote, this, 3); // urso come 3 vezes

    private ImageIcon ursoDormindoIcon;
    private ImageIcon ursoAcordadoIcon;
    private ImageIcon florAtivaIcon;

    public GameUI() {
        setTitle("🐝 Jogo do Urso e das Abelhas 🐻");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        carregarImagens();
        configurarCenario();

        setVisible(true);
        urso.start();
    }

    private ImageIcon carregarImagemRedimensionada(String caminho, int largura, int altura) {
        File imgFile = new File(caminho);
        if (imgFile.exists()) {
            ImageIcon icon = new ImageIcon(caminho);
            Image img = icon.getImage().getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            System.out.println("⚠️ Imagem não encontrada: " + caminho);
            return null;
        }
    }

    private void carregarImagens() {
        ursoDormindoIcon = carregarImagemRedimensionada("img/urso_dormindo.png", 200, 200);
        ursoAcordadoIcon = carregarImagemRedimensionada("img/urso_acordado.png", 200, 200);
        florAtivaIcon = carregarImagemRedimensionada("img/flor.png", 100, 100);
    }

    private void configurarCenario() {
        JPanel painel = new JPanel(null) {
            private Image background = new ImageIcon("img/cenario.png").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            }
        };
        painel.setOpaque(false);
        setContentPane(painel);

        barraMel = new JProgressBar(0, pote.getCapacidadeMaxima());
        barraMel.setValue(0);
        barraMel.setStringPainted(true);
        barraMel.setBounds(300, 50, 200, 30);
        painel.add(barraMel);

        poteLabel = new JLabel("Pote de mel: 0/" + pote.getCapacidadeMaxima());
        poteLabel.setBounds(320, 25, 200, 20);
        painel.add(poteLabel);
        poteLabel.setFont(new Font("Arial", Font.BOLD, 24));
        poteLabel.setForeground(Color.YELLOW);

        Point[] posicoes = {
                new Point(100, 300), new Point(200, 200), new Point(500, 250),
                new Point(150, 450), new Point(600, 400), new Point(400, 300)
        };

        for (int i = 0; i < flores.length; i++) {
            flores[i] = new JLabel(florAtivaIcon);
            flores[i].setBounds(posicoes[i].x, posicoes[i].y, 100, 100);
            painel.add(flores[i]);

            final int idx = i;
            flores[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (animacaoAbelha != null && animacaoAbelha.isRunning())
                        return;

                    JLabel flor = flores[idx];
                    flor.setEnabled(false);
                    flor.setVisible(false);

                    Point destino = flor.getLocation();
                    animarAteFlor(destino, null);
                }
            });
        }

        ImageIcon abelhaIcon = carregarImagemRedimensionada("img/abelha.png", 100, 100);
        abelhaButton = new JButton(abelhaIcon);
        abelhaButton.setContentAreaFilled(false);
        abelhaButton.setBorderPainted(false);
        abelhaButton.setBounds(350, 600, 100, 100);
        painel.add(abelhaButton);

        ursoLabel = new JLabel();
        ursoLabel.setBounds(300, 400, 200, 200);
        if (ursoDormindoIcon != null) {
            ursoLabel.setIcon(ursoDormindoIcon);
        } else {
            ursoLabel.setText("Urso dormindo");
        }
        painel.add(ursoLabel);

        contadorLabel = new JLabel("O urso comeu 0 vezes.");
        contadorLabel.setBounds(310, 580, 300, 30);
        contadorLabel.setFont(new Font("Arial", Font.BOLD, 19));
        painel.add(contadorLabel);
    }

    private void animarAteFlor(Point destinoFlor, Runnable aoFinalizar) {
        passos = 0;
        animacaoAbelha = new Timer(10, new ActionListener() {
            int startX = abelhaButton.getX();
            int startY = abelhaButton.getY();
            int destX = destinoFlor.x;
            int destY = destinoFlor.y;

            @Override
            public void actionPerformed(ActionEvent evt) {
                passos++;
                int newX = startX + (destX - startX) * passos / 50;
                int newY = startY + (destY - startY) * passos / 50;
                abelhaButton.setLocation(newX, newY);

                if (passos >= 50) {
                    animacaoAbelha.stop();
                    animarParaPote(aoFinalizar);
                }
            }
        });
        animacaoAbelha.start();
    }

    private void animarParaPote(Runnable aoFinalizar) {
        passos = 0;
        animacaoAbelha = new Timer(10, new ActionListener() {
            int startX = abelhaButton.getX();
            int startY = abelhaButton.getY();
            int destX = 350;
            int destY = 600;

            @Override
            public void actionPerformed(ActionEvent evt) {
                passos++;
                int newX = startX + (destX - startX) * passos / 50;
                int newY = startY + (destY - startY) * passos / 50;
                abelhaButton.setLocation(newX, newY);

                if (passos >= 50) {
                    animacaoAbelha.stop();
                    animarBarraMel();
                    if (aoFinalizar != null)
                        aoFinalizar.run();
                }
            }
        });
        animacaoAbelha.start();
    }

    private void animarBarraMel() {
        new Thread(() -> {
            synchronized (pote) {
                if (pote.estaCheio()) {
                    JOptionPane.showMessageDialog(this, "O pote está cheio!");
                    return;
                }

                boolean sucesso = pote.adicionarMel();
                if (!sucesso)
                    return;

                int valorAtual = barraMel.getValue();
                int valorFinal = pote.getQuantidadeMel();

                for (int i = valorAtual + 1; i <= valorFinal; i++) {
                    int finalI = i;
                    SwingUtilities.invokeLater(() -> barraMel.setValue(finalI));
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                }

                atualizarPoteLabel();

                if (pote.estaCheio()) {
                    pote.notifyAll();
                }
            }
        }).start();
    }

    public void atualizarPoteLabel() {
        barraMel.setValue(pote.getQuantidadeMel());
        poteLabel.setText("Pote de mel: " + pote.getQuantidadeMel() + "/" + pote.getCapacidadeMaxima());
    }

    public void atualizarUrsoVisual(boolean acordado, int vezes) {
        SwingUtilities.invokeLater(() -> {
            if (acordado && ursoAcordadoIcon != null) {
                ursoLabel.setIcon(ursoAcordadoIcon);
            } else if (!acordado && ursoDormindoIcon != null) {
                ursoLabel.setIcon(ursoDormindoIcon);
            }
            contadorLabel.setText("O urso comeu " + vezes + " vezes.");
        });
    }

    public void finalizarJogo() {
        SwingUtilities.invokeLater(() -> {
            abelhaButton.setEnabled(false);
            JOptionPane.showMessageDialog(this, "O urso entrou em hibernação. Fim de jogo!");
        });
    }
}
