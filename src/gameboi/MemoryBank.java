package gameboi;

/**
 * tomis007
 */
public interface MemoryBank {

    /**
     *
     * read a byte from memory bank
     *
     * @param address to read from
     * @return byte read from memory
     */
    int readByte(int address);


    /**
     * write a byte to memory bank
     * NOTE: memory bank specific behavior
     * writing bytes controls the state
     * of the bank
     *
     * @param address to write to
     * @param data to write
     */
    void writeByte(int address, int data);



}
