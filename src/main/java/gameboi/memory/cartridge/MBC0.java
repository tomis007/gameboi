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
package main.java.gameboi.memory.cartridge;

/**
 *
 * implementation of NO
 * memory bank controller hardware
 * on the cartridge
 *
 * @author tomis007
 */
public class MBC0 implements MemoryBank {
    /**
     *
     * 0x0 - 0x7fff
     */
    private int[] rom;

    /**
     *
     * 0xa000 - 0xbfff
     */
    private int[] extRam;

    /**
     * intialize the MBCO from romCartridge
     *
     * copies the values to ROM and extRAM
     * @param romCartridge int array of the rom
     */
    public MBC0(int[] romCartridge) {
        rom = new int[0x8000];
        extRam = new int[0x2000];

        for (int i = 0; i < romCartridge.length && i < 0x8000; ++i) {
            rom[i] = romCartridge[i];
        }

        for (int i = 0xa000; i < romCartridge.length && i < 0xc000; ++i) {
            extRam[i - 0xa000] = romCartridge[i];
        }

    }

    /**
     * read a byte from MBC0
     *
     * only valid ranges are ROM 0x0 - 0x7fff
     * and extRam 0xa000 - 0xbfff
     *
     *
     * @param address to read from
     * @return data read
     */
    public int readByte(int address) {
        if (address < 0x8000 && address >= 0) {
            return rom[address];
        } else if (address < 0xc000 && address >= 0xa000) {
            return extRam[address];
        } else {
            System.err.println("invalid read from MBC0");
            return 0;
        }
    }

    /**
     * write a Byte to MBC0
     *
     * only writes a byte to extRam
     * (0xa000 - 0xbfff)
     *
     * @param address to write to
     * @param data to write
     */
    public void writeByte(int address, int data) {
        data &= 0xff;
        if (address >= 0xa000 && address < 0xc000) {
            extRam[address - 0xa000] = data & 0xff;
        }
    }

}
