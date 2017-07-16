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
 * interface for cartridge memory bank
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

    /**
     *  save the memory bank state
     *
     *
     * @return to save to
     */
    byte[] saveState();

    /**
     *  load the memory bank state
     *
     *
     * @param buf to load from
     */
    void loadState(byte[] buf);



}
