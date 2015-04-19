package Xtra;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class WindowManipulations {
	
	private static ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
	private static int noShaked = 0;
	
	public static void vibrate(final JFrame frame) {
		Timer a = new Timer();
		a.scheduleAtFixedRate(new TimerTask() {
			int i = 0,
			    direction = 1;
			Point p = frame.getLocation();
			@Override
			public void run() {
				i++;
				frame.setLocation(p.x, p.y + i*direction);
				direction *= -1;
				if(i > 30) {
					frame.setLocation(p);
					cancel();
				}
			}
			
		}, 0, 17);
	}
	
	public static void dauMode(JFrame frame) {
		frame.setTitle("You'll never catch me");
		int ans = JOptionPane.showConfirmDialog(frame.getContentPane(), "DAU-Exception: DAU in Front of Screen detected");
		if(ans == JOptionPane.NO_OPTION) {
			JOptionPane.showMessageDialog(frame.getContentPane(), "Doch");
			dauMode(frame);
		} else if(ans == JOptionPane.CANCEL_OPTION)
			System.exit(-1);
		else {
			moveWindow(frame);
		}
}

public static void moveWindow(final JFrame frame) {
	
	noShaked++;
	ses.scheduleAtFixedRate(new Runnable() {
		int[] awSize = jleu.ui.UI.getActualWindowSize(frame);
		Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = sc.width,
			screenHeight = sc.height,
			moverX = 10,
			moverY = 10;
		boolean inverted = false,
				invertedY = false;
		public void run() {
				Point pPi = MouseInfo.getPointerInfo().getLocation();
				Point pLoc = frame.getLocation();
				//xPos Invertion
				if(pLoc.x + awSize[0] < screenWidth && !inverted) {
					moverX = 10;
				}
				else if(pPi.x - 10 - awSize[0] > 0 && inverted) {
					inverted = true;
					moverX = - (awSize[0] + 10);
				}
				else {
					inverted = !inverted;
				}
				
				if(pLoc.y + awSize[1] < screenHeight && !invertedY) {
					moverY = 10;
				}
				else if(pPi.y - 10 - awSize[1] > 0 && invertedY) {
					moverY = - (awSize[1] + 10);
				}
				else {
					invertedY = !invertedY;
				}
				boolean xClose = pLoc.x - pPi.x >= - awSize[0] /2 && pLoc.x - pPi.x <= 10 ||
						   pPi.x - pLoc.x - awSize[0] + 10 >= - awSize[0] /2 && pPi.x - pLoc.x - awSize[0] + 10 <= 10,
						   
						   yClose = pLoc.y - pPi.y >= - awSize[1]/ 2 && pLoc.y - pPi.y <= 10 || 
								   pPi.y -pLoc.y - awSize[1] + 10 >= - awSize[1] / 2 && pPi.y - pLoc.y - awSize[1] + 10 <= 10;
				if(xClose && yClose) {
					frame.setLocation(pPi.x + moverX,pPi.y + moverY);
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}, 0, 10, TimeUnit.MILLISECONDS);
}

}
