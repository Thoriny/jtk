/****************************************************************************
Copyright (c) 2008, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is 
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package edu.mines.jtk.sgl;

import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Iterator;

import static edu.mines.jtk.ogl.Gl.*;
import edu.mines.jtk.awt.ColorMap;
import edu.mines.jtk.awt.ColorMapListener;
import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.util.*;

/**
 * A group of image panels that displays two 3D arrays of floats.
 * Specifically, an image panel group contains one or more axis-aligned 
 * frames, each containing two axis-aligned image panel children. 
 * <p>
 * The two sets of image panels are called "1st and 2nd image panels." The
 * 1st image panels are rendered (with polygon offset) slightly behind the 
 * 2nd image panels.
 * <p>
 * After constructing an image panel group, but before its image panels are 
 * drawn, one should set clips or percentiles. Otherwise, as each image panel 
 * is drawn for the first time, it will compute clip min and max values using 
 * default percentiles. Because all image panels in this group display the 
 * same array, much of this computation is redundant.
 *
 * @author Dave Hale, Colorado School of Mines
 * @version 2008.08.24
 */
public class ImagePanelGroup2 extends Group {

  /**
   * Constructs an image panel group for all three axes.
   * Both 3D arrays of floats much be consistent with the specified sampling.
   * @param sx sampling of the X axis.
   * @param sy sampling of the Y axis.
   * @param sz sampling of the Z axis.
   * @param f1 1st 3D array of floats.
   * @param f2 2nd 3D array of floats.
   */
  public ImagePanelGroup2(
    Sampling sx, Sampling sy, Sampling sz, 
    Float3 f1, Float3 f2) 
  {
    this(sx,sy,sz,f1,f2,new Axis[]{Axis.X,Axis.Y,Axis.Z});
  }

  /**
   * Constructs an image panel group for specified axes.
   * Both 3D arrays of floats much be consistent with the specified sampling.
   * @param sx sampling of the X axis.
   * @param sy sampling of the Y axis.
   * @param sz sampling of the Z axis.
   * @param f1 1st 3D array of floats.
   * @param f2 2nd 3D array of floats.
   * @param axes array of axes, one for each pair of image panels.
   */
  public ImagePanelGroup2(
    Sampling sx, Sampling sy, Sampling sz, 
    Float3 f1, Float3 f2, Axis[] axes) 
  {
    _clips1 = new Clips(f1);
    _clips2 = new Clips(f2);
    addPanels(sx,sy,sz,f1,f2,axes);
  }

  /**
   * Gets a 1st image panel in this group with the specified axis.
   * @param axis the axis.
   * @return the image panel; null, if none has the axis specified.
   */
  public ImagePanel getImagePanel1(Axis axis) {
    for (ImagePanel ip:_ip1List) {
      if (axis==ip.getFrame().getAxis())
        return ip;
    }
    return null;
  }

  /**
   * Gets a 2nd image panel in this group with the specified axis.
   * @param axis the axis.
   * @return the image panel; null, if none has the axis specified.
   */
  public ImagePanel getImagePanel2(Axis axis) {
    for (ImagePanel ip:_ip2List) {
      if (axis==ip.getFrame().getAxis())
        return ip;
    }
    return null;
  }

  /**
   * Gets an iterator for 1st image panels in this group.
   * @return the iterator.
   */
  public Iterator<ImagePanel> getImagePanels1() {
    return _ip1List.iterator();
  }

  /**
   * Gets an iterator for 2nd image panels in this group.
   * @return the iterator.
   */
  public Iterator<ImagePanel> getImagePanels2() {
    return _ip2List.iterator();
  }

  /**
   * Sets the index color model for 1st image panels in this group.
   * The default color model is a black-to-white gray model.
   * @param colorModel the index color model.
   */
  public void setColorModel1(IndexColorModel colorModel) {
    _colorMap1.setColorModel(colorModel);
    for (ImagePanel ip:_ip1List)
      ip.setColorModel(colorModel);
  }

  /**
   * Sets the index color model for 2nd image panels in this group.
   * The default color model is a jet model with alpha = 0.5.
   * @param colorModel the index color model.
   */
  public void setColorModel2(IndexColorModel colorModel) {
    _colorMap2.setColorModel(colorModel);
    for (ImagePanel ip:_ip2List)
      ip.setColorModel(colorModel);
  }

  /**
   * Gets the index color model for 1st image panels in this group.
   * @return the index color model.
   */
  public IndexColorModel getColorModel1() {
    return _colorMap1.getColorModel();
  }

  /**
   * Gets the index color model for 2nd image panels in this group.
   * @return the index color model.
   */
  public IndexColorModel getColorModel2() {
    return _colorMap2.getColorModel();
  }

