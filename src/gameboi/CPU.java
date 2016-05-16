/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameboi;




/**
 * Z80 Gameboy CPU
 * 
 * Implementation of a gameboy cpu. Eight registers (a,b,c,d,e,f,h,l), a
 * stack pointer, program counter
 * 
 * @author tomis007
 */
public class CPU {
    // registers
    private GBRegisters registers;
    
    private enum Reg {REG_A, REG_B, REG_C, REG_D, REG_E, REG_F, REG_H, REG_L, 
                      REG_AF, REG_BC, REG_DE, REG_HL};
    
    // stack pointer
    private int sp;
    // program counter
    private int pc;
    //associated memory to use with CPU
    private GBMem memory;
    
    public CPU(GBMem memory) {
        pc = 0x100;
        sp = 0xfffe;
        this.memory = memory;
        registers = new GBRegisters();
    }
    
    /**
     * Execute the next opcode in memory
     * 
     * @return clock cycles taken to execute the opcode
     */ 
    public int ExecuteOpcode() {
        
        int opcode = memory.readByte(pc);
        pc++;
        return runInstruction(opcode);
    }
    
    
    
    /**
     * Opcode Instructions for the Gameboy Z80 Chip.
     * 
     * NOTE: PUBLIC FOR TESTING!!!!
     * <p>
     * Runs the instruction associated with the opcode, and returns the 
     * clock cycles taken.
     * 
     * @param opcode (required) opcode to execute
     * @return number of cycles taken to execute
     */ 
    private int runInstruction(int opcode) { 
      int cycles = 0;
      switch (opcode) {
          case 0x0: cycles = 4; //NOP
                    break;
          case 0x06: cycles = eightBitLdNnN(Reg.REG_B);
                     break;
          case 0x0e: cycles = eightBitLdNnN(Reg.REG_C);
                     break;
          case 0x16: cycles = eightBitLdNnN(Reg.REG_D);
                     break;
          case 0x1e: cycles = eightBitLdNnN(Reg.REG_E);
                     break;
          case 0x26: cycles = eightBitLdNnN(Reg.REG_H);
                     break;
          case 0x2e: cycles = eightBitLdNnN(Reg.REG_L);
                     break;
          default:
              System.err.println("Unimplemented opcode: 0x" + 
                      Integer.toHexString(opcode));
              System.exit(1);
      }    
      return cycles;
    }
    
    
    /**
     * LD nn,n. Put value nn into n.
     * 
     * <p>nn = B,C,D,E,H,L,BC,DE,HL,SP
     * n = 8 bit immediate value
     * 
     * @param register (required) register (nn) to load to
     */ 
    private int eightBitLdNnN(Reg register) {
        int data = memory.readByte(pc);
        pc++;
        switch(register) {
            case REG_B: registers.setB(data);
                         break;
            case REG_C: registers.setC(data);
                         break;
            case REG_D: registers.setD(data);
                         break;
            case REG_E: registers.setE(data);
                         break;
            case REG_H: registers.setH(data);
                         break;
            case REG_L: registers.setL(data);
                         break;
            default: System.err.println("Invalid eight bit ld nn, n!");
                         break;
        }
        return 8;   
    }
    
}

