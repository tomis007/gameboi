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
    
    public enum Reg {A, B, C, D, E, F, H, L, AF, BC, DE, HL};
    
    
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
     * Set a value in a register
     * 
     * <p>Value is masked to 8bits (16 if one of the register combinations)
     * 
     * @param register (required) register to set
     * @param data (required) data to place in register
     */ 
    public void setReg(Reg register, int data) {
        switch(register) {
            case A: this.setA(data);
                        break;
            case B: this.setB(data);
                        break;
            case C: this.setC(data);
                        break;
            case D: this.setD(data);
                        break;
            case E: this.setE(data);
                        break;
            case F: this.setF(data);
                        break;
            case H: this.setH(data);
                        break;
            case L: this.setL(data);
                        break;
            case AF: this.setAF(data);
                        break;
            case BC: this.setBC(data);
                        break;
            case DE: this.setDE(data);
                        break;
            case HL: this.setHL(data);
                        break;
            default: System.err.println("Invalid attempt to set Register!");
                     break;
        }
    }
    
    
    
    
    /**
     * Get a value in a register
     * 
     * 
     * @param register (required) register to get value from
     * @return value in register (integer)
     */ 
    public int getReg(Reg register) {
        switch(register) {
            case A: return this.getA();
            case B: return this.getB();
            case C: return this.getC();
            case D: return this.getD();
            case E: return this.getE();
            case F: return this.getF();
            case H: return this.getH();
            case L: return this.getL();
            case AF: return this.getAF();
            case BC: return this.getBC();
            case DE: return this.getDE();
            case HL: return this.getHL();
            default: System.err.println("Invalid attempt to set Register!");
                     break;
        }
        return -1;
    }
    
    
    //register FLAG functions
    /**
     * Set Z FLAG
     */ 
    public void setZ() {
        f |= 0x80;
    }

    /**
     * Reset Z Flag
     */ 
    public void resetZ() {
        f &= 0x70;
    }
    
    /**
     * Set H FLAG
     * 
     */ 
    public void setH() {
        f |= 0x20;
    }

    /**
     * Reset H FLag
     */ 
    public void resetH() {
        f &= 0xd0;
    }
    
    /**
     * Set C Flag
     */ 
    public void setC() {
        f |= 0x10;
    }

    /**
     * Reset C Flag
     */ 
    public void resetC() {
        f &= 0xe0;
    }
    
    /**
     * Set N FLAG
     */ 
    public void setN() {
        f |= 0x40;
    }
    
    /**
     * Reset N Flag
     */ 
    public void resetN() {
        f &= 0xb0;
    }
    
    
    
    
    
    
    /**
     * @return value in register A
     */ 
    private int getA() {
        return a;
    }
    
    /**
     * Set value for register A.
     * 
     * @param num (required) number to set regA to (masked to 8 bits)
     */ 
    private void setA(int num) {
        a = num & 0xff;
    }
    
    /**
     * @return value in register B
     */ 
    private int getB() {
        return b;
    }
    
    /**
     * Set value for register B.
     * 
     * @param num (required) number to set regB to (masked to 8 bits)
     */ 
    private void setB(int num) {
        b = num & 0xff;
    }

    /**
     * @return value in register C
     */ 
    private int getC() {
        return c;
    }
        
    /**
     * Set value for register C.
     * 
     * @param num (required) number to set regC to (masked to 8 bits)
     */ 
    private void setC(int num) {
        c = num & 0xff;
    }
    
    /**
     * @return value in register D
     */ 
    private int getD() {
        return d;
    }
    
    /**
     * Set value for register D.
     * 
     * @param num (required) number to set regD to (masked to 8 bits)
     */ 
    private void setD(int num) {
        d = num & 0xff;
    }
    
    /**
     * @return value in register E
     */ 
    private int getE() {
        return e;
    }
    
    /**
     * Set value for register E.
     * 
     * @param num (required) number to set regE to (masked to 8 bits)
     */ 
    private void setE(int num) {
        e = num & 0xff;
    }
    
    /**
     * @return value in register F
     */ 
    private int getF() {
        return f;
    }
        
    /**
     * Set value for register F.
     * 
     * @param num (required) number to set regF to (masked to 8 bits)
     */ 
    private void setF(int num) {
        f = num & 0xff;
    }
    
    /**
     * @return value in register H
     */ 
    private int getH() {
        return h;
    }
    
    /**
     * Set value for register H.
     * 
     * @param num (required) number to set regH to (masked to 8 bits)
     */ 
    private void setH(int num) {
        h = num & 0xff;
    }
    
    /**
     * Set value for register L.
     * 
     * @param num (required) number to set regL to (masked to 8 bits)
     */ 
    private void setL(int num) {
        l = num & 0xff;
    }


    /**
     * @return value in register L
     */ 
    private int getL() {
        return l;
    }

    /**
     * @return value in register AF
     */ 
    private int getAF() {
        return (a << 8) | f;
    }
    
    /**
     * Set value for register AF.
     * 
     * @param num (required) number to set regAF to (masked to 16 bits)
     */ 
    private void setAF(int num) {
        a = (num & 0xff00) >> 8;
        f = num & 0xff;
    }
        
    /**
     * @return value in register BC
     */ 
    private int getBC() {
        return (b << 8) | c;
    }
    
    /**
     * Set value for register BC.
     * 
     * @param num (required) number to set regBC to (masked to 16 bits)
     */ 
    private void setBC(int num) {
        b = (num & 0xff00) >> 8;
        c = num & 0xff;
    }

    /**
     * @return value in register DE
     */ 
    private int getDE() {
        return (d << 8) | e;
    }
    
    /**
     * Set value for register DE.
     * 
     * @param num (required) number to set regDE to (masked to 16 bits)
     */ 
    private void setDE(int num) {
        d = (num & 0xff00) >> 8;
        e = num & 0xff;
    }

    /**
     * @return value in register HL
     */     
    private int getHL() {
        return (h << 8) | l;
    }
    
    /**
     * Set value for register HL.
     * 
     * @param num (required) number to set regHL to (masked to 16 bits)
     */ 
    private void setHL(int num) {
        h = (num & 0xff00) >> 8;
        l = num & 0xff;
    }
        
}
