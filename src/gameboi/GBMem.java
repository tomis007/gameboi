/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameboi;


import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

/**
 * Represents the memory of the gameboy
 * <p>
 * Provides an interface for accessing the memory necessary
 * for the gameboy cpu and gpu operations
 * 
 * @author thomas
 */
public class GBMem {
    private int memory[];
    private int cartridge[];
    
    /**
     * Constructor for GBMem object
     * <p>
     * Initializes the GBMem object and loads the 'cartridge'
     * into memory
     * 
     * @param path  (required) Path object specifying the game rom to load
     */
    public GBMem(Path path) {
        memory = new int[0x10000];
        
        try {
            byte[] rom = Files.readAllBytes(path);
            cartridge = new int[rom.length];
            
            for (int i = 0; i < rom.length; ++i) {
                cartridge[i] = Byte.toUnsignedInt(rom[i]);
                System.out.println(cartridge[i]);
            }
            System.out.println("Number of bytes in file: " + rom.length);

        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
            System.exit(1);
        }
    }

    
    public int readByte(int address) {
        return 0;
    }
    
    public void writeByte(int address, int data) {

    }
    
    public int readWord(int address) {
        return 0;
    }
    
    public void writeWord(int address, int data) {
        
    }
    
}
