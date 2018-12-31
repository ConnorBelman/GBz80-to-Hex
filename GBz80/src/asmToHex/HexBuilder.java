package asmToHex;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*----------------------------------------------------------------------
  Made by Connor Belman
----------------------------------------------------------------------*/

public class HexBuilder extends JFrame implements ActionListener {

	//Logic variables
	int lineNum = 1;
	Hex startAdd;
	ArrayList<String> opList = new ArrayList<String>();
	Map<String, String> labelAdds = new HashMap<String, String>();
	static Map<String, String> opCodes = new HashMap<String, String>();
	
	//GUI variables
	JPanel p = new JPanel();
	
	JPanel east = new JPanel();
	JTextArea output = new JTextArea(10, 8);
	JScrollPane scrollPaneOut = new JScrollPane(output);
	
	JPanel center = new JPanel();
	JTextArea input = new JTextArea();
	JScrollPane scrollPaneIn = new JScrollPane(input);
	TextLineNumber tln = new TextLineNumber(input);
	
	JPanel north = new JPanel();
	JLabel addLabel = new JLabel("Starting Address:  $");
	JTextField addInput = new JTextField("0000", 6);
	
	JPanel west = new JPanel();
	JButton start = new JButton("Start");

	JLabel log = new JLabel("");
	
	
	public static void main(String args[]) {
		opCodesMap();
		new HexBuilder();
	}
	
