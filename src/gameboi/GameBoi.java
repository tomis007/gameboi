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
 * @author tomis007
 */
public class GameBoi {

    /**
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        
        Path rom_path = Paths.get(argv[0]);
        
        GBMem memory = new GBMem(rom_path);
        
        memory.writeByte(0x9000, 8);
        System.out.println(memory.readByte(0x9000));
    }
    
}
