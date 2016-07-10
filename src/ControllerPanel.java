import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javazoom.jl.decoder.JavaLayerException;


/**
 * controller panel
 * @author haiyun
 *
 */
public class ControllerPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  
  private JButton prevButton;
  private JButton playButton;
  private JButton nextButton;
  private JLabel playedTimeLabel;
  private JLabel totalTimeLabel;
  private JSlider slider;
  private MediaPlayer player;
  private LyricPanel lyricPanel;
  private TimerThread timerThread;
  private ChangeListener sliderChangeListener;
  
  public static boolean paused = true;
  public static boolean toggled = false;
  
  public ControllerPanel(MediaPlayer player,LyricPanel lyricPanel) {
    this.setBackground(Global.DARK_WHITE);
    this.setPreferredSize(Global.CONTROLLER_PANEL_SIZE);
    this.player = player;
    this.lyricPanel = lyricPanel;    
    
    this.playButton = new RoundButton("", 60, 50, 22, ButtonType.PLAY);
    this.playButton.setBackground(Global.DEEP_BLUE);
    // this.playButton.setPreferredSize(new Dimension(50, 50));
    
    this.prevButton = new RoundButton("", 60, 50, 18, ButtonType.PREV);
    this.prevButton.setBackground(Global.DEEP_BLUE);
    
    this.nextButton = new RoundButton("", 60, 50, 18, ButtonType.NEXT);
    this.nextButton.setBackground(Global.DEEP_BLUE);

    this.playButton.setFont(Global.Helvetica);
    this.prevButton.setFont(Global.Helvetica);
    this.nextButton.setFont(Global.Helvetica);
    
    this.slider = new JSlider();
    this.slider.setValue(0);
    this.slider.setPreferredSize(new Dimension(700, 50));
    
    this.playedTimeLabel = new JLabel("00:00");
    this.playedTimeLabel.setFont(Global.Helvetica);
    this.playedTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
    
    this.totalTimeLabel = new JLabel("00:00");
    this.totalTimeLabel.setFont(Global.Helvetica);
    this.totalTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
    // GridLayout layout = new GridLayout(1, 50);
    // BoxLayout layout = new BoxLayout();
    // this.setLayout(layout);
    this.add(prevButton);
    this.add(playButton);
    this.add(nextButton);
    this.add(playedTimeLabel);
    this.add(slider);
    this.add(totalTimeLabel);
        
    this.sliderChangeListener = new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        int value = slider.getValue();
        playedTimeLabel.setText(String.format("%02d:%02d", value / 60, value % 60));
        if (!slider.getValueIsAdjusting()) {
          // System.out.println("curValue: " + slider.getValue());
        }
      }
   };
    
    this.playButtonAddAction();
    this.sliderAddMouseAction();    
  }
  
  public void updateSliderAndTotalTimeLabel() {
    int duration = (int) player.audioProperty.getDuration() / 1000000;
    int duraMin = (int) duration / 60;
    int duraSec = (int) duration % 60;
    totalTimeLabel.setText(String.format("%02d:%02d", duraMin, duraSec));
    slider.setMaximum(duration);
  }
    
  public void sliderAddMouseAction() {
    this.slider.addMouseListener(new MouseListener() {

      @Override
      public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        ControllerPanel.toggled = true;
        slider.addChangeListener(sliderChangeListener);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        ControllerPanel.toggled = false;
        slider.removeChangeListener(sliderChangeListener);
        int newValue = slider.getValue();
        int newCurFrame = (int) (newValue * 1.0 / slider.getMaximum() * player.getTotalFrames());
        try {
          player.setPosition(newCurFrame);
          player.setCurrentFrame(newCurFrame);
        } catch (JavaLayerException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        
        if (!paused) {
          timerThread = new TimerThread(player, slider, playedTimeLabel,lyricPanel);
          timerThread.start();
          try {
            player.play();
          } catch (JavaLayerException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        }
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        
      }
    });
  }
  
  public void playButtonAddAction() {
    this.playButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (player != null && player.getAudioPath() != null) {
          if (paused) {
            paused = false;
            try {
              // System.out.println("Frame: " + player.getCurrentFrame());
              timerThread = new TimerThread(player, slider, playedTimeLabel, lyricPanel);
              timerThread.start();
              player.play();
            } catch (Exception e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            }
          } else {
            paused = true;
            try {
              System.out.println("pause");
              player.pause();
              playButton.repaint();
            } catch (Exception e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            }
          }
        }
      }
    });
  }
}

class TimerThread extends Thread {
  MediaPlayer player;
  JSlider slider;
  JLabel playedTimeLabel;
  LyricPanel lyricPanel;
  
  public TimerThread(MediaPlayer player, JSlider slider, JLabel playedTimeLabel,LyricPanel lyricPanel) {
    this.player = player;
    this.slider = slider;
    this.playedTimeLabel = playedTimeLabel;
    this.lyricPanel  = lyricPanel;
  }
  
  @Override
  public void run() {
    while (!ControllerPanel.paused && !ControllerPanel.toggled) {
      int curFrame = player.getCurrentFrame();
      int totalFrame = player.getTotalFrames();
      if (totalFrame >= curFrame && curFrame >= 0) {
        int value = (int) ((curFrame + 1) * 1.0 / totalFrame * slider.getMaximum());
        this.slider.setValue(value);
        this.playedTimeLabel.setText(String.format("%02d:%02d", value / 60, value % 60));
        this.lyricPanel.repaint(value);
        try {
          Thread.sleep(30);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } 
//      // Attemp to implement cycle, failed.
//      else {
//    	  start();
//          try {
//			player.setPosition(1);
//	        player.setCurrentFrame(1);
//			player.play();
//          } catch (JavaLayerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//      }
//      //////
    }
  }
}
