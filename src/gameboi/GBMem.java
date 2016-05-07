/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameboi;

/**
 *
 * @author thomas
 */
public class GBMem {
    private int memory[];
    private byte cartridge[];
    
    
    
    public GBMem() {
        memory = new int[0x200000];
        cartridge = new byte[0x400];
    }
    
    public int readByte(int address) {
        return 0;
    }
    
    public void writeByte(int address, int data) {

    }
    
    public int readWord(int address) {
        return 0;
    }
    
    public void writeWord(int address, int data) {
        
    }
    
}
