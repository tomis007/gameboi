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

import java.time.LocalDateTime;

/**
 *
 * Implementation of MBC3 hardware in cartridge
 *
 * TODO: real time clock support
 * tomis007
 */
public class MBC3 implements MemoryBank {

    private int[] romBanks;
    private int currentRomBank;
    private int currentRamBank;
    private int[] ramBanks;
    private boolean ramEnabled;

    //rtc clock
    private boolean rtcEnabled;
    private boolean latchOnOne;
    private int mappedRTCReg;

    private int rtc_S;
    private int rtc_M;
    private int rtc_H;
    private int rtc_DL;
    private int rtc_DH;

    private static final int ROM_BANK_SIZE = 0x4000;
    private static final int RAM_BANK_SIZE = 0x2000;


    //TODO RTC CLOCK!!!
    public MBC3(int[] cartridge) {
        romBanks = new int[cartridge.length];
        System.arraycopy(cartridge, 0, romBanks, 0, cartridge.length);

        switch (cartridge[0x149]) {
            case 0:
                ramBanks = null;
                break;
            case 1:
                ramBanks = new int[0x800];
                break;
            case 2:
                ramBanks = new int[0x2000];
                break;
            case 3:
                ramBanks = new int [0x8000];
                break;
            default:
                System.err.println("invalid ram bank size");
                System.exit(1);
        }

        currentRomBank = 1;
        currentRamBank = 0;
        rtcEnabled = false;
        latchOnOne = false;
    }

    /**
     * Read a Byte from MBC3
     *
     *
     * @param address to read from
     * @return byte at the address
     */
    public int readByte(int address) {
        //bank 0 always here
        if (address < 0x4000) {
            return romBanks[address];
        } else if (address < 0x8000) {
            address -= 0x4000;
            return romBanks[address + (currentRomBank * ROM_BANK_SIZE)];
        } else if (address >= 0xa000 && address < 0xc000) {
            if (rtcEnabled && address == 0xa000) {
                return mappedRTCReg;
            } else if (ramEnabled) {
                return ramBanks[(address - 0xa000) + (currentRamBank * RAM_BANK_SIZE)];
            }
        } else {
            System.err.println("invalid read from MBC1: 0x" + Integer.toHexString(address));
            return -1;
        }
        return -1;
    }

    /**
     * controls the MBC1 registers or writes to RAM
     *
     * @param address to write to
     * @param data to write
     */
    public void writeByte(int address, int data) {
        data &= 0xff;

        if (address < 0x8000) {
            updateMBCRegisters(address, data);
        } else if (ramEnabled) {
            ramBanks[(address - 0xa000) + (currentRamBank * RAM_BANK_SIZE)] = data & 0xff;
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
            ramEnabled = rtcEnabled = ((data & 0xf) == 0xa);
        } else if (address < 0x4000) {
            data &= 0x7f;
            currentRomBank = (data == 0) ? 1 : data;
        } else if (address < 0x6000) {
            if (data >= 0 && data <= 3) {
                currentRamBank = data;
            } else if (address >= 0x8 && address <= 0xc) {
//                writeRTCData(data);
            }
        } else if (address < 0x8000){
//            latchClock(data);
        } else {
            System.err.println("invalid write to MBC Registers");
        }
    }

    private void writeRTCData(int data){
        LocalDateTime date = LocalDateTime.now();

        switch(data) {
            case 0x8:
                mappedRTCReg = date.getSecond();
                break;
            case 0x9:
                mappedRTCReg = date.getMinute();
                break;
            case 0xa:
                mappedRTCReg = rtc_H;
                break;
            case 0xb:
                mappedRTCReg = rtc_DL;
                break;
            case 0xc:
                mappedRTCReg = rtc_DH;
                break;
            default:
                break;
        }
    }



    private void latchClock(int data) {
        if (data == 0x0) {
            latchOnOne = true;
            return;
        }
        if (data == 0x1 && latchOnOne) {
            LocalDateTime date = LocalDateTime.now();
            rtc_DL = date.getDayOfYear() & 0xff;
            rtc_H = date.getHour();
            rtc_M = date.getMinute();
            rtc_S = date.getSecond();
        }


        latchOnOne = false;
    }
}
