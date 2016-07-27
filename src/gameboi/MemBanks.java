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
 * TODO: UPDATE RAM BANKING AND OTHER MEMBANK TYPES
 * 
 * @author tomis007
 */
public class MemBanks {
    private MemoryBank memBank;
    
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
            case 0:
                memBank = new MBC0(romCartridge);
                break;
            default:
                System.err.println("Sorry, this MBC is not implemented yet");
                System.exit(1);
        }
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
        return memBank.readByte(address);
    }
    
    /**
     * Returns whether or not ram bank writing is enabled
     *
     * TODO
     * @return true if ram bank writing enabled, false if not
     */ 
    public boolean isRamEnabled() {
        return true;
    }


    /**
     *
     *
     *
     * @param address
     * @param data
     */
    public void writeByte(int address, int data) {
        memBank.writeByte(address, data);
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
/*        if (address < 0x2000) {
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
        }*/
    }
}
