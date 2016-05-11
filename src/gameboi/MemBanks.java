/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameboi;

/**
 * External ROM/RAM banks for gameboy memory
 * 
 * Simulates the external ROM bank attached in the original gameboy cartridges
 * 
 * NOTE: ONLY MBC1 is "implemented"
 * TODO: UPDATE RAM BANKING AND OTHER MEMBANK TYPES
 * 
 * @author tomis007
 */
public class MemBanks {
    private final int[] cartridge;
    private int[] ramBank;
    private int curRAMBank;
    private int curROMBank;
    private boolean isRomBanking;
    private boolean ramEnabled;
    /**
     * Enum type for different RomBanks
     * 
     */
    private enum Mode {NONE, MBC1, MBC2, MBC3};
    private final Mode bankMode;
    
    /**
     * Constructor for RomMemBank
     * 
     *<p> Constructs the gameboy rom bank as specified by the value in the 
     *    gameboy program.
     * 
     * @param romCartridge (required) the rom cartridge to create the rom 
     *     bank from
     * 
     */ 
    public MemBanks(int[] romCartridge) {
        ramBank = new int[0x8000];
        cartridge = new int[romCartridge.length];
        System.arraycopy(romCartridge, 0, cartridge, 0, romCartridge.length);
        
        switch (romCartridge[0x147]) {
            case 0: bankMode = Mode.NONE;
                    break;
            case 1: bankMode = Mode.MBC1;
                    break;
            case 2: bankMode = Mode.MBC1;
                    break;
            case 3: bankMode = Mode.MBC1;
                    break;
            case 13:
                    bankMode = Mode.MBC1; // NOT CORRECT!!! JUST FOR TESTING
                    break;
            default: bankMode = Mode.NONE;
                    break;
        }
        curROMBank = 1;
        curRAMBank = 0;
        isRomBanking = true; //default value
        ramEnabled = false;
    }
    
    /**
     * read a Byte from the RomBank
     * 
     * <p> The address to be read must be between (inclusive) 0x4000 and 
     *     0x7FFF. This is reading from the switchable ROM Bank in the
     *     gameboy's memory.
     * 
     * @param address (required) address of byte to read
     * @return int that is the 'byte' at the address. If invalid address, 
     *     -1 is returned
     */ 
    public int readByte(int address) {
        if ((address >= 0x4000) && (address <= 0x7fff)) {
            return cartridge[(address - 0x4000) + (curROMBank * 0x4000)];
        } else if ((address >= 0xa000) && (address <= 0xbfff)) {
            return ramBank[(address - 0xa000) + (curRAMBank * 0x2000)];
        } else {
            System.err.println("Invalid read address from MemBanks");
            return -1;
        }
    }
    
    /**
     * Returns whether or not ram bank writing is enabled
     * 
     * @return true if ram bank writing enabled, false if not
     */ 
    public boolean isRamEnabled() {
        return ramEnabled;
    }
    
    
    /**
     * updates the Memory Banks
     * 
     * <p> Bank updates are triggered when the gameboy attempts to write
     *     to the ROM addresses (0x0-0x7fff) in the gameboy memory.
     * 
     * @param address int address the write to
     * @param data int data to write to
     */ 
    public void updateBanking(int address, int data) {
        if (address < 0x2000) {
            if (bankMode == Mode.MBC1) {
                int mode = data & 0xf;
                if (mode == 0xa) {
                    ramEnabled = true;
                }
                if (mode == 0x0) {
                    ramEnabled = false;
                }       
            }
        } else if ((address >= 0x2000) && (address < 0x4000)) {
            if (bankMode == Mode.MBC1) {
                curROMBank &= 0xe0;
                curROMBank |= (data & 0x1f);
                curROMBank += (curROMBank == 0) ? 1 : 0;
            } //nelse if (bankMode == Mode.MBC2) {
              //  curROMBank = data & 0xf;
//            }
        } else if ((address >= 0x4000) && (address < 0x6000)) {
            if (bankMode == Mode.MBC1) {
                if (isRomBanking) {
                    curROMBank &= 0x1f;
                    curROMBank |= data & 0xe0;
                } else {
                    curRAMBank = data & 0x3;
                }
            }
        } else if ((address >= 0x6000) && (address < 0x8000)) {
            if (bankMode == Mode.MBC1) {
                int nextMode = 0x1 & data;
                isRomBanking = (nextMode == 0);
                if (isRomBanking) {
                    curRAMBank = 0;
                }
            }
        }
    }
    
    
    /**
     * writes byte to switchable ram bank
     * 
     * @param address (required) address to write to
     * @param data (required) data to write
     */ 
    public void writeByte(int address, int data) {
        data = data & 0x0ff;
        if (ramEnabled) {
            ramBank[(address - 0xa000) + (curRAMBank * 0x2000)] = data;
        }
    }
}
