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
    private int a;
    private int b;
    private int c;
    private int d;
    private int e;
    private int f;
    private int h;
    private int l;
    
    // stack pointer
    private int sp;
    // program counter
    private int pc;
    
    
    private GBMem memory;
    
    
    public CPU(GBMem memory) {
        this.memory = memory;
    }
    
    /**
     * Execute the next opcode in memory
     * 
     * @param memory GBMem memory to access
     * @return clock cycles taken to execute the opcode
     */ 
    public int ExecuteOpcode(GBMem memory) {
        
        int opcode = memory.readByte(pc);
        pc++;
        return runInstruction(opcode);
    }
    
    
    private int runInstruction(int opcode) {
        
      return 0;  
    };
    
}

