/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameboi;

/**
 * Represents the GBRegisters, allows easy access/modifications.
 * 
 * <p>
 * get/set for each of the following A,B,C,D,E,F,H,L,AF,BC,DE,HL
 * 
 * @author tomis007
 */
public class GBRegisters {
    private int a;
    private int b;
    private int c;
    private int d;
    private int e;
    private int f;
    private int h;
    private int l;
    
    /**
     * Initializes the registers to their values as specified in 
     * the original gameboy documentation.
     * 
     */ 
    public GBRegisters() {
        a = 0x1;
        f = 0xb0;
        b = 0x00;
        c = 0x13;
        d = 0x0;
        e = 0xd8;
        h = 0x1;
        l = 0x4d;
    }
    
    /**
     * @return value in register A
     */ 
    public int getA() {
        return a;
    }
    
    /**
     * Set value for register A.
     * 
     * @param num (required) number to set regA to (masked to 8 bits)
     */ 
    public void setA(int num) {
        a = num & 0xff;
    }
    
    /**
     * @return value in register B
     */ 
    public int getB() {
        return b;
    }
    
    /**
     * Set value for register B.
     * 
     * @param num (required) number to set regB to (masked to 8 bits)
     */ 
    public void setB(int num) {
        b = num & 0xff;
    }

    /**
     * @return value in register C
     */ 
    public int getC() {
        return c;
    }
        
    /**
     * Set value for register C.
     * 
     * @param num (required) number to set regC to (masked to 8 bits)
     */ 
    public void setC(int num) {
        c = num & 0xff;
    }
    
    /**
     * @return value in register D
     */ 
    public int getD() {
        return d;
    }
    
    /**
     * Set value for register D.
     * 
     * @param num (required) number to set regD to (masked to 8 bits)
     */ 
    public void setD(int num) {
        d = num & 0xff;
    }
    
    /**
     * @return value in register E
     */ 
    public int getE() {
        return e;
    }
    
    /**
     * Set value for register E.
     * 
     * @param num (required) number to set regE to (masked to 8 bits)
     */ 
    public void setE(int num) {
        e = num & 0xff;
    }
    
    /**
     * @return value in register F
     */ 
    public int getF() {
        return f;
    }
        
    /**
     * Set value for register F.
     * 
     * @param num (required) number to set regF to (masked to 8 bits)
     */ 
    public void setF(int num) {
        f = num & 0xff;
    }
    
    /**
     * @return value in register H
     */ 
    public int getH() {
        return h;
    }
    
    /**
     * Set value for register H.
     * 
     * @param num (required) number to set regH to (masked to 8 bits)
     */ 
    public void setH(int num) {
        h = num & 0xff;
    }
    
    /**
     * Set value for register L.
     * 
     * @param num (required) number to set regL to (masked to 8 bits)
     */ 
    public void setL(int num) {
        l = num & 0xff;
    }


    /**
     * @return value in register L
     */ 
    public int getL() {
        return l;
    }

    /**
     * @return value in register AF
     */ 
    public int getAF() {
        return (a << 8) & f;
    }
    
    /**
     * Set value for register AF.
     * 
     * @param num (required) number to set regAF to (masked to 16 bits)
     */ 
    public void setAF(int num) {
        a = (num & 0xff00) >> 8;
        f = num & 0xff;
    }
        
    /**
     * @return value in register BC
     */ 
    public int getBC() {
        return (b << 8) & c;
    }
    
    /**
     * Set value for register BC.
     * 
     * @param num (required) number to set regBC to (masked to 16 bits)
     */ 
    public void setBC(int num) {
        b = (num & 0xff00) >> 8;
        c = num & 0xff;
    }

    /**
     * @return value in register DE
     */ 
    public int getDE() {
        return (d << 8) & e;
    }
    
    /**
     * Set value for register DE.
     * 
     * @param num (required) number to set regDE to (masked to 16 bits)
     */ 
    public void setDE(int num) {
        d = (num & 0xff00) >> 8;
        e = num & 0xff;
    }

    /**
     * @return value in register HL
     */     
    public int getHL() {
        return (h << 8) & l;
    }
    
    /**
     * Set value for register HL.
     * 
     * @param num (required) number to set regHL to (masked to 16 bits)
     */ 
    public void setHL(int num) {
        h = (num & 0xff00) >> 8;
        l = num & 0xff;
    }
        
}
