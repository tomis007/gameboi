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
package gameboi;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Listens to the keyboard presses and simulates the joyPad
 * 
 * Stores present state of the 8 directional keys in memory at 0xff00
 * if bit n is 0 - key n is pressed, if it's 1 - the key is not pressed
 * 
 * NOTE: poor abstraction here, memory also depends on knowing these key
 * mappings
 * 
 * @author tomis007
 */
public class gameboyKeyListener implements KeyListener {
    private final CPU cpu;
    private final GBMem memory;

    private static final char START = 'p';
    private static final char SELECT = 'l';
    private static final char A = 'n';
    private static final char B = 'm';
    private static final char LEFT = 'a';
    private static final char RIGHT = 'd';
    private static final char UP = 'w';
    private static final char DOWN = 's';
    
    
    gameboyKeyListener(GBMem memory, CPU cpu) {
        this.memory = memory;
        this.cpu = cpu;
    }
    
    
    /**
     * do nothing
     * @param e Keyevent
     */ 
    @Override
    public void keyTyped(KeyEvent e) {
        //dont do anything
    }
    
    /**
     * called whenever a key is typed, if maps to joypad controls
     * the memory at 0xff00 is updated 
     * 
     * @param e keyevent
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int currentJoyPad = memory.getJoyPadState();
        int joypadRequests = memory.readByte(0xff00);
        
        
        int key_num = getKeyNum(e);
        // not mapped to a joypad key
        if (key_num == -1) {
            return;
        }
        
        // 'press key'
        currentJoyPad = setBit(0, key_num, currentJoyPad);
        boolean directionPad = key_num < 4;
        
        if (directionPad && !isSet(joypadRequests, 4)) {
            cpu.requestInterrupt(4);
        } else if (!isSet(joypadRequests, 5)) {
            cpu.requestInterrupt(4);
        }
//        System.out.println(Integer.toBinaryString(currentJoyPad));
        memory.updateJoyPadState(currentJoyPad);
    }

    /**
     * Called whenever a key is released, if matches a joypad
     * key the memory at 0xff00 is updated
     * 
     * @param e Keyevent
     */ 
    @Override
    public void keyReleased(KeyEvent e) {
        int currentJoyPad = memory.getJoyPadState();
        int key_num = getKeyNum(e);
        if (key_num != -1) {
            currentJoyPad = setBit(1, key_num, currentJoyPad);
            memory.updateJoyPadState(currentJoyPad);
        }
    }
    
    /**
     * isSet
     * 
     * Tests the num if the bitNum bit is set
     * 
     * @param num number to test
     * @param bitNum bitnumber to test
     */ 
    private boolean isSet(int num, int bitNum) {
        return (((num >> bitNum) & 0x1) == 1);
    }

    
    /**
     * returns the bit in joypadstate for the keypressed
     * 
     *
     * KEY 7 - START
     * KEY 6 - SELECT
     * KEY 5 - B
     * KEY 4 - A
     * KEY 3 - DOWN
     * KEY 2 - UP
     * KEY 1 - LEFT
     * KEY 0 - RIGHT
     * 
     */ 
    private int getKeyNum(KeyEvent e) {
        char key = e.getKeyChar();
        
        switch(key) {
            case START:   return 7;
            case SELECT:  return 6;
            case B:       return 5;
            case A:       return 4;
            case DOWN:    return 3;
            case UP:      return 2;
            case LEFT:    return 1;
            case RIGHT:   return 0;
            default:      return -1;
        }
    }
    
    /**
     * sets bit bitNum to val in num
     */ 
    private int setBit(int val, int bitNum, int num) {
        if (val == 1) {
            return num | 1 << bitNum;
        } else {
            return num & ~(1 << bitNum);
        }
    }    
    
    
}
