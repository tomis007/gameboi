/*
 * Gameboi
 */
package gameboi;
import java.nio.file.Path;
import java.io.File;

/**
 *
 * @author tomis007
 */
public class GameBoi {

    /**
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        GBMem memory = new GBMem(loadRom());
        CPU z80 = new CPU(memory);
        GPU gpu = new GPU(memory, z80);

        //Start the Gameboy fetch,decode,execute cycle
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

    /**
     * gets Path object to a Rom with a simple GUI
     *
     * @return Path to selected rom
     */
    private static Path loadRom() {
        FileSelector fc = new FileSelector(System.getProperty("user.dir"));

        File rom = fc.selectFile();
        if (rom == null) {
            System.err.println("Sorry, please select a ROM");
            System.exit(1);
        }

        return rom.toPath();
    }


}


