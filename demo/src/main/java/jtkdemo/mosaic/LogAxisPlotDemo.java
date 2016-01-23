package jtkdemo.mosaic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import edu.mines.jtk.mosaic.PlotFrame;
import edu.mines.jtk.mosaic.PlotPanel;
import edu.mines.jtk.mosaic.PointsView;
import edu.mines.jtk.mosaic.Scale;
import edu.mines.jtk.mosaic.Tile;
import edu.mines.jtk.util.ArrayMath;
import static edu.mines.jtk.util.MathPlus.*;

public class LogAxisPlotDemo {
  public static void main(String args[]) {

    System.out.println("LogAxisPlotDemo");
    int n = 1000;
    float X = 300;

    float[] x1 = new float[n];
    float[] x2 = new float[n];
    x1 = ArrayMath.rampfloat(0.0f,X / n,n);
    x2 = ArrayMath.rampfloat(0.0f,X / n,n);

    float[] f1 = new float[n];
    float[] f2 = new float[n];
    for (int i = 0; i < n; ++i) {
      f1[i] = pow(1.5f * x1[i],1);
      f2[i] = 100 * (float) sin(0.1 * x2[i]);
    }

    // new plot
    PlotPanel plot = new PlotPanel(2,2);

    // plain old linear plots
    PointsView pv1 = plot.addPoints(0,1,x1,f1);
    PointsView pv2 = plot.addPoints(0,1,x2,f2);
    pv1.setLineColor(Color.BLUE);
    pv2.setLineColor(Color.RED);

    // log-x plots
    PointsView pv3 = plot.addPoints(0,0,x1,f1);
    PointsView pv4 = plot.addPoints(0,0,x2,f2);
    pv3.setHScale(Scale.LOG);
    pv3.setLineColor(Color.BLUE);
    pv4.setLineColor(Color.RED);

    // log-y plots
    PointsView pv5 = plot.addPoints(1,1,x1,f1);
    PointsView pv6 = plot.addPoints(1,1,x2,f2);
    pv5.setVScale(Scale.LOG);
    pv5.setLineColor(Color.BLUE);
    pv6.setLineColor(Color.RED);

    // need to update the PlotPanel
    // log-log plots
    PointsView pv7 = plot.addPoints(1,0,x1,f1);
    PointsView pv8 = plot.addPoints(1,0,x2,f2);
    pv7.setLineColor(Color.BLUE);
    pv8.setLineColor(Color.RED);

    // make some buttons
    JPanel buttPanel = new JPanel();
    buttPanel.add(changeHAxisButton(plot.getTile(0,0)));
    buttPanel.add(changeVAxisButton(plot.getTile(0,1)));
    buttPanel.add(changeHAxisButton(plot.getTile(0,1)));
    buttPanel.add(changeVAxisButton(plot.getTile(1,1)));

    // frame setup
    plot.setVisible(true);
    PlotFrame frame = new PlotFrame(plot);
    frame.add(buttPanel,BorderLayout.SOUTH);
    frame.setSize(500,500);
    frame.setDefaultCloseOperation(PlotFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);

    /*
     * // Projector log scale test // assuming safe input for now Projector p =
     * new Projector(0.1, 100, 0.0, 1.0); p.setScale(Scale.LINEAR);
     * System.out.println("p.u(0) = " + p.u(0.3));
     * System.out.println("p.u(50) = " + p.u(50));
     * System.out.println("p.u(100) = " + p.u(100)); p.setScale(Scale.LOG);
     * System.out.println("p.u(0.3) = " + p.u(0.3));
     * System.out.println("p.u(10) = " + p.u(10));
     * System.out.println("p.u(100) = " + p.u(100));
     * System.out.println("p.v(p.u(0.3)) = " + p.v(p.u(0.3)));
     * System.out.println("p.v(p.u(10)) = " + p.v(p.u(10)));
     * System.out.println("p.v(p.u(100)) = " + p.v(p.u(100)));
     */
  }

  public static JButton changeHAxisButton(final Tile tile) {
    JButton b = new JButton("Tile " + tile.getRowIndex() + ", "
        + tile.getColumnIndex() + " H");
    b.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (tile.getHScale() == Scale.LOG)
          tile.setHScale(Scale.LINEAR);
        else
          tile.setHScale(Scale.LOG);
      }
    });
    return b;
  }

  public static JButton changeVAxisButton(final Tile tile) {
    JButton b = new JButton("Tile " + tile.getRowIndex() + ", "
        + tile.getColumnIndex() + " V");
    b.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (tile.getVScale() == Scale.LOG)
          tile.setVScale(Scale.LINEAR);
        else
          tile.setVScale(Scale.LOG);
        tile.repaint();
      }
    });
    return b;
  }

  public static float f1(double x) {
    return (float) (4 * x * Math.sin(10 / x)) + 10.0f;
  }

  public static float f2(double x) {
    return (float) Math.exp(x / 20);
  }

  // generate an array of log-spaced data, i.e between 10^a and 10^b with even
  // steps in the exponent
  public static float[] logSpace(double a, double b, int n) {
    float[] y = new float[n];
    double de = (b - a) / n;
    for (int i = 0; i < n; i++)
      y[i] = (float) Math.pow(10,a + i * de);
    return y;
  }

  // generate an array of evenly spaced data
  public static float[] linSpace(double a, double b, int n) {
    float[] y = new float[n];
    double de = (b - a) / n;
    for (int i = 0; i < n; i++)
      y[i] = (float) (a + i * de);
    return y;
  }

}
