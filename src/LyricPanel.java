import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javazoom.jl.decoder.JavaLayerException;


public class LyricPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private JPanel lyricPanel;
  private JPanel imgPanel;
  private JLabel lyricBox;
  private JLabel imgLabel ;
  private MediaPlayer player;
  private boolean useDefaultLyrics = false;
  private boolean useDefaultImg   = false;
  private boolean lrcLoaded = false;
  private boolean imgLoaded = false;
  private float[] opacity = new float[11];
  private int durationCentisec;
		  
  /** Time stored in centisecond */
  private int[] timeLinesArray;
  /** Lyric corresponds to timelinesArray index*/
  private String[] lrcLinesArray;

  
  public LyricPanel(MediaPlayer player) {
	  this.player = player; 
	  // this.setSize(864, 100);
	  this.setPreferredSize(Global.LYRIC_PANEL_SIZE);
	  this.setBackground(Global.DARK_WHITE);
	  
  	  this.setLayout(new OverlayLayout(this));

	  this.lyricPanel = new JPanel();
	  this.lyricPanel.setLayout(null);
	  lyricPanel.setOpaque(false);
	  
	  this.imgPanel = new JPanel();
//	  this.imgPanel.setLocation(100, 200);
//	  this.imgPanel.setBounds(0, 0, this.getWidth(), this.getHeight());
//	  this.imgPanel.setLayout(null);	 
//	  imgPanel.setOpaque(false);
	  

//	  this.lyricPanel.setBounds(0, 0, this.getWidth(),this.getHeight());
	  
	  this.lyricBox = new JLabel("Lyric Box");
//	  this.lyricBox.setFont(Global.Helvetica);
	  this.lyricBox.setFont(new Font("Helvetica", Font.PLAIN, 20));

	  this.lyricBox.setHorizontalAlignment(SwingConstants.CENTER);
	  lyricBox.setText("Hahaha");
	  this.lyricPanel.add(lyricBox);


	  {
	  opacity[0] = (float) 0.1 ; 
	  opacity[1] = (float) 0.2 ; 
	  opacity[2] = (float) 0.4 ; 
	  opacity[3] = (float) 0.6 ; 
	  opacity[4] = (float) 0.8 ; 
	  	opacity[5] = (float) 1.0 ; 
	  opacity[6] = (float) 0.8 ; 
	  opacity[7] = (float) 0.6 ; 
	  opacity[8] = (float) 0.4 ; 
	  opacity[9] = (float) 0.2 ; 
	  opacity[10] = (float) 0.1 ; 
	  }
	
	  // Set up img label
	  imgLabel = new JLabel();
	  this.imgPanel.add(imgLabel);
//	  this.imgLabel.setBounds(15,20, this.imgPanel.getWidth()-30, this.imgPanel.getHeight()-40);

	  this.add(lyricPanel);
	  this.add(imgPanel);
	  
  }

  
  /**
   *  Load background image, if none exists, don't use, for now
   */
  void loadImage() {
	  String songName = player.getAudioPath();
	  String imgName = songName.substring(0, songName.length()-3)+"jpg";
	  
	  BufferedImage backgroudImg;
	  try {
		  backgroudImg = ImageIO.read(new File(imgName));
//		  imgLabel.setIcon(new ImageIcon(backgroudImg));
//		  imgLabel.setBackground(new Color(0x000000));
//		  imgLabel.setForeground(new Color(0xffffff));
		  imgLabel.setIcon(new ImageIcon(resizeImg(backgroudImg)));

		 
	  } catch (IOException e) {
		  try {
			  backgroudImg = ImageIO.read(new File("./music/background.jpg"));
//			  imgLabel.setIcon(new ImageIcon(backgroudImg));
			  imgLabel.setIcon(new ImageIcon(resizeImg(backgroudImg)));
			  useDefaultImg = true;
		  } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		  }

	  } finally {
		  imgLoaded = true;		  
	  }
  }
  
  
  /**
   * Return a resized image that match the size of the lyric panel
   * @param BufferedImage backgroudImg
   * @return BufferedImage resizedImg
   */
  private BufferedImage resizeImg(BufferedImage backgroudImg) {
	  ImageIcon originImg;
	  // Resize the image  
//	  http://stackoverflow.com/questions/6714045/how-to-resize-jlabel-imageicon
	  int width = imgPanel.getWidth();
	  int height = imgPanel.getHeight();
	  originImg = new ImageIcon(backgroudImg);
	  // 1. Create a new Buffered Image and Graphic2D object
	  BufferedImage resizedImg = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
	  Graphics2D g2 = resizedImg.createGraphics();	
	  
	  // 2. Use the Graphic object to draw a new image to the image in the buffer
	  g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	  g2.drawImage(backgroudImg, 0, 0, width, height, null);
	  
	  g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 0.1f));
	  
	  g2.dispose();
	  // 3. Convert the buffered image into an ImageIcon for Label
	  return resizedImg;
}