  /**
   * Sets the clips for 1st image panels in this group. Image panels in 
   * this group map array values to bytes, which are then used as indices 
   * into a specified color model. This mapping from array values to byte 
   * indices is linear, and so depends on only these two clip values. The 
   * clip minimum value corresponds to byte index 0, and the clip maximum 
   * value corresponds to byte index 255. Sample values outside of the 
   * range [clipMin,clipMax] are clipped to lie inside this range.
   * <p>
   * Calling this method disables the computation of clips from percentiles.
   * Any clip values computed or specified previously will be forgotten.
   * @param clipMin the sample value corresponding to color model index 0.
   * @param clipMax the sample value corresponding to color model index 255.
   */
  public void setClips1(double clipMin, double clipMax) {
    _clips1.setClips(clipMin,clipMax);
    clipMin = _clips1.getClipMin();
    clipMax = _clips1.getClipMax();
    for (ImagePanel ip:_ip1List)
      ip.setClips(clipMin,clipMax);
    _colorMap1.setValueRange(clipMin,clipMax);
  }

  /**
   * Sets the clips for 2nd image panels in this group. Image panels in 
   * this group map array values to bytes, which are then used as indices 
   * into a specified color model. This mapping from array values to byte 
   * indices is linear, and so depends on only these two clip values. The 
   * clip minimum value corresponds to byte index 0, and the clip maximum 
   * value corresponds to byte index 255. Sample values outside of the 
   * range [clipMin,clipMax] are clipped to lie inside this range.
   * <p>
   * Calling this method disables the computation of clips from percentiles.
   * Any clip values computed or specified previously will be forgotten.
   * @param clipMin the sample value corresponding to color model index 0.
   * @param clipMax the sample value corresponding to color model index 255.
   */
  public void setClips2(double clipMin, double clipMax) {
    _clips2.setClips(clipMin,clipMax);
    clipMin = _clips2.getClipMin();
    clipMax = _clips2.getClipMax();
    for (ImagePanel ip:_ip2List)
      ip.setClips(clipMin,clipMax);
    _colorMap2.setValueRange(clipMin,clipMax);
  }

  /**
   * Gets the minimum clip value for 1st image panels.
   * @return the minimum clip value.
   */
  public float getClip1Min() {
    return _clips1.getClipMin();
  }

  /**
   * Gets the minimum clip value for 2nd image panels.
   * @return the minimum clip value.
   */
  public float getClip2Min() {
    return _clips2.getClipMin();
  }

  /**
   * Gets the maximum clip value for 1st image panels.
   * @return the maximum clip value.
   */
  public float getClip1Max() {
    return _clips1.getClipMax();
  }

  /**
   * Gets the maximum clip value for 2nd image panels.
   * @return the maximum clip value.
   */
  public float getClip2Max() {
    return _clips2.getClipMax();
  }

  /**
   * Sets the percentiles used to compute clips for 1st image panels in
   * this group. The default percentiles are 0 and 100, which correspond 
   * to the minimum and maximum array values.
   * <p>
   * Calling this method enables the computation of clips from percentiles.
   * Any clip values specified or computed previously will be forgotten.
   * @param percMin the percentile corresponding to clipMin.
   * @param percMax the percentile corresponding to clipMax.
   */
  public void setPercentiles1(double percMin, double percMax) {
    _clips1.setPercentiles(percMin,percMax);
    double clipMin = _clips1.getClipMin();
    double clipMax = _clips1.getClipMax();
    //System.out.println("clip min="+clipMin+" max="+clipMax);
    for (ImagePanel ip:_ip1List)
      ip.setClips(clipMin,clipMax);
    _colorMap1.setValueRange(clipMin,clipMax);
  }

  /**
   * Sets the percentiles used to compute clips for 2nd image panels in
   * this group. The default percentiles are 0 and 100, which correspond 
   * to the minimum and maximum array values.
   * <p>
   * Calling this method enables the computation of clips from percentiles.
   * Any clip values specified or computed previously will be forgotten.
   * @param percMin the percentile corresponding to clipMin.
   * @param percMax the percentile corresponding to clipMax.
   */
  public void setPercentiles2(double percMin, double percMax) {
    _clips2.setPercentiles(percMin,percMax);
    double clipMin = _clips2.getClipMin();
    double clipMax = _clips2.getClipMax();
    //System.out.println("clip min="+clipMin+" max="+clipMax);
    for (ImagePanel ip:_ip2List)
      ip.setClips(clipMin,clipMax);
    _colorMap2.setValueRange(clipMin,clipMax);
  }

  /**
   * Gets the minimum percentile for 1st image panels.
   * @return the minimum percentile.
   */
  public float getPercentile1Min() {
    return _clips1.getPercentileMin();
  }

