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
 * TODO: ADD MBC2/MBC4/MBC5
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

}
