/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameboi;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author thomas
 */
public class GameBoi {

    /**
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        
        Path rom_path = Paths.get(argv[0]);
        
        GBMem memory = new GBMem(rom_path);
        
        memory.readByte(0);

    }
    
}
