package main.java.gameboi.joypad;

import main.java.gameboi.cpu.CPU;
import main.java.gameboi.memory.GBMem;


/**
 * JoyPad class for handling input to the GameBoi
 */
public class JoyPad {
    private final CPU z80;
    private final GBMem mem;


    public JoyPad(CPU cpu, GBMem memory) {
        z80 = cpu;
        mem = memory;
    }

    /**
     * signals to the gameboi that an
     * input key has been pressed
     *
     *       START:   return 7;
     *       SELECT:  return 6;
     *       B:       return 5;
     *       A:       return 4;
     *       DOWN:    return 3;
     *       UP:      return 2;
     *       LEFT:    return 1;
     *       RIGHT:   return 0;
     *
     * @param key_num of joypad key pressed as mapped
     *                above
     */
    public void keyPressed(int key_num) {
        int currentJoyPad = mem.getJoyPadState();

        // not mapped to a joypad key
        if (key_num < 0 || key_num > 7) {
            return;
        }

        // 'press key'
        currentJoyPad = setBit(0, key_num, currentJoyPad);
        mem.updateJoyPadState(currentJoyPad);
        z80.requestInterrupt(4);
        mem.updateJoyPadState(currentJoyPad);
    }

    /**
     *
     * signals to the gameboi that an
     * input key has been released
     *
     *       START:   return 7;
     *       SELECT:  return 6;
     *       B:       return 5;
     *       A:       return 4;
     *       DOWN:    return 3;
     *       UP:      return 2;
     *       LEFT:    return 1;
     *       RIGHT:   return 0;
     *
     * @param key_num of joypad key released as mapped
     *                above
     */
    public void keyReleased(int key_num) {
        int currentJoyPad = mem.getJoyPadState();

        if (key_num >= 0 && key_num < 8) {
            currentJoyPad = setBit(1, key_num, currentJoyPad);
            mem.updateJoyPadState(currentJoyPad);
            z80.requestInterrupt(4);
        }
        mem.updateJoyPadState(currentJoyPad);
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
