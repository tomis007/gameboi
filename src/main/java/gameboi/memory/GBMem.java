/*
 * The MIT License
 *
 * Copyright 2016 tomis007.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package main.java.gameboi.memory;


import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.Arrays;

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
    private MemBanks memBank;
    private int[] vRam;
    private int[] wRam;
    private int[] OAMTable;
    private int[] IOPorts;
    private int[] HRam;


    //saving byte size info
    private static final int BYTE_SAVE_LENGTH = 0x41a1;

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
     * Constructor for gameboy memory
     *
     *
     *
     */
    public GBMem() {
        vRam = new int[0x2000];
        wRam = new int[0x2000];
        OAMTable = new int[0xa0];
        IOPorts = new int[0x80];
        HRam = new int[0x80];
        joyPadState = 0xff; //no keys pressed
        memBank = null;

        //gb 'bios' leaves in this state
        IOPorts[0x10] = 0x80;
        IOPorts[0x11] = 0xbf;
        IOPorts[0x12] = 0xf3;
        IOPorts[0x14] = 0xbf;
        IOPorts[0x16] = 0x3f;
        IOPorts[0x19] = 0xbf;
        IOPorts[0x1a] = 0x7f;
        IOPorts[0x1b] = 0xff;
        IOPorts[0x1c] = 0x9f;
        IOPorts[0x1e] = 0xbf;
        IOPorts[0x20] = 0xff;
        IOPorts[0x23] = 0xbf;
        IOPorts[0x24] = 0x77;
        IOPorts[0x25] = 0xf3;
        IOPorts[0x26] = 0xf1;
        IOPorts[0x40] = 0x91;
        IOPorts[0x47] = 0xfc;
        IOPorts[0x48] = 0xff;
        IOPorts[0x49] = 0xff;
    }


    /**
     * Save the current memory state to a byte array for resuming
     * Doesn't save the ROM (needs to be loaded with loadRom)
     *
     * @return current state of memory (in a long byte array)
     */
    public byte[] saveState() {
        byte[] save = new byte[BYTE_SAVE_LENGTH];
        copyArray(vRam, 0, save, 0, 0x2000);
        copyArray(wRam, 0, save, 0x2000, 0x2000);
        copyArray(OAMTable, 0, save, 0x4000, 0xa0);
        copyArray(IOPorts, 0, save, 0x40a0, 0x80);
        copyArray(HRam, 0, save, 0x4120, 0x80);
        save[BYTE_SAVE_LENGTH - 1] = (byte)(joyPadState & 0xff);
        return save;
    }


    /**
     * Load the current memory state from the byte array generated in saveMem
     * Needs the rom to be loaded as well to play the game TODO
     *
     */
    public void loadState(byte[] save) {
        copyArray(save, 0, vRam, 0, 0x2000);
        copyArray(save, 0x2000, wRam, 0, 0x2000);
        copyArray(save, 0x4000, OAMTable, 0, 0xa0);
        copyArray(save, 0x40a0, IOPorts, 0, 0x80);
        copyArray(save, 0x4120, HRam, 0, 0x80);
        joyPadState = Byte.toUnsignedInt(save[BYTE_SAVE_LENGTH - 1]);
    }


    /**
     * Overloaded copyArray for saving
     *
     * @param src array
     * @param srcPos initial position
     * @param dst byte destination array
     * @param dstPos initial pos
     * @param len len of items to copy
     */
    private void copyArray(int[] src, int srcPos, byte[] dst, int dstPos, int len) {
        for (int i = 0; i < len; ++i) {
            dst[i + dstPos] = (byte)(src[i + srcPos] & 0xff);
        }

    }

    /**
     * Overloaded copyArray for saving
     * @param src byte array
     * @param srcPos initial pos
     * @param dst int destination
     * @param dstPos initial pos
     * @param len len of items to copy
     */
    private void copyArray(byte[] src, int srcPos, int[] dst, int dstPos, int len) {
        for (int i = 0; i < len; ++i) {
            dst[dstPos + i] = Byte.toUnsignedInt(src[i + srcPos]);
        }
    }

    /**
     * The length of the save byte state array
     * @return
     */
    public static int byteSaveLength() {
        return BYTE_SAVE_LENGTH;
    }


    /**
     *
     * Initialize a rom into memory
     *
     *
     * @param path of rom to load
     */
    public void loadRom(Path path) {
        try {
            byte[] rom = Files.readAllBytes(path);
            int[] cartridge = new int[rom.length];

            for (int i = 0; i < rom.length; ++i) {
                cartridge[i] = Byte.toUnsignedInt(rom[i]);
            }

            memBank = new MemBanks(cartridge);
        } catch (IOException e) {
            System.err.println("Error Loading rom: " + e.getMessage());
            System.exit(1); // TODO probably not
        }
    }

    /**
     * Read a 'byte' from memory.
     *
     *
     * NOTE: RIGHT NOW ONLY IMPLEMENTING FOR NO MEMORY BANKS TODO ??
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
        if (address == 0xff00) {
            return translateJoyPad();
        } else if (address < 0x8000) {
            return memBank.readByte(address);
        } else if (address < 0xa000) {
            return vRam[address - 0x8000];
        } else if (address < 0xc000) {
            return memBank.readByte(address);
        } else if (address < 0xe000) {
            return wRam[address - 0xc000];
        } else if (address < 0xfe00) {
            return wRam[address - 0xe000];
        } else if (address < 0xfea0) {
            return OAMTable[address - 0xfe00];
        } else if (address < 0xff00) {
            return -1; // can't use this area
        } else if (address < 0xff80){
            return IOPorts[address - 0xff00];
        } else if (address < 0x10000){
            return HRam[address - 0xff80];
        } else {
            System.err.println("Reading from invalid address");
            return -1; //oops something went wrong
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
        data &= 0xff;

        if (address < 0x8000) {
            memBank.writeByte(address, data);
        } else if (address < 0xa000){
            vRam[address - 0x8000] = data;
        } else if (address < 0xc000) {
            memBank.writeByte(address, data);
        } else if (address < 0xe000) {
            wRam[address - 0xc000] = data;
        } else if (address < 0xfe00) {
            wRam[address - 0xe000] = data; //ECHO
        } else if (address < 0xfea0) {
            OAMTable[address - 0xfe00] = data;
        } else if (address < 0xff00) {
            //cant do anything here
        } else if (address < 0xff80) {
            handleIOWriting(address, data);
        } else if (address < 0x10000) {
            HRam[address - 0xff80] = data;
        }
    }
    
    /**
     * Returns current scanline
     * 
     * @return memory[0xff44]
     */ 
    public int getScanLine() {
        return IOPorts[0x44];
    }


    /**
     * handleIOWriting
     *
     * Writes a byte to the IO PORTs in memory
     * Handles the reset cases for the registers stored here
     *
     * @param address to write to (an IO port address)
     * @param data to write
     */
    private void handleIOWriting(int address, int data) {
        int newAddress = address - 0xff00;

        if (address == 0xff04) {
            IOPorts[newAddress] = 0; //reset DIV register
        } else if (address == 0xff44) {
            IOPorts[newAddress] = 0; //reset LCDC y-Coordinate
        } else if (address == 0xff46) {
            DMATransfer(data);
        } else if (address == 0xff40) {
            IOPorts[newAddress] = data;
        } else {
            IOPorts[newAddress] = data;
        }
    }



    /**
     * preforms a DMA transfer
     *
     * @param data address to start copy at divided by 0x100
     */ 
    private void DMATransfer(int data) {
        int address = data * 0x100;

        for (int i = 0; i < 0xa0; ++i) {
            OAMTable[i] = readByte(address + i);
        }
    }
    
    /**
     * sets the scanline
     * @param num new scanline value
     */ 
    public void setScanLine(int num) {
        IOPorts[0x44] = num;
    }  
    
    /**
     * increments the scanLine at 0xff44
     */ 
    public void incScanLine() {
        IOPorts[0x44]++;
    }
    
    /**
     * increments the divide counter at 0xff04
     */
    public void incrementDivider() {
        IOPorts[0x04] = (IOPorts[0x04] + 1) & 0xff;
    }

    /**
     * sets the state register coincidence bit
     * @param bit to set to
     */
    public void setLCDCoincidence(int bit) {
        bit &= 1;
        IOPorts[0x41] &= ~4 & 0xff;
        IOPorts[0x41] |= bit << 2;

    }

    /**
     *
     * increment the TIMA counter
     */
    public void incrementTIMA() {
        IOPorts[0x05] = (IOPorts[0x05] + 1) & 0xff;
    }

    /**
     *
     * reset the TIMA clock to TMA
     */
    public void resetTIMA() {
        IOPorts[0x05] = IOPorts[0x06];
    }


    /**
     * Sets the current joypad state to nextState
     * @param nextState value to set
     */ 
    public void updateJoyPadState(int nextState) {
        joyPadState = nextState & 0xff;
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
     * translates the keyboard input into
     * the joypad input required for
     * gameboy programs
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
        int requests = IOPorts[0];
        int joypad = 0xff;

        if (!isSet(requests, 4)) {
            joypad = joyPadState & 0xf;
        } else if (!isSet(requests, 5)) {
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
