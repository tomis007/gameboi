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
    private MemBanks memBank;
    
    /**
     * KEY 7 - SELECT
     * KEY 6 - START
     * KEY 5 - B
     * KEY 4 - A
     * KEY 3 - RIGHT
     * KEY 2 - LEFT
     * KEY 1 - UP
     * KEY 0 - DOWN
     */
    private int joyPadState;
    
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
            for (int i = 0; i < 0x8000 && i < rom.length; ++i) {
                memory[i] = cartridge[i];
            }
            
            memBank = new MemBanks(cartridge);
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
            System.exit(1);
        }
        joyPadState = 0xff;
        
        //initialize values in memory
        memory[0xff05] = 0x0;
        memory[0xff06] = 0x0;
        memory[0xff07] = 0x0;
        memory[0xff10] = 0x80;
        memory[0xff11] = 0xbf;
        memory[0xff12] = 0xf3;
        memory[0xff14] = 0xbf;
        memory[0xff16] = 0x3f;
        memory[0xff17] = 0x0;
        memory[0xff19] = 0xbf;
        memory[0xff1a] = 0x7f;
        memory[0xff1b] = 0xff;
        memory[0xff1c] = 0x9f;
        memory[0xff1e] = 0xbf;
        memory[0xff20] = 0xff;
        memory[0xff21] = 0x0;
        memory[0xff22] = 0x0;
        memory[0xff23] = 0xbf;
        memory[0xff24] = 0x77;
        memory[0xff25] = 0xf3;
        memory[0xff26] = 0xf1;
        memory[0xff40] = 0x91;
        memory[0xff42] = 0x0;
        memory[0xff43] = 0x0;
        memory[0xff45] = 0x0;
        memory[0xff47] = 0xfc;
        memory[0xff48] = 0xff;
        memory[0xff49] = 0xff;
        memory[0xff4a] = 0x0;
        memory[0xff4b] = 0x0;
        memory[0xffff] = 0x0;
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
        
        //for debugging
        if (address == 0xff80) {
//            System.out.println("reading 0x" + memory[0xff80] + " from 0xff80");
        }
        
        if (address == 0xff00) {
            return translateJoyPad();
        } else if (((address >= 0x4000) && (address <= 0x7fff)) || 
                ((address >= 0xa000) && (address <= 0xbfff))) {
            //rom or ram banking
            return memBank.readByte(address);
        } else {
            return memory[address];
        }
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
        data = data & 0xff;
        
        //for debugging TODO
        if (address == 0xff80) {
            return;
        }
        
        if (address < 0) {
            System.err.println("ERROR: writing to negative address");
            System.exit(1);
        } else if (address < 0x8000) {
            // can't write to ROM, but update banks
            memBank.updateBanking(address, data);
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
        } else if ((address == 0xff04) || (address == 0xff44)) { 
            //write to divide counter or scanLine means reset to 0
            memory[address] = 0;
        } else  if (address == 0xff46) {
            DMATransfer(data);
        } else {
            if (memBank.isRamEnabled()) {
                memBank.writeByte(address, data);
            } else {
                memory[address] = data;
            }
        }
    }
    
    /**
     * Returns current scanline
     * 
     * @return memory[0xff44]
     */ 
    public int getScanLine() {
        return memory[0xff44];
    }
    
    
    
    /**
     * preforms a DMA transfer 
     */ 
    private void DMATransfer(int data) {
        int address = data * 0x100;
        for (int i = 0; i < 0xa0; ++i) {
            memory[0xfe00 + i] = memory[address + 1];
        }
    }
    
    /**
     * sets the scanline
     * @param num new scanline value
     */ 
    public void setScanLine(int num) {
        memory[0xff44] = num;
    }  
    
    /**
     * increments the scanLine at 0xff44
     */ 
    public void incScanLine() {
        memory[0xff44]++;
    }
    
    /**
     * increments the divide counter at 0xff04
     * 
     */ 
    public void incrementDivider() {
        memory[0xff04] = (memory[0xff04] + 1) & 0xff;
    }
    
    
    /**
     * Sets the current joypad state to nextSTate
     * @param nextState value to set
     */ 
    public void updateJoyPadState(int nextState) {
        joyPadState = nextState;
    }
    
    /**
     * returns the current value of joyPadState
     * @return current joypad state
     */ 
    public int getJoyPadState() {
        return joyPadState;
    }
    
    
    /**
     *
     * KEY 7 - START
     * KEY 6 - SELECT
     * KEY 5 - B
     * KEY 4 - A
     * KEY 3 - DOWN
     * KEY 2 - UP
     * KEY 1 - LEFT
     * KEY 0 - RIGHT
     */ 
    private int translateJoyPad() {
        int requests = memory[0xff00];

        int joypad = 0xff;
        // interested in directional pad
        if (!isSet(requests, 5)) {
            joypad = joyPadState & 0xf;
        } else if (!isSet(requests, 4)) {
            //interested in other buttons
            joypad = (joyPadState & 0xf0) >> 4;
        }
        return joypad;
    }
    
   /**
     * isSet
     * 
     * Tests the num if the bitNum bit is set
     * 
     * @param num number to test
     * @param bitNum bitnumber to test
     */ 
    private boolean isSet(int num, int bitNum) {
        return (((num >> bitNum) & 0x1) == 1);
    }    
    
}
