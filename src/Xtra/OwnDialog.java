package Xtra;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.Timer;
import java.util.TimerTask;

import jleu.ui.MultiColorTransition;

import javax.swing.SwingConstants;

import GUI.Hauptfenster;


//EASTER EGG 
public class OwnDialog extends JDialog {
	//Performanter, siehe Hauptfenster
	private static final long serialVersionUID = 1089995272506462845L;
	private final JPanel contentPanel = new JPanel();
    private static Timer timer = new Timer();
	private JLabel nameA = new JLabel(""),
				   nameB = new JLabel("");
	private char[] cNameA = "Julian Leuze".toCharArray(),
				   cNameB = "René Sachs".toCharArray();
	private MultiColorTransition mct = new MultiColorTransition(127, new int[][] { {255,0,0}, {0,255,0}, {0,0,255} });
	private OwnDialog thisOne = this;

	public OwnDialog() {
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		contentPanel.setLayout(null);
		setResizable(false);
		setModal(false);
		setBounds(100, 100, 450, 117);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JButton btnNichtKlicken = new JButton("Fly away!");
		btnNichtKlicken.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				fly();
			}
			
		});
		btnNichtKlicken.setBounds(getBounds().width / 2 - 89 /2, 50, 111, 23);
		contentPanel.add(btnNichtKlicken);
		setLocationRelativeTo(null);
		
			nameA.setHorizontalAlignment(SwingConstants.CENTER);
			nameA.setBounds(getBounds().width /2 - 220, 11, 220, 28);
			nameB.setHorizontalAlignment(SwingConstants.CENTER);
			nameB.setBounds(getBounds().width / 2 , 11, 220, 28);
			fillLabel(nameA, '*', cNameA.length);
			fillLabel(nameB, '*', cNameB.length);
			contentPanel.add(nameA);
			contentPanel.add(nameB);
			timer.scheduleAtFixedRate(new TimerTask() {
				int charCount = 0;
				public void run() {
					replace(nameA, cNameA[charCount], charCount);
					charCount++;
					if(charCount == cNameA.length)
						this.cancel();
				}
			}, 10, 1000/cNameA.length);
			timer.scheduleAtFixedRate(new TimerTask() {
				int charCount = 0;
				public void run() {
					replaceRight(nameB, cNameB[charCount], charCount);
					charCount++;
					if(charCount == cNameB.length)
						this.cancel();
				}
			}, 10, 1000/cNameB.length);
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					contentPanel.setBackground(mct.infinteTest());
				}
				
			}, 0, 17);
	}
	
	public void fly() {
		timer.scheduleAtFixedRate(new TimerTask() {
			int deg = 135;
			int incrementor = 1;
			Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
			@Override
			public void run() {
				int whWhole = getHeight() + getInsets().bottom + getInsets().top,
					wwWhole = getWidth() + getInsets().left + getInsets().right;
				if(deg < 360)
					deg++;
				else
					deg = 0;
				
				double[] da = jleu.math.JMath.xyCircle(sc.width / 2,
													   sc.height /2,
													   wwWhole /2 +incrementor++,
													   whWhole /2 +incrementor++,
													   deg);
				if((int)da[0] < sc.width && (int) da[1] < sc.height) {
					setLocation((int)da[0],(int) da[1]);
				}
				else {
					dispose();
					Hauptfenster.appendNewLine(thisOne.getClass(), "Developer JDialog disposed");
					this.cancel();
				}
			}
			
		}, 0, 2);
	}
	public void replace(JLabel jl, char c, int pos) {
		char[] ch = jl.getText().toCharArray();
		ch[pos] = c;
		jl.setText(String.valueOf(ch));
	}
	
	public void replaceRight(JLabel jl, char c, int pos) {
		char[] ch = jl.getText().toCharArray();
		for(int i = 0; i < ch.length -1; i++) {
			char temp = ch[i];
			ch[i] = ch[i+1];
			ch[i+1] = temp;
		}
		ch[ch.length- 1] = c;
		jl.setText(String.valueOf(ch));
	}
	
	public void fillLabel(JLabel jl, char c, int len) {
		for(int i = 0; i < len; i++)
			jl.setText(jl.getText() + c);
	}
}
