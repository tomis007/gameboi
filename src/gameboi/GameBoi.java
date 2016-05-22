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
        z80.ExecuteOpcode();
        z80.ExecuteOpcode();
        while (true) {
            z80.ExecuteOpcode();
        }
    }
    
}