/**
   * Load lyrics to timeLineArray and lrcLineArray
   */
  void loadLrcFile(){
	  // Get the total duration in Centisec
	  durationCentisec = (int) player.audioProperty.getDuration()/10000;
	  
	  // Get the lrc file, currently only support lrc with same name
	  String songName = player.getAudioPath();
//	  String songName = "./music/王菲-匆匆那年.mp3";
//	  System.out.println("songname"+songName);
	  String lrcName = songName.substring(0, songName.length()-3)+"lrc";
	  ArrayList<Integer> timeLines = new ArrayList<Integer>();
	  ArrayList<String> lrcLines = new ArrayList<String>();
	  try {
		BufferedReader reader = new BufferedReader(new FileReader(lrcName));
		String line;
//		 example
//		 [03:16.43]就像那年匆促
		while ((line = reader.readLine()) != null) {
			// remove any leading and trailing white space
			line = line.trim();
			if ( line.substring(0, 1).equals("[")){
				String timeStr = line.substring(0,10).substring(1, 9);
				int minute = Integer.parseInt(timeStr.substring(0, 2));
				int second = Integer.parseInt(timeStr.substring(3, 5));
				int centisec = Integer.parseInt(timeStr.substring(6,8));
				int time = (minute * 60 + second ) * 100 + centisec;
				timeLines.add(time);
				lrcLines.add(line.substring(10));
			}
		}
		reader.close();
		timeLines.add(durationCentisec+1);
		lrcLines.add("");
		// Copy lyrics to array for quicker access
		lrcLinesArray = new String[lrcLines.size()];
		timeLinesArray = new int[lrcLines.size()];
		for ( int i = lrcLinesArray.length-1 ; i > -1 ; i-- ){
			lrcLinesArray[i]  = lrcLines.remove(lrcLines.size()-1);
			timeLinesArray[i] = timeLines.remove(timeLines.size()-1);
		}
	  } catch (FileNotFoundException e) {
		// Use default lrc
		useDefaultLyrics = true;		  
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	  lrcLoaded = true;
	  	  
  }
  
  /**
   *  Repaint the lyric, called by TimerThread in ControllerPanel.
   * @param currentSecond
   */
  void repaint(int currentSecond){
	  if ( ! imgLoaded ) loadImage();
	  if ( ! lrcLoaded ) loadLrcFile();
	  
	  if (!useDefaultLyrics ){
		  int curFrame = this.player.getCurrentFrame();
		  int totalFrame = this.player.getTotalFrames();
//		  System.out.println("curFrame: " + curFrame);
//		  System.out.println("TotalFrame: " + totalFrame);
		  int currentCentisec = (int) ( curFrame * 1.0 / totalFrame * durationCentisec);
//		  System.out.println("currentCentisec: " + currentCentisec);
		  
		  
//		  if (totalFrame >= curFrame && curFrame >= 0) { }

		  // find the largest number in timeLineArray that is smaller than currentMillisec
		  int currentLine = 0;
		  for (int i = 0 ; i < timeLinesArray.length; i++ ){
			  if (timeLinesArray[i] > currentCentisec )   break;
			  currentLine = i;
		  }
		  
		  
		  String output = "<html>";
		  for (int i = - 5 ; i <  6 ; i ++ )
			  if ( currentLine + i >= 0 && currentLine + i <lrcLinesArray.length )	{
				  if ( i == 0) {
//					  output += String.format("<center><font color=purple style=\"font-size:40px;\">%s</font></center><br/>",lrcLinesArray[currentLine + i]);
					  output += String.format("<center><font color=\"#ff22ff\" style=\"font-size:40px;\">%s</font></center><br/>",lrcLinesArray[currentLine + i]);
//					  output += String.format("<center><div style=\"color: rgba(0, 0, 0, .5)\"> This text color is black, but opacity of 0.5 makes it look grey.</div></center>");
					  
				  } else{
					  output += String.format("<center><font color=red style=\"opacity:%.1f;\">%s</font></center><br/>", opacity[i+5],lrcLinesArray[currentLine + i]);
//					  output += String.format("<center><div style=\"color: rgba(255, 0, 0, :%.1f)\">%s</div></center><br/>", opacity[i+5],lrcLinesArray[currentLine + i]);
//					  output += String.format("<center><font color=red ><font style=\"opacity:%.1f\">%s</font></font></center><br/>", opacity[i+5],lrcLinesArray[currentLine + i]);
					  
				  }
			  } else {
				  output += String.format("<center></center><br />");
			  }
//		  output += "<font color=red> Test </font>";
		  output += "</html>";
//		  System.out.println("String " + output );
		  
		  this.lyricBox.setText(output);
		  
		  // TODO Set the position of lyric box to let it float
		  int timeDisplay = timeLinesArray[currentLine+1] - timeLinesArray[currentLine];
		  int timePass = currentCentisec - timeLinesArray[currentLine];
		  int pxAdjust =(int) ( ( (double) timePass/timeDisplay - 0.5 )*40 );
		  
		  this.lyricBox.setBounds(15, 20 - pxAdjust  ,this.getWidth()-30,this.getHeight() - 30 );
		  
//		  this.lyricBox.setText(lrcLinesArray[currentLine]);
//		  lyricBox.setBounds(0, 0, 216, 400);
		  
	  }
  }
}
