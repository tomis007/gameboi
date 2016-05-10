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
 * <p>
 * Internal Memory structure of the gameboy is as follows
 * <ul>
 *   <li> 0000-3FFF - 16Kb ROM Bank 00</li>
 *   <li> 4000-7FFF - 16Kb ROM variable bank</li>
 *   <li> 8000-9FFF - 8Kb Graphics RAM</li>
 *   <li> A000-BFFF - 8Kb Switchable External RAM Bank</li>
 *   <li> C000-DFFF - 8Kb Working RAM</li>
 *   <li> E000-FDFF - Working RAM shadow</li>
 *   <li> FE00-FE9F - Graphics + Sprite Info (OAM)</li>
 *   <li> FEA0-FEFF - Unusable</li>
 *   <li> FF00-FF7F - I/O Info</li>
 *   <li> FF80-FFFE - Zero-Page RAM</li>
 *   <li> FFFF      - Interrupt Enable Register</li>
 * </ul>
 * 
 * @author tomis007
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
            }
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Read a 'byte' from memory.
     * 
     * <p> Returns an int with the value of the byte stored in memory
     *     at address.
     * 
     * @param address (required) address to read data in memory from
     * @return an int that is the value of the data stored in memory
     *     at address
     * @see GBMem
     */ 
    public int readByte(int address) {
        return memory[address];
    }

    /**
     * Write a 'byte' to the gameboy memory.
     * 
     * <p> Writes the input int data to gameboy memory, only storing the low
     *     8 bits.
     * 
     * @param address (required) int specifying valid address to write at 
     * @param data (required) int 'byte' data to write
     * @see GBMem
     */ 
    public void writeByte(int address, int data) {
        //only store a byte in memory
        data = data & 0x0ff;
        
        if (address < 0) {
            System.err.println("ERROR: writing to negative address");
            System.exit(1);
        } else if (address < 0x8000) {
            // can't write to ROM
        } else if ((address >= 0xfea0) && (address <= 0xfeff)) {
            // can't access this region
        } else if ((address >= 0xc000) && (address <= 0xde00)) {
            memory[address] = data;
            // ECHO
            memory[address + 0x2000] = data;
        } else if ((address >= 0xc000) && (address <= 0xfe00)) {
            memory[address] = data;
            // ECHO
            memory[address - 0x2000] = data;
        } else {
            memory[address] = data;
        }
    }
    
    public int readWord(int address) {
        return 0;
    }
    
    public void writeWord(int address, int data) {
        
    }
    
}
