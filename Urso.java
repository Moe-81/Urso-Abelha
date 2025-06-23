public class Urso extends Thread {
    private final Pote pote;
    private final GameUI ui;
    private int vezesQueComeu = 0;
    private final int maxVezes;

    public Urso(Pote pote, GameUI ui, int maxVezes) {
        this.pote = pote;
        this.ui = ui;
        this.maxVezes = maxVezes;
    }

    @Override
    public void run() {
        while (vezesQueComeu < maxVezes) {
            synchronized (pote) {
                try {
                    while (!pote.estaCheio()) {
                        pote.wait();
                    }

                    vezesQueComeu++;
                    System.out.println("🐻 Urso acordou e comeu! Vezes: " + vezesQueComeu + "/" + maxVezes);
                    pote.esvaziar();
                    ui.atualizarPoteLabel();
                    ui.atualizarUrsoVisual(true, vezesQueComeu);

                    Thread.sleep(1500); // tempo acordado

                    ui.atualizarUrsoVisual(false, vezesQueComeu);
                    pote.notifyAll();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        ui.finalizarJogo();
    }
}
