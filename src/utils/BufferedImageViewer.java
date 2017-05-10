package utils;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
*
* @author Lukas Brchl
*/
public class BufferedImageViewer {
	private JFrame frame;
	private JLabel lbl;
	private ImageIcon icon;
	
	public BufferedImageViewer() {
//		init();
	}
	
	private void init () {	
		icon = null;
		frame = new JFrame();
		frame.setLayout(new FlowLayout());
		frame.setSize(640, 512);
		lbl = new JLabel();
		frame.add(lbl);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void loadImage(BufferedImage bufferedImage) {
		if (frame == null) init();
		icon = new ImageIcon(bufferedImage);			
		lbl.setIcon(icon);
	}
}
