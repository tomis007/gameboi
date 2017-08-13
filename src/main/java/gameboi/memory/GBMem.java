/*
 * The MIT License
 *
 * Copyright 2017 tomis007.
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
    private int[] vRam1;
    private int[] wRam;
    private int[] wRamBanks;
    private int wRamIndex;
    private int[] OAMTable;
    private int[] IOPorts;
    private int[] HRam;
    private int bankNum;

    //gbc color register
    private boolean autoInc;
    private int bgColorIndex;
    private int[] bgPalettes;

    //gbc sprite reg
    private boolean autoSpriteInc;
    private int spriteIndex;
    private int[] spritePalettes;

    //if current ROM is gbc
    private boolean gbcMode;

    private boolean hBlankDMAInProgress;
    private int dmaTransferLength;
    private int dmaSrc;
    private int dmaDst;

    //saving byte size info
    private static final int RAM_SAVE_LEN = MemBanks.getByteSaveSize();
    private static final int BYTE_SAVE_LENGTH = 0x41a1 + RAM_SAVE_LEN;

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
        vRam1 = new int[0x2000];
        wRamBanks = new int[0x7000];
        wRamIndex = 1;
        wRam = new int[0x2000];
        OAMTable = new int[0xa0];
        IOPorts = new int[0x80];
        HRam = new int[0x80];
        joyPadState = 0xff; //no keys pressed
        memBank = null;
        gbcMode = false; //defaults to false
        bankNum = 0;

        //color registers
        bgColorIndex = 0;
        bgPalettes = new int[0x40];
        autoInc = false;

        //sprite palette registers
        spriteIndex = 0;
        spritePalettes = new int[0x40];
        autoSpriteInc = false;

        hBlankDMAInProgress = false;
        dmaTransferLength = 0;
        dmaSrc = 0;
        dmaDst = 0;

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
     * TODO: SAVE GBC MODE
     *
     * @return current state of memory (in a long byte array)
     */
    public byte[] saveState() {
        byte[] save = new byte[BYTE_SAVE_LENGTH];
        MemCopyUtil.copyArray(vRam, 0, save, 0, 0x2000);
        MemCopyUtil.copyArray(wRam, 0, save, 0x2000, 0x2000);
        MemCopyUtil.copyArray(OAMTable, 0, save, 0x4000, 0xa0);
        MemCopyUtil.copyArray(IOPorts, 0, save, 0x40a0, 0x80);
        MemCopyUtil.copyArray(HRam, 0, save, 0x4120, 0x80);
        System.arraycopy(memBank.saveState(), 0, save, 0x41a0, RAM_SAVE_LEN);
        save[BYTE_SAVE_LENGTH - 1] = (byte)(joyPadState & 0xff);
        return save;
    }


    /**
     * Load the current memory state from the byte array generated in saveMem
     *
     */
    public void loadState(byte[] save) {
        MemCopyUtil.copyArray(save, 0, vRam, 0, 0x2000);
        MemCopyUtil.copyArray(save, 0x2000, wRam, 0, 0x2000);
        MemCopyUtil.copyArray(save, 0x4000, OAMTable, 0, 0xa0);
        MemCopyUtil.copyArray(save, 0x40a0, IOPorts, 0, 0x80);
        MemCopyUtil.copyArray(save, 0x4120, HRam, 0, 0x80);
        byte[] ram = new byte[RAM_SAVE_LEN];
        System.arraycopy(save, 0x41a0, ram, 0, RAM_SAVE_LEN);
        memBank.loadState(ram);
        joyPadState = Byte.toUnsignedInt(save[BYTE_SAVE_LENGTH - 1]);
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

            gbcMode = (cartridge[0x143] != 0x0);
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
            if (bankNum == 0)
                return vRam[address - 0x8000];
            else
                return vRam1[address - 0x8000];
        } else if (address < 0xc000) {
            return memBank.readByte(address);
        } else if (address < 0xe000) {
            if (gbcMode) {
                if (address < 0xd000) {
                    return wRam[address - 0xc000];
                } else {
                    int index = (address - 0xd000) + (0x1000 * (wRamIndex - 1));
                    return wRamBanks[index];
                }

            } else {
                return wRam[address - 0xc000];
            }
        } else if (address < 0xfe00) {
            return wRam[address - 0xe000];
        } else if (address < 0xfea0) {
            return OAMTable[address - 0xfe00];
        } else if (address < 0xff00) {
            System.err.println("Tried to read from invalid address: " + Integer.toHexString(address));
            return -1; // can't use this area
        } else if (address < 0xff80){
            if (address == 0xff69) {
                return bgPalettes[bgColorIndex];
            } else if (address == 0xff6b) {
                return spritePalettes[spriteIndex];
            } else {
                return IOPorts[address - 0xff00];
            }
        } else if (address < 0x10000){
            return HRam[address - 0xff80];
        } else {
            System.err.println("Reading from invalid address: " + Integer.toHexString(address));
            return -1; //oops something went wrong
        }

    }

    public int readVram(int address, int bank) {
        bank &= 1;
        if (address >= 0x8000 && address < 0xc000) {
            if (bank == 1) {
                return vRam1[address - 0x8000];
            } else {
                return vRam[address - 0x8000];
            }
        }
        else {
            System.err.println("invalid vram1 read");
        }
        return -1;
    }


    public int readVram1(int address) {
        if (address >= 0x8000 && address < 0xc000)
            return vRam1[address - 0x8000];
        else
            System.err.println("invalid vram1 read");
            System.exit(1); //TODO  get rid of
        return -1;
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
            if (bankNum == 0)
                vRam[address - 0x8000] = data;
            else
                vRam1[address - 0x8000] = data;
        } else if (address < 0xc000) {
            memBank.writeByte(address, data);
        } else if (address < 0xe000) {
            if (gbcMode) {
                if (address < 0xd000) {
                    wRam[address - 0xc000] = data;
                } else {
                    int index = (address - 0xd000) + (0x1000 * (wRamIndex - 1));
                    wRamBanks[index] = data;
                }
            } else {
                wRam[address - 0xc000] = data;
            }
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
        data &= 0xff;

        if (address == 0xff04) {
            IOPorts[newAddress] = 0; //reset DIV register
        } else if (address == 0xff44) {
            IOPorts[newAddress] = 0; //reset LCDC y-Coordinate
        } else if (address == 0xff46) {
            DMATransfer(data);
        } else if (address == 0xff40) {
            IOPorts[newAddress] = data;
        } else if (address == 0xff4d) {
            System.err.println("GBC DOUBLE SPEED MODE");
        } else if (address == 0xff4f) {
            IOPorts[newAddress] = data & 0x1;
            bankNum = data & 0x1;
        } else if (address == 0xff68) {
            // color 'register'
            autoInc = (data & 0x80) != 0x0;
            bgColorIndex = data & 0x3f;
            IOPorts[newAddress] = data;
        } else if (address == 0xff69) {
            bgPalettes[bgColorIndex] = data;
            bgColorIndex += autoInc ? 1 : 0;
            bgColorIndex &= 0x3f;
        } else if (address == 0xff6a) {
            autoSpriteInc = (data & 0x80) != 0x0;
            spriteIndex = data & 0x3f;
            IOPorts[newAddress] = data;
        } else if (address == 0xff6b) {
            spritePalettes[spriteIndex] = data;
            spriteIndex += autoSpriteInc ? 1 : 0;
        } else if (address == 0xff55) {
            //IOPorts[newAddress] = data;
            gbcDMATransfer(data);
        } else if (address == 0xff70) {
            wRamIndex = data & 0x7;
            //writing 0 means writing 1
            wRamIndex = wRamIndex == 0 ? 1 : wRamIndex;
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
     *
     * TODO: CHECK TRANSFER RANGES
     * @param initial
     */
    private void gbcDMATransfer(int initial) {
        System.err.println("GBC DMA TRANSFER");
        boolean generalPurpose = !isSet(initial, 7);
        dmaTransferLength = initial & 0x7f;
        int src = IOPorts[0x51] << 8 | IOPorts[0x52];
        src &= 0xfff0; //ignore lower nibble
        int dst = IOPorts[0x53] << 8 | IOPorts[0x54];
        dst |= 0x8000;
        dst &= 0x1ff0; //ignore top 3 bits, lower nibble

        //first check to see if stopping DMA transfer in progress
        if (hBlankDMAInProgress) {
            if (!isSet(initial, 7)) {
                IOPorts[0x55] = 0xff;
                hBlankDMAInProgress = false;
            }
        }

        if (generalPurpose) {
            //transfer data
            System.err.println("General purpose gbcdma");
            for (int i = 0; i < dmaTransferLength; ++i) {
                this.writeByte(dst + i, this.readByte(src + i));
            }
            IOPorts[0x55] = 0xff;
        } else {
            System.err.println("HbLANK DMA ");
            hBlankDMAInProgress = true;
            dmaSrc = src;
            dmaDst = dst;
            IOPorts[0x55] = dmaTransferLength;
        }
    }
    
    /**
     * sets the scanline
     * @param num new scanline value
     */ 
    public void setScanLine(int num) {
        IOPorts[0x44] = num;
        checkDMA();
    }

    private void checkDMA() {
        if (hBlankDMAInProgress && IOPorts[0x44] < 144) {
            System.err.println("doing a dma transfer scanline:" + IOPorts[0x44]);
            for (int i = 0; i < 0x10; ++i) {
                writeByte(dmaDst + i, readByte(dmaSrc + i));
            }
            dmaDst += 0x10;
            dmaSrc += 0x10;
            dmaTransferLength -= 0x1;
            IOPorts[0x55] = dmaTransferLength;
            //Done with the transfer
            if (dmaTransferLength <= 0) {
                hBlankDMAInProgress = false;
                IOPorts[0x55] = 0xff;
            }
        }
    }
    
    /**
     * increments the scanLine at 0xff44
     * TODO add DMA transfer for GBC
     * Should be in H-Blank when called
     */ 
    public void incScanLine() {
        IOPorts[0x44]++;
        checkDMA();
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

    //TODO Check if actually correct
    public int getGBCBGPaletteColor(int pal, int num) {
        if (pal < 0 || pal > 7) {
            System.err.println(pal);
            System.err.println("invalid bg num");
        }
        int color = 0;
        int index = (pal * 8) + (num * 2);
        int palette = bgPalettes[index] | bgPalettes[index + 1] << 8;
        int r = palette & 0x1f;
        int g = (palette & 0x3e0) >> 5;
        int b = (palette & 0x7c00) >> 10;
        color = ((r * 13 + g * 2 + b) >> 1);
        color |= ((g * 3 + b) << 1) << 8;
        color |= ((r * 3 + g * 2 + b * 11) >> 1) << 16;

        return color;
    }

    public int getGBCSpritePaletteColor(int pal, int num) {
        if (pal < 0 || pal > 7) {
            System.err.println("invalid palette num");
        }
        int color = 0;
        int palette = spritePalettes[(pal * 8) + (num * 2)] | spritePalettes[(pal * 8) + (num * 2) + 1] << 8;
        int r = palette & 0x1f;
        int g = (palette & 0x3e0) >> 5;
        int b = (palette & 0x7c00) >> 10;
        color = ((r * 13 + g * 2 + b) >> 1);
        color |= ((g * 3 + b) << 1) << 8;
        color |= ((r * 3 + g * 2 + b * 11) >> 1) << 16;

        return color;
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

    public boolean isGBCRom() {
        return gbcMode;
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
