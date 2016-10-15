package edu.valelab.gaussianfit;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.text.TextPanel;
import ij.text.TextWindow;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Timer;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.display.DisplayWindow;

/**
 *
 * @author nico
 */
/**
 * KeyListener and MouseListenerclass for ResultsTable When user selected a line
 * in the ResulsTable and presses a key, the corresponding image will move to
 * the correct slice and draw the ROI that was used to calculate the Gaussian
 * fit Works only in conjunction with appropriate column names Up and down keys
 * also work as expected
 */
public class ResultsTableListener implements KeyListener, MouseListener {

   private final ImagePlus siPlus_;
   private final ResultsTable res_;
   private final TextWindow win_;
   private final TextPanel tp_;
   private final DisplayWindow dw_;
   private final Studio studio_;
   private final int hBS_;
   private final Timer t_;
   private int key_;
   private int row_;

   public ResultsTableListener(Studio studio, DisplayWindow dw, ImagePlus siPlus, 
           ResultsTable res, TextWindow win, int halfBoxSize) {
      studio_ = studio;
      dw_ = dw;
      siPlus_ = siPlus;
      res_ = res;
      win_ = win;
      tp_ = win.getTextPanel();
      hBS_ = halfBoxSize;
      t_ = new Timer(200, new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent ae) {
            row_ = tp_.getSelectionStart();
            if (key_ == KeyEvent.VK_J) {
               if (row_ > 0) {
                  row_--;
                  tp_.setSelection(row_, row_);
               }
            } else if (key_ == KeyEvent.VK_K) {
               if (row_ < tp_.getLineCount() - 1) {
                  row_++;
                  tp_.setSelection(row_, row_);
               }
            }
            update();
         }
      });

   }
   

   @Override
   public void keyPressed(KeyEvent e) {
      key_ = e.getKeyCode();
     // t_.start();
   }

   @Override
   public void keyReleased(KeyEvent e) {
      t_.stop();
   }

   @Override
   public void keyTyped(KeyEvent e) {
      //t_.stop();
   }

   @Override
   public void mouseReleased(MouseEvent e) {
      update();
   }

   @Override
   public void mousePressed(MouseEvent e) {
   }

   @Override
   public void mouseClicked(MouseEvent e) {
   }

   @Override
   public void mouseEntered(MouseEvent e) {
   }

   ;
   @Override
   public void mouseExited(MouseEvent e) {
   }

   ;

   private void update() {
      if (siPlus_ == null && dw_ == null) {
         return;
      }
      int row = tp_.getSelectionStart();
      if (row >= 0 && row < tp_.getLineCount()) {
         if (siPlus_.getWindow() != null) {
            if (siPlus_ != IJ.getImage()) {
               siPlus_.getWindow().toFront();
               win_.toFront();
            }
         } else {
            return;
         }
         int frame = (int) res_.getValue(Terms.FRAME, row);
         int slice = (int) res_.getValue(Terms.SLICE, row);
         int channel = (int) res_.getValue(Terms.CHANNEL, row);
         int pos = (int) res_.getValue(Terms.POSITION, row);
         int x = (int) res_.getValue(Terms.XPIX, row);
         int y = (int) res_.getValue(Terms.YPIX, row);
         
         if (dw_ != null) {
            Coords.CoordsBuilder builder = studio_.data().getCoordsBuilder();
            Coords coords = builder.channel(channel - 1).time(frame - 1).
                    z(slice - 1).stagePosition(pos - 1).build();
            dw_.setDisplayedImageTo(coords);
         } else if (siPlus_.isHyperStack()) {
            siPlus_.setPosition(channel, slice, frame);
         } else {
            siPlus_.setPosition(Math.max(frame, slice));
         }
         siPlus_.setRoi(new Roi(x - hBS_, y - hBS_, 2 * hBS_, 2 * hBS_));
      }
   }
}