  /**
   * Gets the minimum percentile for 2nd image panels.
   * @return the minimum percentile.
   */
  public float getPercentile2Min() {
    return _clips2.getPercentileMin();
  }

  /**
   * Gets the maximum percentile for 1st image panels.
   * @return the maximum percentile.
   */
  public float getPercentile1Max() {
    return _clips1.getPercentileMax();
  }

  /**
   * Gets the maximum percentile for 2nd image panels.
   * @return the maximum percentile.
   */
  public float getPercentile2Max() {
    return _clips2.getPercentileMax();
  }

  /**
   * Adds the specified color map listener for 1st image panels.
   * @param cml the listener.
   */
  public void addColorMap1Listener(ColorMapListener cml) {
    _colorMap1.addListener(cml);
  }

  /**
   * Adds the specified color map listener for 2nd image panels.
   * @param cml the listener.
   */
  public void addColorMap2Listener(ColorMapListener cml) {
    _colorMap2.addListener(cml);
  }

  /**
   * Removes the specified color map listener.
   * @param cml the listener.
   */
  public void removeColorMap1Listener(ColorMapListener cml) {
    _colorMap1.removeListener(cml);
  }

  /**
   * Removes the specified color map listener.
   * @param cml the listener.
   */
  public void removeColorMap2Listener(ColorMapListener cml) {
    _colorMap2.removeListener(cml);
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  // List of 1st image panels.
  private ArrayList<ImagePanel> _ip1List;
  private ArrayList<ImagePanel> _ip2List;

  // Clips.
  Clips _clips1;
  Clips _clips2;

  // Color maps.
  private ColorMap _colorMap1 = new ColorMap(0.0,1.0,ColorMap.GRAY);
  private ColorMap _colorMap2 = new ColorMap(0.0,1.0,ColorMap.getJet(0.5f));

  private static void checkSampling(
    Sampling sx, Sampling sy, Sampling sz, Float3 f1, Float3 f2) 
  {
    Check.argument(f1.getN1()==sz.getCount(),
                  "f1.getN1()==sz.getCount()");
    Check.argument(f1.getN2()==sy.getCount(),
                  "f1.getN2()==sy.getCount()");
    Check.argument(f1.getN3()==sx.getCount(),
                  "f1.getN3()==sx.getCount()");
    Check.argument(f2.getN1()==sz.getCount(),
                  "f2.getN1()==sz.getCount()");
    Check.argument(f2.getN2()==sy.getCount(),
                  "f2.getN2()==sy.getCount()");
    Check.argument(f2.getN3()==sx.getCount(),
                  "f2.getN3()==sx.getCount()");
  }

  private void addPanels(
    Sampling sx, Sampling sy, Sampling sz, Float3 f1, Float3 f2, Axis[] axes) 
  {
    checkSampling(sx,sy,sz,f1,f2);
    int nx = sx.getCount();
    int ny = sy.getCount();
    int nz = sz.getCount();
    double dx = sx.getDelta();
    double dy = sy.getDelta();
    double dz = sz.getDelta();
    double fx = sx.getFirst();
    double fy = sy.getFirst();
    double fz = sz.getFirst();
    double lx = fx+(nx-1)*dx;
    double ly = fy+(ny-1)*dy;
    double lz = fz+(nz-1)*dz;
    Point3 qmin = new Point3(fx,fy,fz);
    Point3 qmax = new Point3(lx,ly,lz);
    int np = axes.length;
    _ip1List = new ArrayList<ImagePanel>(np);
    _ip2List = new ArrayList<ImagePanel>(np);
    PolygonState ps1 = new PolygonState();
    ps1.setPolygonOffset(1.0f,1.0f);
    ps1.setPolygonOffsetFill(true);
    StateSet s1 = new StateSet();
    s1.add(ps1);
    BlendState bs2 = new BlendState();
    bs2.setFunction(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
    StateSet s2 = new StateSet();
    s2.add(bs2);
    for (int jp=0; jp<np; ++jp) {
      AxisAlignedQuad aaq = new AxisAlignedQuad(axes[jp],qmin,qmax);
      AxisAlignedFrame aaf = aaq.getFrame();
      ImagePanel ip1 = new ImagePanel(sx,sy,sz,f1);
      ImagePanel ip2 = new ImagePanel(sx,sy,sz,f2);
      ip1.setStates(s1);
      ip2.setStates(s2);
      ip1.setColorModel(getColorModel1());
      ip2.setColorModel(getColorModel2());
      aaf.addChild(ip1);
      aaf.addChild(ip2);
      this.addChild(aaq);
      _ip1List.add(ip1);
      _ip2List.add(ip2);
    }
  }
}