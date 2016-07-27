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

    private int[] romBank0;



    public int readByte(int address) {
        return 0;
    }


    public void writeByte(int address, int data) {

    }

}
