package gameboi;

/**
 *
 * MBC1 cartridge chip for membanks
 *
 *
 *
 * tomis007
 */
public class MBC1 implements MemoryBank {

    private int[] romBanks;
    private int currentRomBank;
    private int currentRamBank;
    private int[] ramBanks;
    private boolean ramEnabled;
    private mode currentMode;


    private static final int ROM_BANK_SIZE = 0x4000;
    private static final int RAM_BANK_SIZE = 0x2000;
    private static final mode ROM = mode.ROM;
    private static final mode RAM = mode.RAM;


    private enum mode {ROM, RAM};


    public MBC1(int[] cartridge) {
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
        currentMode = ROM;
    }

    /**
     *
     *
     *
     *
     * @param address to read from
     * @return
     */
    public int readByte(int address) {
        //bank 0 always here
        if (address < 0x4000) {
            return romBanks[address];
        } else if (address < 0x8000){
            address -= 0x4000;
            if (currentMode == ROM) {
                return romBanks[address + (currentRomBank * ROM_BANK_SIZE)];
            } else {
                return romBanks[address + ((currentRomBank & 0x1f) * ROM_BANK_SIZE)];
            }
        } else if ((address >= 0xa000 && address < 0xc000) && ramEnabled) {
            if (currentMode == ROM) {
                return ramBanks[address - 0xa000];
            } else {
                return ramBanks[(address - 0xa000) + (currentRamBank * RAM_BANK_SIZE)];
            }
        } else {
            System.err.println("invalid read from MBC1: 0x" + Integer.toHexString(address));
            return -1;
        }
    }

    /**
     * controls the MBC1 registers
     *
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
                ramBanks[(address - 0xa000) + (currentRamBank * RAM_BANK_SIZE)] = data & 0xff;
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
            currentRomBank = (currentRomBank & 0x60) + data;
            currentRomBank += (currentRomBank == 0) ? 1 : 0;
        } else if (address < 0x6000) {
            data &= 0x3;
            currentRomBank &= ((data << 5) | 0x1f);
            currentRamBank = data;
        } else if (address < 0x8000){
            currentMode = ((data & 0x1) == 1) ? RAM : ROM;
        } else {
            System.err.println("invalid write to MBC Registers");
        }
    }
}
