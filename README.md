# GBz80-to-Hex
Converts GBz80 Assembly into hex values

## About   

This program is used to convert GBz80 (the Gameboy assembly language) into hex values. This program was made to
assist people with arbitrary code execution on the Gameboy, such as the 8F glitch in Pokemon Red. 
For more information about the 8F glitch, check out [this guide by TheZZAZZGlitch](https://forums.glitchcity.info/index.php?topic=6638.0) 
and for help setting up location-based input, [check out this video by ChickasaurusGL](https://www.youtube.com/watch?v=ddSHGg4-qSY&t=5s) (and read the video description).

## Notes  

#### Syntax:
For the program to recognize instructions, they must follow a specific syntax.
- Any hex values must be preceded by a '$'
   * LD BC, $ABCD ✔
   * LD BC, ABCD  :x:
- Each instruction must be on a seperate line
- Instructions can have any amount of whitespace at the beginning or end, but and instruction with multiple words
  must have only one space between each word, and if the instruction has two operands there must be a comma at the
  end of the first operand.
   * INC BC    :heavy_check_mark:
   * INC  BC   :x: (2 spaces in between words)
   * LD B, H   :heavy_check_mark:
   * LD B H    :x:
   * LD B , H  :x:
- Instructions are NOT case sensitive.
   * ADD HL, SP  :heavy_check_mark:
   * add hl, sp  :heavy_check_mark:
   * aDd Hl, sP  :heavy_check_mark: (not recommended)
#### Labels:
This program does support labels. Labels must be on a seperate line and must be one word ended by a colon ':'.

   my_label:  
   LD A, B  
   JP NZ, my_label  
   
- Labels ARE case sensitive
#### Other:
- This program currently does not support the db instruction for lists of values.
- Alternate mnemonics for instructions are supported, such as "LD A, (C)" and "LD A,($FF00+C)". A full list of instructions
  can be found [here.](http://www.pastraiser.com/cpu/gameboy/gameboy_opcodes.html)
  
If you have any questions, feel free to email me at connorbman@gmail.com

©2019 Connor Belman 
  