	public HexBuilder() {
		super("CGB ASM to Hex Converter");
		setSize(600,400);
		setResizable(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		p.setLayout(new BorderLayout());
		
		//input (center)
		center.setLayout(new BorderLayout());
		int tabSize = 4;
		input.setTabSize(tabSize);
		scrollPaneIn.setRowHeaderView(tln);
		center.add(scrollPaneIn, BorderLayout.CENTER);
		TitledBorder inputTitle;
		inputTitle = BorderFactory.createTitledBorder("Input");
		center.setBorder(inputTitle);
		p.add(center, BorderLayout.CENTER);
		
		//output (east)
		east.setLayout(new BorderLayout());
		east.add(scrollPaneOut, BorderLayout.CENTER);
		TitledBorder outputTitle;
		outputTitle = BorderFactory.createTitledBorder("Output");
		east.setBorder(outputTitle);
		p.add(east, BorderLayout.EAST);
		
		//starting address (north)
		north.add(addLabel);
		north.add(addInput);
		p.add(north, BorderLayout.NORTH);
		
		//start button (west)
		west.setLayout(new GridLayout(10, 1));
		start.addActionListener(this);
		west.add(start);
		p.add(west, BorderLayout.WEST);
		
		//error log (south)
		log.setForeground(Color.red);
		p.add(log, BorderLayout.SOUTH);
		
		add(p);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		
		//clears variables
		log.setText("");
		String address = addInput.getText();
		opList = new ArrayList<String>();
		labelAdds = new HashMap<String, String>();
		lineNum = 1;
		try {
			startAdd = new Hex(address);
			findLabels();
			String fullText = preprocess();
			for (String line : fullText.split("\\n")) {
				parseLine(line);
				lineNum++;
			}
			printList();
		}
		catch(Exception e1) {
			System.out.println(e1.getMessage());
			log.setText(e1.getMessage());
		}
	}
	
	private void parseLine(String line) {
		String parsedLine = line.toUpperCase().trim();
		String[] lineSeg = parsedLine.split(" ");
		//Checks if the line is already a valid opcode
		if (opCodes.containsKey(parsedLine)) {
			updateList(parsedLine);
		}
		//If the line in not already a recognized opcode,
		//if the line is 2 words, calls updateListHex with the hex value in the second word
		else if (lineSeg.length == 2) {
			updateListHex(parsedLine, new Hex(lineSeg[1].substring(1)));
		}
		//If the line is 3 words
		else if (lineSeg.length == 3 ) {
			//Looks for instructions that must have a hex value in the third word
			if (lineSeg[0].equals("JR") || lineSeg[0].equals("JP") || lineSeg[0].equals("CALL") ||
				lineSeg[0].equals("ADD") || lineSeg[0].equals("ADC") || lineSeg[0].equals("SBC") || lineSeg[0].equals("LDHL")) {
				updateListHex(parsedLine, new Hex(lineSeg[2].substring(1)));
			}
			//If the first word is "LD", checks for which instruction it could be
			else if (lineSeg[0].equals("LD")) {
				//Checks for a hex value contained by parenthesis in the second word
				if (lineSeg[1].charAt(0) == '(' && !lineSeg[1].equals("(HL),")) {
					//Checks for instruction "LD ($FF00+a8), A"
					if (lineSeg[1].contains("($FF00+")) {
						updateListHex(parsedLine, new Hex(lineSeg[1].substring(8, lineSeg[1].length() - 2)));
					}
					//Otherwise, assumes the second word is has a hex value in parenthesis
					else {
						updateListHex(parsedLine, new Hex(lineSeg[1].substring(2, lineSeg[1].length() - 2)));
					}
				}
				//Checks for a hex value contained by parenthesis in the third word
				else if (lineSeg[2].charAt(0) == '(' && !lineSeg[2].equals("(HL),")) {
					//Checks for instruction "LD A, ($FF00+a8)"
					if (lineSeg[2].contains("($FF00+")) {
						updateListHex(parsedLine, new Hex(lineSeg[2].substring(8, lineSeg[2].length() - 1)));
					}
					//Otherwise, assumes the third word is a hex value in parenthesis
					else {
						updateListHex(parsedLine, new Hex(lineSeg[2].substring(2, lineSeg[2].length() - 1)));
					}
				}
				//Checks for instruction "LD HL, SP+r8"
				else if (lineSeg[2].contains("SP+")) {
					updateListHex(parsedLine, new Hex(lineSeg[2].substring(4)));
				}
				//Otherwise, assumes the instruction is a LD instruction with a hex value as the third word
				else {
					updateListHex(parsedLine, new Hex(lineSeg[2].substring(1)));
				}
			}
			//If the first word is "LDH"
			else if (lineSeg[0].equals("LDH")) {
				//Checks for instruction "LDH (a8), A"
				if (lineSeg[1].charAt(0) == '(') {
					updateListHex(parsedLine, new Hex(lineSeg[1].substring(2, lineSeg[1].length() - 2)));
				}
				//Otherwise, checks for instruction "LDH A, (a8)"
				else if (lineSeg[2].charAt(0) == '(') {
					updateListHex(parsedLine, new Hex(lineSeg[2].substring(2, lineSeg[2].length() - 1)));
				}
			}
			//If unable to recognize the line, throws an error
			else {
				throw new RuntimeException("Cannot parse line: " + parsedLine);
			}
		}
	}
	
	/*
	 * Updates the ArrayList that is printed for lines that do not contain hex values.
	 */
	private void updateList(String parsedLine) {
		if(opCodes.get(parsedLine) == null) {
			throw new RuntimeException("Invalid instruction on line " + lineNum + ": updateList: " + parsedLine);
		}
		opList.add(opCodes.get(parsedLine));
	}
	
	/*
	 * Updates the ArrayList that is printed for lines that do contain hex values. If the hex values
	 * is 2 characters long, replaces the hex value with "D8". If the hex value is 4 characters long,
	 * replaces the hex value with "D16". The edited lines are checked for in the opCodes map and
	 * added to the ArrayList along with the hex value in little endian format.
	 */
	private void updateListHex(String parsedLine, Hex hexValue) {
		//If the hex value is 2 characters, replaces it the "D8" and looks for it in the map
		if(hexValue.length() == 2) {
			if(opCodes.get(parsedLine.replace(hexValue.toString(), "D8")) == null) {
				throw new RuntimeException("Invalid instruction on line " + lineNum + ": updateListHex");
			}
			opList.add(opCodes.get(parsedLine.replace(hexValue.toString(), "D8")));
			opList.add(hexValue.getValue());
		}
		//If the hex value is 4 characters, replaces it with "D16" and looks for it in the map
		else if(hexValue.length() == 4) {
			String opLine = parsedLine.replace(hexValue.toString(), "D16");
			if(opCodes.get(opLine) == null) {
				throw new RuntimeException("Invalid instruction on line " + lineNum + ": updateList");
			}
			opList.add(opCodes.get(opLine));
			opList.add(hexValue.getValue().substring(2));
			opList.add(hexValue.getValue().substring(0, 2));
		}
		//Otherwise, the hex value is invalid, throws an error
		else {
			throw new RuntimeException("Invalid Hex Value on line " + lineNum + ": updateList");
		}
	}
	
	/*
	 * Before preprocessing, goes through the input and finds the address of any labels.
	 * Adds them to the labelAdds map.
	 */
	public void findLabels() {
		Hex curAdd = startAdd.copy();
		int lineNum = 1;
		for (String line : input.getText().split("\\n")) {
			//If the line is not blank
			if (!line.equals("")) {
				String[] lineSeg = line.split(" ");
				//If the line is only 1 word
				if (lineSeg.length == 1) {
					//If the line is a label, adds it to the label map
					if (line.charAt(line.length() - 1) == ':') {
						String key = line.substring(0, line.length() - 1);
						if (labelAdds.containsKey(key)) {
							throw new RuntimeException("Label " + key + " declared twice");
						}
						else {
							labelAdds.put(key, curAdd.toString());
							System.out.println("label " + key + " found at address " + curAdd.toString());
						}
					}
					//Otherwise, increases the current address by 1 if the line is an opcode
					else if (opCodes.containsKey(line.toUpperCase())) {
						curAdd.add(new Hex(1));
					}
					else {
						throw new RuntimeException("Invalid instruction on line " + lineNum);
					}
				}
				//Else if the line is 2 words
				else if (lineSeg.length == 2) {
					String firstSeg = lineSeg[0].toUpperCase();
					//If the line is already an opcode, adds 1 to the current address
					if (opCodes.containsKey(line.toUpperCase())) {
						curAdd.add(new Hex(1));
					}
					//If the line is a 2 byte instruction, adds 2 to the current address
					else if (firstSeg.equals("JR") || firstSeg.equals("SUB") || firstSeg.equals("AND") ||
							firstSeg.equals("OR") || firstSeg.equals("XOR") || firstSeg.equals("CP")) {
						curAdd.add(new Hex(2));
					}
					//If the line is a 3 byte instruction, adds 3 to the current address
					else if (firstSeg.equals("JP") || firstSeg.equals("CALL")) {
						curAdd.add(new Hex(3));
					}
					else {
						throw new RuntimeException("Invalid instruction on line " + lineNum);
					}
				}
				//Else if the line is 3 words
				else if (lineSeg.length == 3) {
					String firstSeg = lineSeg[0].toUpperCase();
					//If the line is already an opcode, adds 1 to the current address
					if (opCodes.containsKey(line.toUpperCase())) {
						curAdd.add(new Hex(1));
					}
					//If the line is a 2 byte instruction, adds 2 to the current address
					else if (firstSeg.equals("JR") || firstSeg.equals("ADC") || firstSeg.equals("SBC") ||
							firstSeg.equals("LDH") || firstSeg.equals("ADD") || firstSeg.equals("LDHL") ||
							(firstSeg.equals("LD") && (line.toUpperCase().contains("$FF00+") || lineSeg[1].length() == 2 ||
							lineSeg[1].toUpperCase().equals("(HL),") || lineSeg[2].toUpperCase().contains("SP+")))) {
						curAdd.add(new Hex(2));
					}
					//If the line is a 3 byte instruction, adds 3 to the current address
					else if (firstSeg.equals("LD") || firstSeg.equals("JP") || firstSeg.equals("CALL")) {
						curAdd.add(new Hex(3));
					}
				}
				//No opcodes are more than 3 words, so throws an error if that is the case
				else {
					throw new RuntimeException("Invalid instruction on line " + lineNum);
				}
				lineNum++;
			}
		}
	}
	
	/*
	 * Preprocesses the asm text by finding all of the function names and replacing them
	 * with their addresses.
	 * Returns a string with the updated version of the input.
	 */
	public String preprocess() {
		String processedText = "";
		for (String line : input.getText().split("\n")) {
			//If the line isn't empty
			if (!line.equals("")) {
				//If the line is not a label
				if (!line.substring(line.length() - 1, line.length()).equals(":")) {
					//Checks if the instruction uses and label and replaces it with the labels address
					for (String label : labelAdds.keySet()) {
						if (line.contains(label)) {
							line = line.replace(label, labelAdds.get(label));
						}
					}
					//Adds the line to the processed text
					processedText += line + "\n";
				}
			}
		}
		return processedText;
	}
	
	/* 
	 * prints the hex values in opList with their associated address.
	 */
	private void printList() {
		Hex address = startAdd.copy();
		output.setText("");
		for(int i = 0; i < opList.size(); i++) {
			output.setText(output.getText() + address.getValue() + ":   " + opList.get(i) + "\n");
			address.inc();
		}
	}
	
	/*
	 * Fills the opCodes map with all of the GBC opcodes and their associated hex value.
	 * Some instructions have alternative mnemonics.
	 */
	public static void opCodesMap() {	
		opCodes.put("NOP", 				"00");
		opCodes.put("LD BC, D16", 		"01");
		opCodes.put("LD (BC), A", 		"02");
		opCodes.put("INC BC", 			"03");
		opCodes.put("INC B", 			"04");
		opCodes.put("DEC B", 			"05");
		opCodes.put("LD B, D8", 		"06");
		opCodes.put("RLCA", 			"07");
		opCodes.put("LD (D16), SP", 	"08");
		opCodes.put("ADD HL, BC",		"09");
		opCodes.put("LD A, (BC)", 		"0A");
		opCodes.put("DEC BC", 			"0B");
		opCodes.put("INC C", 			"0C");
		opCodes.put("DEC C", 			"0D");
		opCodes.put("LD C, D8", 		"0E");
		opCodes.put("RRCA", 			"0F");
		opCodes.put("STOP 0", 			"10");
		opCodes.put("LD DE, D16", 		"11");
		opCodes.put("LD (DE), A", 		"12");
		opCodes.put("INC DE", 			"13");
		opCodes.put("INC D", 			"14");
		opCodes.put("DEC D", 			"15");
		opCodes.put("LD D, D8", 		"16");
		opCodes.put("RLA", 				"17");
		opCodes.put("JR D8", 			"18");
		opCodes.put("ADD HL, DE",		"19");
		opCodes.put("LD A, (DE)", 		"1A");
		opCodes.put("DEC DE", 			"1B");
		opCodes.put("INC E", 			"1C");
		opCodes.put("DEC E", 			"1D");
		opCodes.put("LD E, D8", 		"1E");
		opCodes.put("RRA", 				"1F");
		opCodes.put("JR NZ, D8", 		"20");
		opCodes.put("LD HL, D16", 		"21");
		opCodes.put("LD (HL+), A", 		"22");
		opCodes.put("LD (HLI), A", 		"22");
		opCodes.put("LDI (HL), A", 		"22");
		opCodes.put("INC HL", 			"23");
		opCodes.put("INC H", 			"24");
		opCodes.put("DEC H", 			"25");
		opCodes.put("LD H, D8", 		"26");
		opCodes.put("DAA", 				"27");
		opCodes.put("JR Z, D8", 		"28");
		opCodes.put("ADD HL, HL", 		"29");
		opCodes.put("LD A, (HL+)", 		"2A");
		opCodes.put("LD A, (HLI)", 		"2A");
		opCodes.put("LDI A, (HL)", 		"2A");
		opCodes.put("DEC HL", 			"2B");
		opCodes.put("INC L", 			"2C");
		opCodes.put("DEC L", 			"2D");
		opCodes.put("LD L, D8", 		"2E");
		opCodes.put("CPL", 				"2F");
		opCodes.put("JR NC, D8", 		"30");
		opCodes.put("LD SP, D16", 		"31");
		opCodes.put("LD (HL-), A", 		"32");
		opCodes.put("LD (HLD), A", 		"32");
		opCodes.put("LDD (HL), A", 		"32");
		opCodes.put("INC SP", 			"33");
		opCodes.put("INC (HL)", 		"34");
		opCodes.put("DEC (HL)", 		"35");
		opCodes.put("LD (HL), D8", 		"36");
		opCodes.put("SCF", 				"37");
		opCodes.put("JR C, D8", 		"38");
		opCodes.put("ADD HL, SP", 		"39");
		opCodes.put("LD A, (HL-)", 		"3A");
		opCodes.put("LD A, (HLD)", 		"3A");
		opCodes.put("LDD A, (HL)", 		"3A");
		opCodes.put("DEC SP", 			"3B");
		opCodes.put("INC A", 			"3C");
		opCodes.put("DEC A", 			"3D");
		opCodes.put("LD A, D8", 		"3E");
		opCodes.put("CCF", 				"3F");
		opCodes.put("LD B, B", 			"40");
		opCodes.put("LD B, C", 			"41");
		opCodes.put("LD B, D", 			"42");
		opCodes.put("LD B, E", 			"43");
		opCodes.put("LD B, H", 			"44");
		opCodes.put("LD B, L", 			"45");
		opCodes.put("LD B, (HL)", 		"46");
		opCodes.put("LD B, A", 			"47");
		opCodes.put("LD C, B", 			"48");
		opCodes.put("LD C, C", 			"49");
		opCodes.put("LD C, D", 			"4A");
		opCodes.put("LD C, E", 			"4B");
		opCodes.put("LD C, H", 			"4C");
		opCodes.put("LD C, L", 			"4D");
		opCodes.put("LD C, (HL)", 		"4E");
		opCodes.put("LD C, A", 			"4F");
		opCodes.put("LD D, B", 			"50");
		opCodes.put("LD D, C", 			"51");
		opCodes.put("LD D, D", 			"52");
		opCodes.put("LD D, E", 			"53");
		opCodes.put("LD D, H", 			"54");
		opCodes.put("LD D, L", 			"55");
		opCodes.put("LD D, (HL)", 		"56");
		opCodes.put("LD D, A", 			"57");
		opCodes.put("LD E, B", 			"58");
		opCodes.put("LD E, C", 			"59");
		opCodes.put("LD E, D", 			"5A");
		opCodes.put("LD E, E", 			"5B");
		opCodes.put("LD E, H", 			"5C");
		opCodes.put("LD E, L", 			"5D");
		opCodes.put("LD E, (HL)", 		"5E");
		opCodes.put("LD E, A", 			"5F");
		opCodes.put("LD H, B", 			"60");
		opCodes.put("LD H, C", 			"61");
		opCodes.put("LD H, D", 			"62");
		opCodes.put("LD H, E", 			"63");
		opCodes.put("LD H, H", 			"64");
		opCodes.put("LD H, L", 			"65");
		opCodes.put("LD H, (HL)", 		"66");
		opCodes.put("LD H, A", 			"67");
		opCodes.put("LD L, B", 			"68");
		opCodes.put("LD L, C", 			"69");
		opCodes.put("LD L, D", 			"6A");
		opCodes.put("LD L, E", 			"6B");
		opCodes.put("LD L, H", 			"6C");
		opCodes.put("LD L, L", 			"6D");
		opCodes.put("LD L, (HL)", 		"6E");
		opCodes.put("LD L, A", 			"6F");
		opCodes.put("LD (HL), B", 		"70");
		opCodes.put("LD (HL), C", 		"71");
		opCodes.put("LD (HL), D", 		"72");
		opCodes.put("LD (HL), E", 		"73");
		opCodes.put("LD (HL), H", 		"74");
		opCodes.put("LD (HL), L", 		"75");
		opCodes.put("HALT", 			"76");
		opCodes.put("LD (HL), A", 		"77");
		opCodes.put("LD A, B", 			"78");
		opCodes.put("LD A, C", 			"79");
		opCodes.put("LD A, D", 			"7A");
		opCodes.put("LD A, E", 			"7B");
		opCodes.put("LD A, H", 			"7C");
		opCodes.put("LD A, L", 			"7D");
		opCodes.put("LD A, (HL)", 		"7E");
		opCodes.put("LD A, A", 			"7F");
		opCodes.put("ADD A, B", 		"80");
		opCodes.put("ADD A, C", 		"81");
		opCodes.put("ADD A, D", 		"82");
		opCodes.put("ADD A, E", 		"83");
		opCodes.put("ADD A, H", 		"84");
		opCodes.put("ADD A, L", 		"85");
		opCodes.put("ADD A, (HL)", 		"86");
		opCodes.put("ADD A, A", 		"87");
		opCodes.put("ADC A, B", 		"88");
		opCodes.put("ADC A, C", 		"89");
		opCodes.put("ADC A, D", 		"8A");
		opCodes.put("ADC A, E", 		"8B");
		opCodes.put("ADC A, H", 		"8C");
		opCodes.put("ADC A, L", 		"8D");
		opCodes.put("ADC A, (HL)", 		"8E");
		opCodes.put("ADC A, A", 		"8F");
		opCodes.put("SUB B", 			"90");
		opCodes.put("SUB C", 			"91");
		opCodes.put("SUB D", 			"92");
		opCodes.put("SUB E", 			"93");
		opCodes.put("SUB H", 			"94");
		opCodes.put("SUB L", 			"95");
		opCodes.put("SUB (HL)", 		"96");
		opCodes.put("SUB A", 			"97");
		opCodes.put("SBC A, B", 		"98");
		opCodes.put("SBC A, C", 		"99");
		opCodes.put("SBC A, D", 		"9A");
		opCodes.put("SBC A, E", 		"9B");
		opCodes.put("SBC A, H", 		"9C");
		opCodes.put("SBC A, L", 		"9D");
		opCodes.put("SBC A, (HL)", 		"9E");
		opCodes.put("SBC A, A", 		"9F");
		opCodes.put("AND B", 			"A0");
		opCodes.put("AND C", 			"A1");
		opCodes.put("AND D", 			"A2");
		opCodes.put("AND E", 			"A3");
		opCodes.put("AND H", 			"A4");
		opCodes.put("AND L", 			"A5");
		opCodes.put("AND (HL)", 		"A6");
		opCodes.put("AND A", 			"A7");
		opCodes.put("XOR B", 			"A8");
		opCodes.put("XOR C", 			"A9");
		opCodes.put("XOR D", 			"AA");
		opCodes.put("XOR E", 			"AB");
		opCodes.put("XOR H", 			"AC");
		opCodes.put("XOR L", 			"AD");
		opCodes.put("XOR (HL)", 		"AE");
		opCodes.put("XOR A", 			"AF");
		opCodes.put("OR B", 			"B0");
		opCodes.put("OR C", 			"B1");
		opCodes.put("OR D", 			"B2");
		opCodes.put("OR E", 			"B3");
		opCodes.put("OR H", 			"B4");
		opCodes.put("OR L", 			"B5");
		opCodes.put("OR (HL)", 			"B6");
		opCodes.put("OR A", 			"B7");
		opCodes.put("CP B", 			"B8");
		opCodes.put("CP C", 			"B9");
		opCodes.put("CP D", 			"BA");
		opCodes.put("CP E", 			"BB");
		opCodes.put("CP H", 			"BC");
		opCodes.put("CP L", 			"BD");
		opCodes.put("CP (HL)", 			"BE");
		opCodes.put("CP A", 			"BF");
		opCodes.put("RET NZ", 			"C0");
		opCodes.put("POP BC", 			"C1");
		opCodes.put("JP NZ, D16", 		"C2");
		opCodes.put("JP D16", 			"C3");
		opCodes.put("CALL NZ, D16", 	"C4");
		opCodes.put("PUSH BC", 			"C5");
		opCodes.put("ADD A, D8", 		"C6");
		opCodes.put("RST 00H", 			"C7");
		opCodes.put("RET Z", 			"C8");
		opCodes.put("RET", 				"C9");
		opCodes.put("JP Z, D16", 		"CA");
		opCodes.put("PREFIX CB", 		"CB");
		opCodes.put("CALL Z, D16", 		"CC");
		opCodes.put("CALL D16", 		"CD");
		opCodes.put("ADC A, D8", 		"CE");
		opCodes.put("RST 08H", 			"CF");
		opCodes.put("RET NC", 			"D0");
		opCodes.put("POP DE", 			"D1");
		opCodes.put("JP NC, D16", 		"D2");
		opCodes.put("D3_UNUSED", 		"D3");
		opCodes.put("CALL NC, D16", 	"D4");
		opCodes.put("PUSH DE", 			"D5");
		opCodes.put("SUB D8", 			"D6");
		opCodes.put("RST 10H", 			"D7");
		opCodes.put("RET C", 			"D8");
		opCodes.put("RETI", 			"D9");
		opCodes.put("JP C, D16", 		"DA");
		opCodes.put("DB_UNUSED", 		"DB");
		opCodes.put("CALL C, D16", 		"DC");
		opCodes.put("DD_UNUSED", 		"DD");
		opCodes.put("SBC A, D8", 		"DE");
		opCodes.put("RST 18H", 			"DF");
		opCodes.put("LDH (D8), A", 		"E0");
		opCodes.put("LD ($FF00+D8), A", "E0");
		opCodes.put("POP HL", 			"E1");
		opCodes.put("LD (C), A", 		"E2");
		opCodes.put("LD ($FF00+C), A", 	"E2");
		opCodes.put("E3_UNUSED", 		"E3");
		opCodes.put("E4_UNUSED", 		"E4");
		opCodes.put("PUSH HL", 			"E5");
		opCodes.put("AND D8", 			"E6");
		opCodes.put("RST 20H", 			"E7");
		opCodes.put("ADD SP, D8", 		"E8");
		opCodes.put("JP (HL)", 			"E9");
		opCodes.put("LD (D16), A", 		"EA");
		opCodes.put("EB_UNUSED", 		"EB");
		opCodes.put("EC_UNUSED", 		"EC");
		opCodes.put("ED_UNUSED", 		"ED");
		opCodes.put("XOR D8", 			"EE");
		opCodes.put("RST 28H", 			"EF");
		opCodes.put("LDH A, (D8)", 		"F0");
		opCodes.put("LD A, ($FF00+D8)", "F0");
		opCodes.put("POP AF", 			"F1");
		opCodes.put("LD A, (C)", 		"F2");
		opCodes.put("LD A, ($FF00+C)", 	"F2");
		opCodes.put("DI", 				"F3");
		opCodes.put("F4_UNUSED", 		"F4");
		opCodes.put("PUSH AF", 			"F5");
		opCodes.put("OR D8", 			"F6");
		opCodes.put("RST 30H", 			"F7");
		opCodes.put("LD HL, SP+D8", 	"F8");
		opCodes.put("LDHL SP, D8", 		"F8");
		opCodes.put("LD SP, HL", 		"F9");
		opCodes.put("LD A, (D16)", 		"FA");
		opCodes.put("EI", 				"FB");
		opCodes.put("FC_UNUSED", 		"FC");
		opCodes.put("FD_UNUSED", 		"FD");
		opCodes.put("CP D8", 			"FE");
		opCodes.put("RST 38H", 			"FF");
	}
	
}