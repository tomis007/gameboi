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

import main.java.gameboi.memory.cartridge.MBC0;
import main.java.gameboi.memory.cartridge.MBC1;
import main.java.gameboi.memory.cartridge.MBC3;
import main.java.gameboi.memory.cartridge.MemoryBank;

/**
 * External ROM/RAM banks for gameboy memory
 * 
 * Simulates the external ROM bank attached in the original gameboy cartridges
 * 
 * TODO: ADD MBC2/MBC4/MBC5
 * 
 * @author tomis007
 */
public class MemBanks {
    private MemoryBank memBank;
    //largest of supported membanks, and additional info
    private static final int BYTE_SAVE_SIZE = 0x8010;
    
    /**
     * Constructor for RomMemBank
     * 
     * Constructs the gameboy rom bank as specified by the value in the
     *    gameboy program.
     * 
     * @param romCartridge (required) the rom cartridge to create the rom 
     *     bank from
     * 
     */ 
    public MemBanks(int[] romCartridge) {
        switch(romCartridge[0x147]) {
            case 0x0:
                memBank = new MBC0(romCartridge);
                break;
            case 0x1:
                memBank = new MBC1(romCartridge);
                break;
            case 0x2:
                memBank = new MBC1(romCartridge);
                break;
            case 0x3:
                memBank = new MBC1(romCartridge);
                break;
            case 0xf:
                memBank = new MBC3(romCartridge);
                break;
            case 0x10:
                memBank = new MBC3(romCartridge);
                break;
            case 0x11:
                memBank = new MBC3(romCartridge);
                break;
            case 0x12:
                memBank = new MBC3(romCartridge);
                break;
            case 0x13:
                memBank = new MBC3(romCartridge);
                break;
            default:
                System.err.println("Sorry, this MBC is not implemented yet: " + romCartridge[0x147]);
                System.exit(1);
        }
    }
    
    /**
     * read a Byte from the RomBank
     * 
     * <p> The address to be read must be between located in ROM 0 -
     *     0x7FFF, or an external Ram address
     *
     * @param address (required) address of byte to read
     * @return int that is the 'byte' at the address
     */
    public int readByte(int address) {
        return memBank.readByte(address);
    }

    /**
     * write a byte to external ram or to
     * a MBC controller
     *
     *
     * @param address to write to (ext RAM or ROM)
     * @param data to write
     */
    public void writeByte(int address, int data) {
        memBank.writeByte(address, data);
    }

    public byte[] saveState() {
        byte[] buf = new byte[BYTE_SAVE_SIZE];
        byte[] save = memBank.saveState();
        System.arraycopy(save, 0, buf, 0, save.length);
        return buf;
    }

    public void loadState(byte[] buf) {
        memBank.loadState(buf);
    }

    /**
     *
     * Get the size of this objects save state buffer
     *
     * @return int size of byte save buffer
     */
    public static int getByteSaveSize() {
        return BYTE_SAVE_SIZE;
    }

}
