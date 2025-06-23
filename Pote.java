import java.util.concurrent.Semaphore;

public class Pote {
    private final int capacidadeMaxima;
    private int quantidadeMel = 0;
    private final Semaphore mutex = new Semaphore(1); // garante exclusão mútua

    public Pote(int capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public boolean adicionarMel() {
        try {
            mutex.acquire(); // entra na região crítica

            if (quantidadeMel < capacidadeMaxima) {
                quantidadeMel++;
                return true;
            }
            return false;

        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            mutex.release(); // sai da região crítica
        }
    }

    public boolean estaCheio() {
        return quantidadeMel >= capacidadeMaxima;
    }

    public void esvaziar() {
        try {
            mutex.acquire();
            quantidadeMel = 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.release();
        }
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public int getQuantidadeMel() {
        return quantidadeMel;
    }
}
