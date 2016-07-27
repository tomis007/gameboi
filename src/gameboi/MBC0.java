package gameboi;

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
        if (address >= 0xa000 && address < 0xc000) {
            extRam[address - 0xa000] = data & 0xff;
        }
    }

}
