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

import main.java.gameboi.memory.MemCopyUtil;

/**
 *
 * Implementation of MBC1 cartridge chip
 *
 * tomis007
 */
public class MBC1 implements MemoryBank {

    private int[] romBanks;
    private int currentROmBank;
    private int currentRAmBank;
    private int[] ramBanks;
    private boolean ramEnabled;
    private mode currentMode;
    private static final int STATE_LEN = 4;


    private static final int ROM_BANK_SIZE = 0x4000;
    private static final int RAM_BANK_SIZE = 0x2000;
    private static final mode ROM = mode.ROM;
    private static final mode RAM = mode.RAM;


    private enum mode {ROM, RAM}


    /**
     * MBC1 Constructor
     * MBC1 Memory controller
     * @param cartridge ROM to initialize from
     */
    public MBC1(int[] cartridge) {
        romBanks = new int[cartridge.length];
        System.arraycopy(cartridge, 0, romBanks, 0, cartridge.length);
        ramBanks = initRamBank(cartridge[0x149]);
        currentROmBank = 1;
        currentRAmBank = 0;
        currentMode = ROM;
    }


    /**
     * create a RamBank of the correct size based on
     * rom's value at 0x149
     *
     * @param bankInfo cartridge byte at 0x149
     * @return int[] of appropriate rambank size
     */
    private int[] initRamBank(int bankInfo) {
        int[] bank;
        switch(bankInfo) {
            case 0:
                bank = null;
                break;
            case 1:
                bank = new int[0x800];
                break;
            case 2:
                bank = new int[0x2000];
                break;
            case 3:
                bank = new int[0x8000];
                break;
            default:
                System.err.println("invalid ram bank size");
                System.err.println("MBC1 INIT: will probably crash soon");
                bank = new int[0x800];
        }
        return bank;
    }


    /**
     * Read a byte from MBC1
     *
     * @param address to read from
     * @return byte at address
     */
    public int readByte(int address) {
        //bank 0 always here
        if (address < 0x4000) {
            return romBanks[address];
        } else if (address < 0x8000){
            address -= 0x4000;
            if (currentMode == ROM) {
                return romBanks[address + (currentROmBank * ROM_BANK_SIZE)];
            } else {
                return romBanks[address + ((currentROmBank & 0x1f) * ROM_BANK_SIZE)];
            }
        } else if ((address >= 0xa000 && address < 0xc000) && ramEnabled) {
            if (currentMode == ROM) {
                return ramBanks[address - 0xa000];
            } else {
                return ramBanks[(address - 0xa000) + (currentRAmBank * RAM_BANK_SIZE)];
            }
        } else {
            System.err.println("invalid read from MBC1: 0x" + Integer.toHexString(address));
            return -1;
        }
    }

    /**
     * controls the MBC1 registers
     * or writes to RAM BANK
     *
     * @param address to write to
     * @param data to write
     */
    public void writeByte(int address, int data) {
        data &= 0xff;
        if (address < 0x8000) {
            updateMBCRegisters(address, data);
        } else if (ramEnabled) {
            if (currentMode == ROM) {
                ramBanks[address - 0xa000] = data & 0xff;
            } else {
                ramBanks[(address - 0xa000) + (currentRAmBank * RAM_BANK_SIZE)] = data & 0xff;
            }
        }
    }


    /**
     * Updates the MBC Control Registers as
     * activated when attempting to write to
     * ROM
     *
     * @param address to write to
     * @param data to write
     */
    private void updateMBCRegisters(int address, int data) {
        data &= 0xff;

        if (address < 0x2000) {
            ramEnabled = ((data & 0xf) == 0xa);
        } else if (address < 0x4000) {
            data &= 0x1f;
            currentROmBank = (currentROmBank & 0x60) + data;
            currentROmBank += (currentROmBank == 0) ? 1 : 0;
        } else if (address < 0x6000) {
            data &= 0x3;
            currentROmBank &= ((data << 5) | 0x1f);
            currentRAmBank = data;
        } else if (address < 0x8000){
            currentMode = ((data & 0x1) == 1) ? RAM : ROM;
        } else {
            System.err.println("invalid write to MBC Registers");
        }
    }

    /**
     *
     *
     * @param buf to load from
     */
    public void loadState(byte[] buf) {
        ramBanks = initRamBank(romBanks[0x149]);
        MemCopyUtil.copyArray(buf, 0, ramBanks, 0, ramBanks.length);
        currentRAmBank = Byte.toUnsignedInt(buf[ramBanks.length]);
        currentROmBank = Byte.toUnsignedInt(buf[ramBanks.length + 1]);
        ramEnabled = Byte.toUnsignedInt(buf[ramBanks.length + 2]) == 1;
        currentMode = Byte.toUnsignedInt(buf[ramBanks.length + 3]) == 1 ? ROM : RAM;
    }

    /**
     * Save the state for MBC1
     *
     * @return state saved in byte[] array
     */
    public byte[] saveState() {
        byte[] state = new byte[ramBanks.length + STATE_LEN];
        MemCopyUtil.copyArray(ramBanks, 0, state, 0, ramBanks.length);
        state[ramBanks.length] = (byte)(currentRAmBank & 0xff);
        state[ramBanks.length + 1] = (byte)(currentROmBank & 0xff);
        state[ramBanks.length + 2] = (byte)(ramEnabled ? 1 : 0);
        state[ramBanks.length + 3] = (byte)(currentMode == ROM ? 1 : 0);
        return state;
    }
}
