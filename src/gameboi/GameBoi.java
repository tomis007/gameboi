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
        int count = 0;
        while (true) {
            int cycles;
//            count++;
            cycles = z80.ExecuteOpcode();
            gpu.updateGraphics(cycles);
//            System.out.println(count);
        }
  //      System.out.println(count);
//        System.exit(1);

    }
    
}
