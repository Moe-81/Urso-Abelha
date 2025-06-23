import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Semaphore;

public class GameUI extends JFrame {

    private final Semaphore poteLock = new Semaphore(1);

    private JLabel abelhaLabel;
    private JLabel ursoLabel;
    private JLabel poteLabel;
    private JProgressBar barraMel;
    private JButton botaoFlor;

    private Pote pote;

    public GameUI() {
        pote = new Pote(1);

        setTitle("Urso e Abelhas");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        ImageIcon abelhaIcon = new ImageIcon("img/abelha.png");
        Image imgAbelha = abelhaIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        abelhaLabel = new JLabel(new ImageIcon(imgAbelha));
        abelhaLabel.setBounds(100, 600, 100, 100);
        add(abelhaLabel);

        ImageIcon ursoIcon = new ImageIcon("img/urso_dormindo.png");
        Image imgUrso = ursoIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        ursoLabel = new JLabel(new ImageIcon(imgUrso));
        ursoLabel.setBounds(300, 100, 200, 200);
        add(ursoLabel);

        ImageIcon poteIcon = new ImageIcon("img/pote.png");
        Image imgPote = poteIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        poteLabel = new JLabel(new ImageIcon(imgPote));
        poteLabel.setBounds(350, 500, 100, 100);
        add(poteLabel);

        barraMel = new JProgressBar(0, pote.getCapacidadeMaxima());
        barraMel.setBounds(300, 620, 200, 30);
        barraMel.setStringPainted(true);
        add(barraMel);

        botaoFlor = new JButton("Coletar mel da flor");
        botaoFlor.setBounds(300, 700, 200, 30);
        botaoFlor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                coletarMel();
            }
        });
        add(botaoFlor);

        Urso urso = new Urso(pote, this, 3);
        urso.start();

        setVisible(true);
    }

    private void coletarMel() {
        new Thread(() -> {
            try {
                poteLock.acquire();

                if (pote.estaCheio()) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "O pote está cheio!");
                    });
                    return;
                }

                boolean sucesso = pote.adicionarMel();
                if (!sucesso) return;

                int valorAtual = barraMel.getValue();
                int valorFinal = pote.getQuantidadeMel();

                for (int i = valorAtual + 1; i <= valorFinal; i++) {
                    int finalI = i;
                    SwingUtilities.invokeLater(() -> barraMel.setValue(finalI));
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {}
                }

                SwingUtilities.invokeLater(this::atualizarPoteLabel);

                synchronized (pote) {
                    if (pote.estaCheio()) {
                        pote.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                poteLock.release();
            }
        }).start();
    }

    public void atualizarPoteLabel() {
        barraMel.setValue(pote.getQuantidadeMel());
    }

    public void atualizarUrsoVisual(boolean acordado, int vezes) {
        if (acordado) {
            ursoLabel.setIcon(new ImageIcon("img/urso_comendo.png"));
        } else {
            ursoLabel.setIcon(new ImageIcon("img/urso_dormindo.png"));
        }
    }

    public void finalizarJogo() {
        JOptionPane.showMessageDialog(this, "O urso hibernou! Fim do jogo.");
        System.exit(0);
    }

    public static void main(String[] args) {
        new GameUI();
    }
}
