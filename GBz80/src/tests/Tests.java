package tests;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import asmToHex.Hex;

public class Tests extends JFrame {
	
	JPanel p = new JPanel();
	
	JPanel east = new JPanel();
	JTextArea output = new JTextArea(10, 8);
	
	JPanel center = new JPanel();
	JTextArea input = new JTextArea();
	
	JPanel north = new JPanel();
	JLabel addLabel = new JLabel("Starting Address:  $");
	JTextField addInput = new JTextField(6);
	
	JPanel west = new JPanel();
	JButton start = new JButton("Start");

	
	JLabel log = new JLabel("");

	public static void main(String[] args) {
		new Tests();
	}
	
	public Tests() {
		super("Basic Swing App");
		setSize(600,400);
		setResizable(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		p.setLayout(new BorderLayout());
		
		//input (center)
		center.setLayout(new BorderLayout());
		int tabSize = 4;
		input.setTabSize(tabSize);
		center.add(input, BorderLayout.CENTER);
		TitledBorder inputTitle;
		inputTitle = BorderFactory.createTitledBorder("Input");
		center.setBorder(inputTitle);
		p.add(center, BorderLayout.CENTER);
		
		//output (east)
		east.setLayout(new BorderLayout());
		east.add(output, BorderLayout.CENTER);
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
		west.add(start);
		p.add(west, BorderLayout.WEST);
		
		//error log (south)
		p.add(log, BorderLayout.SOUTH);
		
		
		
		add(p);
		setVisible(true);
	}
	
}
