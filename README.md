# GameBoi

#### About
This is a gameboy/gameboy color emulator written in java. It is currently
under development. However, unlike most emulators, GameBoi does not
provide an interface for playing the emulator. Instead it abstracts the
emulation away from the GUI to provide an API for any type of GUI one may
want to implement. For reference: https://github.com/tomis007/cloudboi


#### Status
The emulator passes Blargg's cpu instruction test and
emulates some games successfully. The cpu 'z80' emulation
is solid and the lcd emulation works well. Saving has yet to be implemented.
The memory access timing and instruction timing are both not very accurate.

#### Screenshots  

![alt text][cpu_instr]  

![alt text][mario_1]  

![alt text][mario_2]  

![alt text][link]  

![alt text][pokemon]  



[cpu_instr]: https://github.com/tomis007/gameboi/blob/master/screenshots/blargg_cpu_instructions.png
[mario_1]: https://github.com/tomis007/gameboi/blob/master/screenshots/mario_land_2_1.png
[mario_2]: https://github.com/tomis007/gameboi/blob/master/screenshots/mario_land_2_2.png
[link]: https://github.com/tomis007/gameboi/blob/master/screenshots/Links_Awakening.png
[pokemon]: https://github.com/tomis007/gameboi/blob/master/screenshots/pokemon.png
