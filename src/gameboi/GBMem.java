/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameboi;


import sun.misc.ASCIICaseInsensitiveComparator;
import sun.misc.FloatingDecimal;

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
    private int[] memory;
    private int[] cartridge;
    private MemBanks memBank;
    private int[] vRam;
    private int[] extRam;
    private int[] wRam;
    private int[] rom; //just for now without MBC (tetris/dr mario)
    private int[] OAMTable;
    private int[] IOPorts;
    private int[] HRam;



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
        this.rom = new int[0x8000];
        vRam = new int[0x2000];
        wRam = new int[0x2000];
        OAMTable = new int[0xa0];
        extRam = new int[0x2000];
        IOPorts = new int[0x80];
        HRam = new int[0x80];


        try {
            byte[] rom = Files.readAllBytes(path);
            cartridge = new int[rom.length];

            for (int i = 0; i < rom.length; ++i) {
                cartridge[i] = Byte.toUnsignedInt(rom[i]);
            }
            for (int i = 0; i < 0x8000 && i < rom.length; ++i) {
                this.rom[i] = cartridge[i];
                memory[i] = cartridge[i];
            }
            for (int i = 0x8000; i < 0xa000 && i < rom.length; ++i) {
                vRam[i - 0x8000] = cartridge[i];
            }
            for (int i = 0xa000; i < 0xc000 && i < rom.length; ++i) {
                extRam[i - 0xa000] = cartridge[i];
            }
            for (int i = 0xc000; i < 0xe000 && i < rom.length; ++i) {
                wRam[i - 0xc000] = cartridge[i];
            }
            for (int i = 0xfe00; i < 0xfe9f && i < rom.length; ++i) {
                OAMTable[i - 0xfe00] = cartridge[i];
            }

            //memBank = new MemBanks(cartridge);

        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
            System.exit(1);
        }
        joyPadState = 0xff;

        rom[0x104] = 0xce;
        rom[0x105] = 0xed;
        rom[0x106] = 0x66;
        rom[0x107] = 0x66;
        rom[0x108] = 0xcc;
        rom[0x109] = 0x0d;
        rom[0x10a] = 0x00;
        rom[0x10b] = 0x0b;
        rom[0x10c] = 0x03;
        rom[0x10d] = 0x73;
        rom[0x10e] = 0x00;
        rom[0x10f] = 0x83;
        rom[0x110] = 0x00;
        rom[0x111] = 0x0c;
        rom[0x112] = 0x00;
        rom[0x113] = 0x0d;
        rom[0x114] = 0x00;
        rom[0x115] = 0x08;
        rom[0x116] = 0x11;
        rom[0x117] = 0x1f;
        rom[0x118] = 0x88;
        rom[0x119] = 0x89;
        rom[0x11a] = 0x00;
        rom[0x11b] = 0x0e;
        rom[0x11c] = 0xdc;
        rom[0x11d] = 0xcc;
        rom[0x11e] = 0x6e;
        rom[0x11f] = 0xe6;
        rom[0x120] = 0xdd;
        rom[0x121] = 0xdd;
        rom[0x122] = 0xd9;
        rom[0x123] = 0x99;
        rom[0x124] = 0xbb;
        rom[0x125] = 0xbb;
        rom[0x126] = 0x67;
        rom[0x127] = 0x63;
        rom[0x128] = 0x6e;
        rom[0x129] = 0x0e;
        rom[0x12a] = 0xec;
        rom[0x12b] = 0xcc;
        rom[0x12c] = 0xdd;
        rom[0x12d] = 0xdc;
        rom[0x12e] = 0x99;
        rom[0x12f] = 0x9f;
        rom[0x130] = 0xbb;
        rom[0x131] = 0xb9;
        rom[0x132] = 0x33;
        rom[0x133] = 0x3e;



        //for bios checksum
        rom[0x14d] = 0xe7;


        //initialize values in memory
        //gb bios leaves this state after running
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

        for (int i = 0xff00; i < 0xff80; ++i) {
            IOPorts[i - 0xff00] = memory[i];
        }
        for (int i = 0xff80; i < 0xffff; ++i) {
            HRam[i - 0xff80] = memory[i];
        }

    }

    /**
     * Read a 'byte' from memory.
     *
     *
     * NOTE: RIGHT NOW ONLY IMPLEMENTING FOR NO MEMORY BANKS
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
            int data =  translateJoyPad();
            return IOPorts[0] | data; //todo Not 100% sure
        } else if (address < 0x8000) {
            return rom[address];
        } else if (address < 0xa000) {
            return vRam[address - 0x8000];
        } else if (address < 0xc000) {
            return extRam[address - 0xa000];
        } else if (address < 0xe000) {
            return wRam[address - 0xc000];
        } else if (address < 0xfe00) {
            return wRam[address - 0xe000];
        } else if (address < 0xfe9f) {
            return OAMTable[address - 0xfe00];
        } else if (address < 0xff00) {
            return -1; // can't use this area
        } else if (address < 0xff80){
            return IOPorts[address - 0xff00];
        } else if (address < 0x10000){
            return HRam[address - 0xff80];
        } else {
            System.err.println("Ooops reading from invalid address");
            return -1; //oops something went wrong
        }

    }

//        } else if (((address >= 0x4000) && (address <= 0x7fff)) ||
//                ((address >= 0xa000) && (address <= 0xbfff))) {
            //rom or ram banking
//            return memBank.readByte(address);
//        } else {
//            return memory[address];
//        }
//    }

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
        
        //for BLARGG's test ROMS
        if (address == 0xff02) {
            if (data == 0x81) {
                char output = (char)IOPorts[1];
                System.out.print(output);
            }
        }

        if (address < 0x8000) {
            //ROM
        } else if (address < 0xa000){
            vRam[address - 0x8000] = data;
        } else if (address < 0xc000) {
            extRam[address - 0xa000] = data;
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



/*        if (address < 0) {
            System.err.println("ERROR: writing to negative address");
        } else if (address < 0x8000) {
            // can't write to ROM, but update banks
//            memBank.updateBanking(address, data);

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
        }*/
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
     * @param address
     * @param data
     */
    private void handleIOWriting(int address, int data) {
        int newAddress = address - 0xff00;
        if (address == 0xff04) {
            IOPorts[newAddress] = 0; //reset DIV register
        } else if (address == 0xff44) {
            IOPorts[newAddress] = 0; //reset LCDC y-Coordinate
        } else if (address == 0xff46) {
            DMATransfer(data);
        } else {
            IOPorts[newAddress] = data;
        }
    }



    /**
     * preforms a DMA transfer
     *
     * TODO (PROBABLY REALLY SLOW)
     * @param data address to start copy at divided by 0x100
     */ 
    private void DMATransfer(int data) {
        int address = data << 8;

        //TODO MAKE FASTER?
        for (int i = 0; i < 0xa0; ++i) {
            OAMTable[i] = readByte(address + i);
        }
    }
    
    /**
     * sets the scanline
     * @param num new scanline value
     */ 
    public void setScanLine(int num) {
        //memory[0xff44] = num;
        IOPorts[0x44] = num;
    }  
    
    /**
     * increments the scanLine at 0xff44
     */ 
    public void incScanLine() {
        //memory[0xff44]++;
        IOPorts[0x44]++;
    }
    
    /**
     * increments the divide counter at 0xff04
     * 
     */ 
    public void incrementDivider() {
//        memory[0xff04] = (memory[0xff04] + 1) & 0xff;
        IOPorts[0x04] = (IOPorts[0x04] + 1) & 0xff;
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
     * KEY 7 - START
     * KEY 6 - SELECT
     * KEY 5 - B
     * KEY 4 - A
     * KEY 3 - DOWN
     * KEY 2 - UP
     * KEY 1 - LEFT
     * KEY 0 - RIGHT
     */
    //TODO
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
