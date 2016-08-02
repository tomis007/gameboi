/*
 * Gameboi
 */
package gameboi;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author tomis007
 */
public class GameBoi {

    /**
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {

        Path rom_path = Paths.get(argv[0]);

        GBMem memory = new GBMem(rom_path);

        CPU z80 = new CPU(memory);

        GPU gpu = new GPU(memory, z80);

        z80.setGPU(gpu);
        while (true) {
            int count = 0;
            long startTime = System.nanoTime();
            while (count < 69905) {
                int cycles;
                cycles = z80.ExecuteOpcode();
                gpu.updateGraphics(cycles);
                count += cycles;
            }
            long sleepTime = 16700000 - (System.nanoTime() - startTime);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1000000, (int)sleepTime % 1000000);
                } catch (InterruptedException e) {
                    System.err.println("woops...");
                }
            }
        }
    }
}


