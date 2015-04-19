package GUI;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;

import java.awt.Component;

public class AnfrageBereich extends JPanel {

	private static final long serialVersionUID = -1654485096034194100L;

	public AnfrageBereich(String msg, final Runnable rJa, ActionListener alNein, final JPanel root, final JScrollPane scrollPane) {
		setBorder(new LineBorder(Color.GREEN));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JLabel lblInfo = new JLabel("Wenn du diesen Text siehst ist etwas schiefgelaufen");
		lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
		lblInfo.setText(msg);
		add(lblInfo);
		
		JPanel panel = new JPanel();
		add(panel);
		
		JButton btnJa = new JButton("Annehmen");
		btnJa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(rJa).start();
				System.out.println("ja");
			}
		});
		panel.add(btnJa);
		
		JButton btnNein = new JButton("Ablehnen");
		btnNein.addActionListener(alNein);
		btnNein.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("nein");
			}
		});
		//Wird immer ausgeführt
		ActionListener removeAfterClick = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				AnfrageBereich ab = AnfrageBereich.this;
				root.remove(ab);
				root.setPreferredSize(new Dimension(315, root.getPreferredSize().height - ab.getPreferredSize().height));
				root.validate();
				root.repaint();
				scrollPane.validate();
				scrollPane.repaint();
			}	
		};
		btnJa.addActionListener(removeAfterClick);
		btnNein.addActionListener(removeAfterClick);
		panel.add(btnNein);

	}
}
