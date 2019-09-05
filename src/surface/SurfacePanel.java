package surface;
/*----------------------------------------------------------------------------------------*
 * SurfaceCanvas.java                                                                     *
 *                                                                                        *
 * Surface Plotter   version 1.10    14 Oct 1996                                          *
 *                   version 1.20     8 Nov 1996                                          *
 *                   version 1.30b1  17 May 1997                                          *
 *                   version 1.30b2  18 Oct 2001                                          *
 *					 version 1.4     05 Sep 2019 (zward, changed to JPanel)                                                                       
 *                                                                                        *
 * Copyright (c) Yanto Suryono <yanto@fedu.uec.ac.jp>                                     *
 *                                                                                        *
 * This program is free software; you can redistribute it and/or modify it                *
 * under the terms of the GNU General Public License as published by the                  *
 * Free Software Foundation; either version 2 of the License, or (at your option)         *
 * any later version.                                                                     *
 *                                                                                        *
 * This program is distributed in the hope that it will be useful, but                    *
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or          *
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for               *
 * more details.                                                                          *
 *                                                                                        *
 * You should have received a copy of the GNU General Public License along                *
 * with this program; if not, write to the Free Software Foundation, Inc.,                *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA                                  *
 *                                                                                        *
 *----------------------------------------------------------------------------------------*/

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.*;

/**
 * The class <code>SurfaceCanvas</code> is responsible
 * for the generation of surface images and user mouse events handling. 
 *
 * @author  Yanto Suryono
 * @author Zachary J. Ward  
 */
public class SurfacePanel extends JPanel
{

	private ISurfacePlotModel model;              // the model
	private Image bufferImage;                    // the backing buffer
	private Graphics bufferGraphics;               // the graphics context of backing buffer
	private boolean image_drawn;             // image drawn flag
	private Projector projector;             // the projector
	private Point3D[] vertex;        // vertices array
	private boolean data_available;          // data availability flag
	private boolean printing;                // printing flag
	private int curBufferWidth, curBufferHeight;       // canvas size
	private int printwidth, printheight;     // print size
	private float color;                     // color of surface
	private Point3D cop;               // center of projection
	// setting variables
	private int plot_mode;
	private int calc_divisions;
	private boolean isBoxed, isMesh, isScaleBox,
		isDisplayXY, isDisplayZ, isDisplayGrids;
	private float xmin, xmax, ymin;
	private float ymax, zmin, zmax;

	// constants
	private static final int TOP = 0;
	private static final int CENTER = 1;

	private int click_x, click_y;    // previous mouse cursor position
	
	/**
	 * The constructor of <code>SurfaceCanvas</code>
	 *
	 * @param model The model provides data about the surface
	 */
	public SurfacePanel()
	{
		super();
		projector = new Projector();
		projector.setDistance(70);
		projector.set2DScaling(15);
		projector.setRotationAngle(125);
		projector.setElevationAngle(10);
		Point3D.setProjector(projector);
		
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				click_x = e.getX();
				click_y = e.getY();
			}
		});
		
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				float new_value = 0.0f;
				int x=e.getX();
				int y=e.getY();

				if (e.isControlDown()){
					projector.set2D_xTranslation(
						projector.get2D_xTranslation() + (x - click_x));
					projector.set2D_yTranslation(
						projector.get2D_yTranslation() + (y - click_y));
				}
				else{
					if (e.isShiftDown()){
						new_value = projector.get2DScaling() + (y - click_y) * 0.5f;
						if (new_value > 60.0f){
							new_value = 60.0f;
						}
						if (new_value < 2.0f){
							new_value = 2.0f;
						}
						projector.set2DScaling(new_value);
					}
					else{
						new_value = projector.getRotationAngle() + (x - click_x);
						while (new_value > 360){
							new_value -= 360;
						}
						while (new_value < 0){
							new_value += 360;
						}
						projector.setRotationAngle(new_value);
						new_value = projector.getElevationAngle() + (y - click_y);
						if (new_value > 90){
							new_value = 90;
						}
						else{
							if (new_value < 0){
								new_value = 0;
							}
						}
						projector.setElevationAngle(new_value);
					}
				}
				image_drawn = false;
				repaint();
				click_x = x;
				click_y = y;
				
			}
		});
		
	}

	public void setModel(ISurfacePlotModel model)
	{
		this.model = model;
		plot_mode = model.getPlotMode();
		isBoxed = model.isBoxed();
		isMesh = model.isMesh();
		isScaleBox = model.isScaleBox();
		isDisplayXY = model.isDisplayXY();
		isDisplayZ = model.isDisplayZ();
		isDisplayGrids = model.isDisplayGrids();
		calc_divisions = model.getCalcDivisions();
		xAxisLabel = model.getXAxisLabel();
		yAxisLabel = model.getYAxisLabel();
		zAxisLabel = model.getZAxisLabel();
		bufferImage = null;
		bufferGraphics = null;
		image_drawn = false;
		data_available = false;
		printing = false;
		curBufferWidth = curBufferHeight = -1;
		if(bufferImage != null)
			bufferImage.flush();
		bufferImage = null;
		this.renderSurface();
	}

	protected String xAxisLabel = "X";

	protected String yAxisLabel = "Y";

	protected String zAxisLabel = "Z";

	/**
	 * Destroys the internal image. It will force <code>SurfaceCanvas</code>
	 * to regenerate all images when the <code>paint</code> method is called.
	 */
	public void destroyImage()
	{
		image_drawn = false;
	}

	/**
	 * Sets the x and y ranges of calculated surface vertices.
	 * The ranges will not affect surface appearance. They affect axes
	 * scale appearance.
	 *
	 * @param xmin the minimum x
	 * @param xmax the maximum x
	 * @param ymin the minimum y
	 * @param ymax the maximum y
	 */
	public void setRanges(float xmin, float xmax, float ymin, float ymax)
	{
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}

	/**
	 * Gets the current x, y, and z ranges.
	 *
	 * @return array of x,y, and z ranges in order of
	 *         xmin, xmax, ymin, ymax, zmin, zmax
	 */
	public float[] getRanges()
	{
		float[] ranges = new float[6];

		ranges[0] = xmin;
		ranges[1] = xmax;
		ranges[2] = ymin;
		ranges[3] = ymax;
		ranges[4] = zmin;
		ranges[5] = zmax;

		return ranges;
	}

	/**
	 * Sets the data availability flag. If this flag is <code>false</code>,
	 * <code>SurfaceCanvas</code> will not generate any surface image, even
	 * if the data is available. But it is the programmer's responsiblity
	 * to set this flag to <code>false</code> when data is not available.
	 *
	 * @param avail the availability flag
	 */
	public void setDataAvailability(boolean avail)
	{
		data_available = avail;
	}

	/**
	 * Sets the new vertices array of surface.
	 *
	 * @param vertex the new vertices array
	 * @see   #getValuesArray
	 */
	public void setValuesArray(Point3D[] vertex)
	{
		this.vertex = vertex;
	}

	/**
	 * Gets the current vertices array.
	 *
	 * @return current vertices array
	 * @see    #setValuesArray
	 */
	public Point3D[] getValuesArray()
	{
		if (!data_available)
		{
			return null;
		}
		return vertex;
	}
	
	private void renderSurface()
	{
		float stepx, stepy, x, y, v;
		float xi, xx, yi, yx;
		float min, max;
		boolean f1, f2;
		int i, j, k, total;

		xi = model.getXMin();
		yi = model.getYMin();
		xx = model.getXMax();
		yx = model.getYMax();

		setRanges(xi, xx, yi, yx);

		calc_divisions = model.getCalcDivisions();
		setDataAvailability(false);

		stepx = (xx - xi) / calc_divisions;
		stepy = (yx - yi) / calc_divisions;

		total = (calc_divisions + 1) * (calc_divisions + 1);

		Point3D[] tmpVertices = new Point3D[total];

		max = Float.NaN;
		min = Float.NaN;

		destroyImage();

		i = 0;
		j = 0;
		k = 0;
		x = xi;
		y = yi;

		float xfactor = 20 / (xx - xi);
		float yfactor = 20 / (yx - yi);

		while (i <= calc_divisions)
		{
			while (j <= calc_divisions)
			{
				v = model.calculateZ(x, y);
				if (Float.isInfinite(v))
				{
					v = Float.NaN;
				}
				if (!Float.isNaN(v))
				{
					if (Float.isNaN(max) || (v > max))
					{
						max = v;
					}
					else
					{
						if (Float.isNaN(min) || (v < min))
						{
							min = v;
						}
					}
				}
				tmpVertices[k] = new Point3D((x - xi) * xfactor - 10,
											 (y - yi) * yfactor - 10, v);
				j++;
				y += stepy;
				k++;
			}
			j = 0;
			y = yi;
			i++;
			x += stepx;
		}

		setValuesArray(tmpVertices);
		setDataAvailability(true);
		repaint();
	}


	/**
	 * Paints surface. Creates surface plot, contour plot, or density plot
	 * based on current vertices array, contour plot flag, and density plot
	 * flag. If no data is available, creates image of base plane and axes.
	 *
	 * @param g the graphics context to paint
	 * @see   #setContour
	 * @see   #setDensity
	 * @see   #setValuesArray
	 * @see   #setDataAvailability
	 */
	@Override
	public void paint(Graphics g)
	{
		if ((getBounds().width <= 0) || (getBounds().height <= 0))
			return;

		// Initialize Buffer Image
		if (bufferImage == null
			|| (getBounds().width != curBufferWidth)
			|| (getBounds().height != curBufferHeight))
		{
			projector.setProjectionArea(new Rectangle(0, 0, getBounds().width, getBounds().height));
			image_drawn = false;
			if (bufferImage != null)
				bufferImage.flush();
			bufferImage = createImage(getBounds().width, getBounds().height);

			if (bufferGraphics != null)
				bufferGraphics.dispose();
			bufferGraphics = bufferImage.getGraphics();
			curBufferWidth = getBounds().width;
			curBufferHeight = getBounds().height;
		}

		printing = (g instanceof PrintGraphics);

		if (printing)
		{

			// modifies variables

			Graphics savedgc = bufferGraphics;
			bufferGraphics = g;

			Dimension pagedimension = ((PrintGraphics) g).getPrintJob().getPageDimension();

			printwidth = pagedimension.width;
			printheight = curBufferHeight * printwidth / curBufferWidth;

			if (printheight > pagedimension.height)
			{
				printheight = pagedimension.height;
				printwidth = curBufferWidth * printheight / curBufferHeight;
			}

			float savedscalingfactor = projector.get2DScaling();
			projector.setProjectionArea(new Rectangle(0, 0, printwidth, printheight));
			projector.set2DScaling(savedscalingfactor * printwidth / curBufferWidth);

			bufferGraphics.clipRect(0, 0, printwidth, printheight);

			// starts printing

			if (!data_available)
			{
				drawBoxGridsTicksLabels(bufferGraphics);
			}
			else
			{
				int fontsize = (int) (Math.round(projector.get2DScaling() * 0.8));
				bufferGraphics.setFont(new Font("Arial", Font.PLAIN, fontsize));

				Point3D.invalidate();

				// surface plot
				if (plot_mode == ISurfacePlotModel.PLOT_MODE_WIREFRAME)
					plotWireframe();
				else
					plotSurface();

				if (isBoxed)
					drawBoundingBox();
			}
			bufferGraphics.drawRect(0, 0, printwidth - 1, printheight - 1);

			// restores variables

			projector.set2DScaling(savedscalingfactor);
			projector.setProjectionArea(new Rectangle(0, 0, getBounds().width, getBounds().height));
			bufferGraphics = savedgc;
		}
		else
		{

			if (image_drawn && (bufferImage != null))
			{
				g.drawImage(bufferImage, 0, 0, this);
			}
			else
			{
				if (data_available)
				{
					int fontsize = (int) (Math.round(projector.get2DScaling() * 0.8));
					bufferGraphics.setFont(new Font("Arial", Font.PLAIN, fontsize));

					Point3D.invalidate();

					// surface plot
					if (plot_mode == ISurfacePlotModel.PLOT_MODE_WIREFRAME)
						plotWireframe();
					else
						plotSurface();
					if (isBoxed)
						drawBoundingBox();

					image_drawn = true;
					g.drawImage(bufferImage, 0, 0, this);
				}
				else
				{
					g.setColor(Color.lightGray);
					g.fillRect(0, 0, getBounds().width, getBounds().height);
					drawBoxGridsTicksLabels(g);
				}
			}
		}
	}

	/**
	 * Updates image. Just call the <code>paint</code> method to
	 * avoid flickers.
	 *
	 * @param g the graphics context to update
	 * @see   #paint
	 */
	@Override
	public void update(Graphics g)
	{
		paint(g);                        // do not erase, just paint
	}

	/**
	 * Returns the preferred size of this object. This will be the initial
	 * size of <code>SurfaceCanvas</code>.
	 *
	 * @return the preferred size.
	 */
//	public Dimension preferredSize()
//	{
//		return new Dimension(550, 550);  // initial canvas size
//	}

	/*----------------------------------------------------------------------------------------*
	 *                            Private methods begin here                                  *
	 *----------------------------------------------------------------------------------------*/
	private int factor_x, factor_y;   // conversion factors
	private int t_x, t_y, t_z;        // determines ticks density

	/**
	 * Draws the bounding box of surface.
	 */
	private void drawBoundingBox()
	{
		Point startingpoint, projection;

		startingpoint = projector.project(factor_x * 10, factor_y * 10, 10);
		bufferGraphics.setColor(Color.black);
		projection = projector.project(-factor_x * 10, factor_y * 10, 10);
		bufferGraphics.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
		projection = projector.project(factor_x * 10, -factor_y * 10, 10);
		bufferGraphics.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
		projection = projector.project(factor_x * 10, factor_y * 10, -10);
		bufferGraphics.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
	}

	/**
	 * Draws the base plane. The base plane is the x-y plane.
	 *
	 * @param g the graphics context to draw.
	 * @param x used to retrieve x coordinates of drawn plane from this method.
	 * @param y used to retrieve y coordinates of drawn plane from this method.
	 */
	private void drawBase(Graphics g, int[] x, int[] y)
	{
		Point projection = projector.project(-10, -10, -10);
		x[0] = projection.x;
		y[0] = projection.y;
		projection = projector.project(-10, 10, -10);
		x[1] = projection.x;
		y[1] = projection.y;
		projection = projector.project(10, 10, -10);
		x[2] = projection.x;
		y[2] = projection.y;
		projection = projector.project(10, -10, -10);
		x[3] = projection.x;
		y[3] = projection.y;
		x[4] = x[0];
		y[4] = y[0];

		if (plot_mode != ISurfacePlotModel.PLOT_MODE_WIREFRAME)
		{
			if (plot_mode == ISurfacePlotModel.PLOT_MODE_NORENDER)
			{
				g.setColor(Color.lightGray);
			}
			else
			{
				g.setColor(new Color(192, 220, 192));
			}
			g.fillPolygon(x, y, 4);
		}
		g.setColor(Color.black);
		g.drawPolygon(x, y, 5);
	}

	/**
	 * Draws non-surface parts, i.e: bounding box, axis grids, axis ticks,
	 * axis labels, base plane.
	 *
	 * @param g         the graphics context to draw
	 * @param draw_axes if <code>true</code>, only draws base plane and z axis
	 */
	private void drawBoxGridsTicksLabels(Graphics g)
	{
		Point projection, tickpos;
		boolean x_left = false, y_left = false;
		int x[], y[], i;

		x = new int[5];
		y = new int[5];
		if (projector == null)
		{
			return;
		}

		factor_x = factor_y = 1;
		projection = projector.project(0, 0, -10);
		x[0] = projection.x;
		projection = projector.project(10.5f, 0, -10);
		y_left = projection.x > x[0];
		i = projection.y;
		projection = projector.project(-10.5f, 0, -10);
		if (projection.y > i)
		{
			factor_x = -1;
			y_left = projection.x > x[0];
		}
		projection = projector.project(0, 10.5f, -10);
		x_left = projection.x > x[0];
		i = projection.y;
		projection = projector.project(0, -10.5f, -10);
		if (projection.y > i)
		{
			factor_y = -1;
			x_left = projection.x > x[0];
		}
		setAxesScale();
		drawBase(g, x, y);

		if (isBoxed)
		{
			projection = projector.project(-factor_x * 10, -factor_y * 10, -10);
			x[0] = projection.x;
			y[0] = projection.y;
			projection = projector.project(-factor_x * 10, -factor_y * 10, 10);
			x[1] = projection.x;
			y[1] = projection.y;
			projection = projector.project(factor_x * 10, -factor_y * 10, 10);
			x[2] = projection.x;
			y[2] = projection.y;
			projection = projector.project(factor_x * 10, -factor_y * 10, -10);
			x[3] = projection.x;
			y[3] = projection.y;
			x[4] = x[0];
			y[4] = y[0];

			if (plot_mode != ISurfacePlotModel.PLOT_MODE_WIREFRAME)
			{
				if (plot_mode == ISurfacePlotModel.PLOT_MODE_NORENDER)
				{
					g.setColor(Color.lightGray);
				}
				else
				{
					g.setColor(new Color(192,220,192));
				}
				g.fillPolygon(x, y, 4);
			}
			g.setColor(Color.black);
			g.drawPolygon(x, y, 5);

			projection = projector.project(-factor_x * 10, factor_y * 10, 10);
			x[2] = projection.x;
			y[2] = projection.y;
			projection = projector.project(-factor_x * 10, factor_y * 10, -10);
			x[3] = projection.x;
			y[3] = projection.y;
			x[4] = x[0];
			y[4] = y[0];

			if (plot_mode != ISurfacePlotModel.PLOT_MODE_WIREFRAME)
			{
				if (plot_mode == ISurfacePlotModel.PLOT_MODE_NORENDER)
				{
					g.setColor(Color.lightGray);
				}
				else
				{
					g.setColor(new Color(192, 220, 192));
				}
				g.fillPolygon(x, y, 4);
			}
			g.setColor(Color.black);
			g.drawPolygon(x, y, 5);
		}
		else
		{
			if (isDisplayZ)
			{
				projection = projector.project(factor_x * 10, -factor_y * 10, -10);
				x[0] = projection.x;
				y[0] = projection.y;
				projection = projector.project(factor_x * 10, -factor_y * 10, 10);
				g.drawLine(x[0], y[0], projection.x, projection.y);

				projection = projector.project(-factor_x * 10, factor_y * 10, -10);
				x[0] = projection.x;
				y[0] = projection.y;
				projection = projector.project(-factor_x * 10, factor_y * 10, 10);
				g.drawLine(x[0], y[0], projection.x, projection.y);
			}
		}

		for (i = -9; i <= 9; i++)
		{
			if (isDisplayXY || isDisplayGrids)
			{
				if (!isDisplayGrids || (i % (t_y / 2) == 0) || isDisplayXY)
				{
					if (isDisplayGrids && (i % t_y == 0))
					{
						projection = projector.project(-factor_x * 10, i, -10);
					}
					else
					{
						if (i % t_y != 0)
						{
							projection = projector.project(factor_x * 9.8f, i, -10);
						}
						else
						{
							projection = projector.project(factor_x * 9.5f, i, -10);
						}
					}
					tickpos = projector.project(factor_x * 10, i, -10);
					g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
					if ((i % t_y == 0) && isDisplayXY)
					{
						tickpos = projector.project(factor_x * 10.5f, i, -10);
						if (y_left)
						{
							drawNumber(g, tickpos.x, tickpos.y,
									 (float) ((double) (i + 10) / 20 * (ymax - ymin) + ymin),
									 Label.LEFT, TOP);
						}
						else
						{
							drawNumber(g, tickpos.x, tickpos.y,
									 (float) ((double) (i + 10) / 20 * (ymax - ymin) + ymin),
									 Label.RIGHT, TOP);
						}
					}
				}
				if (!isDisplayGrids || (i % (t_x / 2) == 0) || isDisplayXY)
				{
					if (isDisplayGrids && (i % t_x == 0))
					{
						projection = projector.project(i, -factor_y * 10, -10);
					}
					else
					{
						if (i % t_x != 0)
						{
							projection = projector.project(i, factor_y * 9.8f, -10);
						}
						else
						{
							projection = projector.project(i, factor_y * 9.5f, -10);
						}
					}
					tickpos = projector.project(i, factor_y * 10, -10);
					g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
					if ((i % t_x == 0) && isDisplayXY)
					{
						tickpos = projector.project(i, factor_y * 10.5f, -10);
						if (x_left)
						{
							drawNumber(g, tickpos.x, tickpos.y,
									 (float) ((double) (i + 10) / 20 * (xmax - xmin) + xmin),
									 Label.LEFT, TOP);
						}
						else
						{
							drawNumber(g, tickpos.x, tickpos.y,
									 (float) ((double) (i + 10) / 20 * (xmax - xmin) + xmin),
									 Label.RIGHT, TOP);
						}
					}
				}
			}


			// z grids and ticks

			if (isDisplayZ || (isDisplayGrids && isBoxed))
			{
				if (!isDisplayGrids || (i % (t_z / 2) == 0) || isDisplayZ)
				{
					if (isBoxed && isDisplayGrids && (i % t_z == 0))
					{
						projection = projector.project(-factor_x * 10, -factor_y * 10, i);
						tickpos = projector.project(-factor_x * 10, factor_y * 10, i);
					}
					else
					{
						if (i % t_z == 0)
						{
							projection = projector.project(-factor_x * 10, factor_y * 9.5f, i);
						}
						else
						{
							projection = projector.project(-factor_x * 10, factor_y * 9.8f, i);
						}
						tickpos = projector.project(-factor_x * 10, factor_y * 10, i);
					}
					g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
					if (isDisplayZ)
					{
						tickpos = projector.project(-factor_x * 10, factor_y * 10.5f, i);
						if (i % t_z == 0)
						{
							if (x_left)
							{
								drawNumber(g, tickpos.x, tickpos.y,
										 (float) ((double) (i + 10) / 20 * (zmax - zmin) + zmin),
										 Label.LEFT, CENTER);
							}
							else
							{
								drawNumber(g, tickpos.x, tickpos.y,
										 (float) ((double) (i + 10) / 20 * (zmax - zmin) + zmin),
										 Label.RIGHT, CENTER);
							}
						}
					}
					if (isDisplayGrids && isBoxed && (i % t_z == 0))
					{
						projection = projector.project(-factor_x * 10, -factor_y * 10, i);
						tickpos = projector.project(factor_x * 10, -factor_y * 10, i);
					}
					else
					{
						if (i % t_z == 0)
						{
							projection = projector.project(factor_x * 9.5f, -factor_y * 10, i);
						}
						else
						{
							projection = projector.project(factor_x * 9.8f, -factor_y * 10, i);
						}
						tickpos = projector.project(factor_x * 10, -factor_y * 10, i);
					}
					g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
					if (isDisplayZ)
					{
						tickpos = projector.project(factor_x * 10.5f, -factor_y * 10, i);
						if (i % t_z == 0)
						{
							if (y_left)
							{
								drawNumber(g, tickpos.x, tickpos.y,
										 (float) ((double) (i + 10) / 20 * (zmax - zmin) + zmin),
										 Label.LEFT, CENTER);
							}
							else
							{
								drawNumber(g, tickpos.x, tickpos.y,
										 (float) ((double) (i + 10) / 20 * (zmax - zmin) + zmin),
										 Label.RIGHT, CENTER);
							}
						}
					}
					if (isDisplayGrids && isBoxed)
					{
						if (i % t_y == 0)
						{
							projection = projector.project(-factor_x * 10, i, -10);
							tickpos = projector.project(-factor_x * 10, i, 10);
							g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
						}
						if (i % t_x == 0)
						{
							projection = projector.project(i, -factor_y * 10, -10);
							tickpos = projector.project(i, -factor_y * 10, 10);
							g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
						}
					}
				}
			}
		}
		if (isDisplayXY)
		{
			tickpos = projector.project(0, factor_y * 14, -10);
			drawString(g, tickpos.x, tickpos.y, xAxisLabel, Label.CENTER, TOP);
			tickpos = projector.project(factor_x * 14, 0, -10);
			drawString(g, tickpos.x, tickpos.y, yAxisLabel, Label.CENTER, TOP);
		}
		if(isDisplayZ)
		{
			tickpos = projector.project(-factor_x * 10, factor_y * 14, 0);
			drawString(g, tickpos.x, tickpos.y, zAxisLabel, Label.CENTER, TOP);
		}
	}

	/**
	 * Sets the axes scaling factor. Computes the proper axis lengths
	 * based on the ratio of variable ranges. The axis lengths will
	 * also affect the size of bounding box.
	 */
	private void setAxesScale()
	{
		float scale_x, scale_y, scale_z, divisor;
		int longest;

		if (!isScaleBox)
		{
			projector.setScaling(1f);
			t_x = t_y = t_z = 4;
			return;
		}

		scale_x = xmax - xmin;
		scale_y = ymax - ymin;
		scale_z = zmax - zmin;

		if (scale_x < scale_y)
		{
			if (scale_y < scale_z)
			{
				longest = 3;
				divisor = scale_z;
			}
			else
			{
				longest = 2;
				divisor = scale_y;
			}
		}
		else
		{
			if (scale_x < scale_z)
			{
				longest = 3;
				divisor = scale_z;
			}
			else
			{
				longest = 1;
				divisor = scale_x;
			}
		}
		scale_x /= divisor;
		scale_y /= divisor;
		scale_z /= divisor;

		if ((scale_x < 0.2f) || (scale_y < 0.2f) && (scale_z < 0.2f))
		{
			switch (longest)
			{
				case 1:
					if (scale_y < scale_z)
					{
						scale_y /= scale_z;
						scale_z = 1.0f;
					}
					else
					{
						scale_z /= scale_y;
						scale_y = 1.0f;
					}
					break;
				case 2:
					if (scale_x < scale_z)
					{
						scale_x /= scale_z;
						scale_z = 1.0f;
					}
					else
					{
						scale_z /= scale_x;
						scale_x = 1.0f;
					}
					break;
				case 3:
					if (scale_y < scale_x)
					{
						scale_y /= scale_x;
						scale_x = 1.0f;
					}
					else
					{
						scale_x /= scale_y;
						scale_y = 1.0f;
					}
					break;
			}
		}
		if (scale_x < 0.2f)
		{
			scale_x = 1.0f;
		}
		projector.setXScaling(scale_x);
		if (scale_y < 0.2f)
		{
			scale_y = 1.0f;
		}
		projector.setYScaling(scale_y);
		if (scale_z < 0.2f)
		{
			scale_z = 1.0f;
		}
		projector.setZScaling(scale_z);

		if (scale_x < 0.5f)
		{
			t_x = 8;
		}
		else
		{
			t_x = 4;
		}
		if (scale_y < 0.5f)
		{
			t_y = 8;
		}
		else
		{
			t_y = 4;
		}
		if (scale_z < 0.5f)
		{
			t_z = 8;
		}
		else
		{
			t_z = 4;
		}
	}

	/**
	 * Draws string at the specified coordinates with the specified alignment.
	 *
	 * @param g       graphics context to draw
	 * @param x       the x coordinate
	 * @param y       the y coordinate
	 * @param s       the string to draw
	 * @param x_align the alignment in x direction
	 * @param y_align the alignment in y direction
	 */
	private void drawString(Graphics g, int x, int y,
								 String s, int x_align, int y_align)
	{
		switch (y_align)
		{
			case TOP:
				y += g.getFontMetrics(g.getFont()).getAscent();
				break;
			case CENTER:
				y += g.getFontMetrics(g.getFont()).getAscent() / 2;
				break;
		}
		switch (x_align)
		{
			case Label.LEFT:
				g.drawString(s, x, y);
				break;
			case Label.RIGHT:
				g.drawString(s, x - g.getFontMetrics(
					g.getFont()).stringWidth(s), y);
				break;
			case Label.CENTER:
				g.drawString(s, x - g.getFontMetrics(
					g.getFont()).stringWidth(s) / 2, y);
				break;
		}
	}

	/**
	 * Draws float at the specified coordinates with the specified alignment.
	 *
	 * @param g       graphics context to draw
	 * @param x       the x coordinate
	 * @param y       the y coordinate
	 * @param f       the float to draw
	 * @param x_align the alignment in x direction
	 * @param y_align the alignment in y direction
	 */
	private void drawNumber(Graphics g, int x, int y,
								float f, int x_align, int y_align)
	{
		String s = Float.toString(f);
		drawString(g, x, y, s, x_align, y_align);
	}

	/*----------------------------------------------------------------------------------------*
	 *                       Plotting routines and methods begin here                         *
	 *----------------------------------------------------------------------------------------*/
	private float color_factor;
	private Color line_color;
	private final int poly_x[] = new int[9];
	private final int poly_y[] = new int[9];

	/**
	 * Plots a single plane
	 *
	 * @param vertex vertices array of the plane
	 * @param verticescount number of vertices to process
	 */
	private void plotPlane(Point3D[] vertex, int verticescount)
	{
		Point projection;
		int count, loop, index;
		float z, result;
		boolean low1, low2;
		boolean valid1, valid2;

		if (verticescount < 3)
		{
			return;
		}
		count = 0;
		z = 0.0f;
		line_color = Color.darkGray;
		low1 = (vertex[0].z < zmin);
		valid1 = !low1 && (vertex[0].z <= zmax);
		index = 1;
		for (loop = 0; loop < verticescount; loop++)
		{
			low2 = (vertex[index].z < zmin);
			valid2 = !low2 && (vertex[index].z <= zmax);
			if ((valid1 || valid2) || (low1 ^ low2))
			{
				if (!valid1)
				{
					if (low1)
					{
						result = zmin;
					}
					else
					{
						result = zmax;
					}
					float ratio = (result - vertex[index].z) / (vertex[loop].z - vertex[index].z);
					float new_x = ratio * (vertex[loop].x - vertex[index].x) + vertex[index].x;
					float new_y = ratio * (vertex[loop].y - vertex[index].y) + vertex[index].y;
					if (low1)
					{
						projection = projector.project(new_x, new_y, -10);
					}
					else
					{
						projection = projector.project(new_x, new_y, 10);
					}
					poly_x[count] = projection.x;
					poly_y[count] = projection.y;
					count++;
					z += result;
				}
				if (valid2)
				{
					projection = vertex[index].projection();
					poly_x[count] = projection.x;
					poly_y[count] = projection.y;
					count++;
					z += vertex[index].z;
				}
				else
				{
					if (low2)
					{
						result = zmin;
					}
					else
					{
						result = zmax;
					}
					float ratio = (result - vertex[loop].z) / (vertex[index].z - vertex[loop].z);
					float new_x = ratio * (vertex[index].x - vertex[loop].x) + vertex[loop].x;
					float new_y = ratio * (vertex[index].y - vertex[loop].y) + vertex[loop].y;
					if (low2)
					{
						projection = projector.project(new_x, new_y, -10);
					}
					else
					{
						projection = projector.project(new_x, new_y, 10);
					}
					poly_x[count] = projection.x;
					poly_y[count] = projection.y;
					count++;
					z += result;
				}
			}
			if (++index == verticescount)
			{
				index = 0;
			}
			valid1 = valid2;
			low1 = low2;
		}
		if (count > 0)
		{
			switch (plot_mode)
			{
				case ISurfacePlotModel.PLOT_MODE_NORENDER:
					bufferGraphics.setColor(Color.lightGray);
					break;
				case ISurfacePlotModel.PLOT_MODE_SPECTRUM:
					z = 0.8f - (z / count - zmin) * color_factor;
					bufferGraphics.setColor(Color.getHSBColor(z, 1.0f, 1.0f));
					break;
				case ISurfacePlotModel.PLOT_MODE_GRAYSCALE:
					z = (z / count - zmin) * color_factor;
					bufferGraphics.setColor(Color.getHSBColor(0, 0, z));
					if (z < 0.3f)
					{
						line_color = new Color(0.6f, 0.6f, 0.6f);
					}
					break;
				case ISurfacePlotModel.PLOT_MODE_DUALSHADE:
					z = (z / count - zmin) * color_factor + 0.4f;
					bufferGraphics.setColor(Color.getHSBColor(color, 0.7f, z));
					break;
			}

			bufferGraphics.fillPolygon(poly_x, poly_y, count);
			bufferGraphics.setColor(line_color);
			if (isMesh || (plot_mode == ISurfacePlotModel.PLOT_MODE_NORENDER))
			{
				poly_x[count] = poly_x[0];
				poly_y[count] = poly_y[0];
				count++;
				bufferGraphics.drawPolygon(poly_x, poly_y, count);
			}
		}
	}

	/**
	 * Determines whether a plane is plottable, i.e: does not have
	 * invalid vertex.
	 *
	 * @return <code>true</code> if the plane is plottable,
	 *         <code>false</code> otherwise
	 * @param values vertices array of the plane
	 */
	private static boolean isPointsValid(Point3D[] values)
	{
		return (!values[0].isInvalid()
			&& !values[1].isInvalid()
			&& !values[2].isInvalid()
			&& !values[3].isInvalid());
	}


	/**
	 * Plots an area of group of planes
	 *
	 * @param start_lx start index in x direction
	 * @param start_ly start index in y direction
	 * @param end_lx   end index in x direction
	 * @param end_ly   end index in y direction
	 * @param sx       step in x direction
	 * @param sy       step in y direction
	 */
	private void plotArea(int start_lx, int start_ly,
								int end_lx, int end_ly,
								int sx, int sy)
	{
		Point3D values1[] = new Point3D[4];

		start_lx *= calc_divisions + 1;
		sx *= calc_divisions + 1;
		end_lx *= calc_divisions + 1;

		int lx = start_lx;
		int ly = start_ly;

		while (ly != end_ly)
		{
			values1[1] = vertex[lx + ly];
			values1[2] = vertex[lx + ly + sy];

			while (lx != end_lx)
			{
				values1[0] = values1[1];
				values1[1] = vertex[lx + sx + ly];
				values1[3] = values1[2];
				values1[2] = vertex[lx + sx + ly + sy];
				if (plot_mode == ISurfacePlotModel.PLOT_MODE_DUALSHADE)
				{
					color = 0.2f;
				}
				if (isPointsValid(values1))
				{
					plotPlane(values1, 4);
				}
				lx += sx;
			}
			ly += sy;
			lx = start_lx;
		}
	}

	/**
	 * Creates a surface plot
	 */
	private void plotSurface()
	{
		float zi, zx;
		int sx, sy;
		int start_lx, end_lx;
		int start_ly, end_ly;

		image_drawn = false;
		zi = model.getZMin();
		zx = model.getZMax();

		int plot_density = model.getDispDivisions();
		int multiple_factor = calc_divisions / plot_density;

		zmin = zi;
		zmax = zx;
		color_factor = 0.8f / (zmax - zmin);
		if (plot_mode == ISurfacePlotModel.PLOT_MODE_DUALSHADE)
		{
			color_factor *= 0.6f / 0.8f;
		}

		if (!printing)
		{
			bufferGraphics.setColor(Color.lightGray);
			bufferGraphics.fillRect(0, 0, getBounds().width, getBounds().height);
		}

		drawBoxGridsTicksLabels(bufferGraphics);

		Point3D.setZRange(zmin, zmax);

		// direction test

		float distance = projector.getDistance() * projector.getCosElevationAngle();

		// cop : center of projection

		cop = new Point3D(distance * projector.getSinRotationAngle(),
						  distance * projector.getCosRotationAngle(),
						  projector.getDistance() * projector.getSinElevationAngle());
		cop.transform();

		boolean inc_x = cop.x > 0;
		boolean inc_y = cop.y > 0;


		if (inc_x)
		{
			start_lx = 0;
			end_lx = calc_divisions;
			sx = multiple_factor;
		}
		else
		{
			start_lx = calc_divisions;
			end_lx = 0;
			sx = -multiple_factor;
		}
		if (inc_y)
		{
			start_ly = 0;
			end_ly = calc_divisions;
			sy = multiple_factor;
		}
		else
		{
			start_ly = calc_divisions;
			end_ly = 0;
			sy = -multiple_factor;
		}

		if ((cop.x > 10) || (cop.x < -10))
		{
			if ((cop.y > 10) || (cop.y < -10))
			{
				plotArea(start_lx, start_ly, end_lx, end_ly, sx, sy);
			}
			else
			{    // split in y direction
				int split_y = (int) ((cop.y + 10) * plot_density / 20) * multiple_factor;
				plotArea(start_lx, 0, end_lx, split_y, sx, multiple_factor);
				plotArea(start_lx, calc_divisions, end_lx, split_y, sx, -multiple_factor);
			}
		}
		else
		{
			if ((cop.y > 10) || (cop.y < -10))
			{   // split in x direction
				int split_x = (int) ((cop.x + 10) * plot_density / 20) * multiple_factor;
				plotArea(0, start_ly, split_x, end_ly, multiple_factor, sy);
				plotArea(calc_divisions, start_ly, split_x, end_ly, -multiple_factor, sy);
			}
			else
			{    // split in both x and y directions
				int split_x = (int) ((cop.x + 10) * plot_density / 20) * multiple_factor;
				int split_y = (int) ((cop.y + 10) * plot_density / 20) * multiple_factor;
				plotArea(0, 0, split_x, split_y, multiple_factor, multiple_factor);
				plotArea(0, calc_divisions, split_x, split_y, multiple_factor, -multiple_factor);
				plotArea(calc_divisions, 0, split_x, split_y, -multiple_factor, multiple_factor);
				plotArea(calc_divisions, calc_divisions, split_x, split_y,
						 -multiple_factor, -multiple_factor);
			}
		}
	}

	/**
	 * Creates wireframe plot
	 */
	private void plotWireframe()
	{
		int i, j, k;
		int plot_density, multiple_factor;
		int counter;
		float zi, zx;
		float z;
		float lx = 0, ly = 0, lastz = 0;
		Point lastproj = new Point(0, 0);
		boolean error, lasterror, invalid;

		image_drawn = false;
		Point projection = new Point(0, 0);
		zi = model.getZMin();
		zx = model.getZMax();

		plot_density = model.getDispDivisions();
		multiple_factor = calc_divisions / plot_density;

		zmin = zi;
		zmax = zx;

		if (!printing)
		{
			bufferGraphics.setColor(Color.lightGray);
			bufferGraphics.fillRect(0, 0, getBounds().width, getBounds().height);
		}

		drawBoxGridsTicksLabels(bufferGraphics);
		bufferGraphics.setColor(Color.black);

		Point3D.setZRange(zmin, zmax);


		i = 0;
		j = 0;
		k = 0;
		counter = 0;

		// plot - x direction

		while (i <= calc_divisions)
		{
			lasterror = true;
			if (counter == 0)
			{
				while (j <= calc_divisions)
				{
					z = vertex[k].z;
					invalid = Float.isNaN(z);
					if (!invalid)
					{
						if (z < zmin)
						{
							error = true;
							float ratio = (zmin - lastz) / (z - lastz);
							projection = projector.project(ratio * (vertex[k].x - lx) + lx,
														   ratio * (vertex[k].y - ly) + ly, -10);
						}
						else
						{
							if (z > zmax)
							{
								error = true;
								float ratio = (zmax - lastz) / (z - lastz);
								projection = projector.project(ratio * (vertex[k].x - lx) + lx,
															   ratio * (vertex[k].y - ly) + ly, 10);
							}
							else
							{
								error = false;
								projection = vertex[k].projection();
							}
						}
						if (lasterror && (!error) && (j != 0))
						{
							if (lastz > zmax)
							{
								float ratio = (zmax - z) / (lastz - z);
								lastproj = projector.project(
									ratio * (lx - vertex[k].x) + vertex[k].x,
									ratio * (ly - vertex[k].y) + vertex[k].y, 10);
							}
							else
							{
								if (lastz < zmin)
								{
									float ratio = (zmin - z) / (lastz - z);
									lastproj = projector.project(
										ratio * (lx - vertex[k].x) + vertex[k].x,
										ratio * (ly - vertex[k].y) + vertex[k].y, -10);
								}
							}
						}
						else
						{
							invalid = error && lasterror;
						}
					}
					else
					{
						error = true;
					}
					if (!invalid && (j != 0))
					{
						bufferGraphics.drawLine(lastproj.x, lastproj.y, projection.x, projection.y);
					}
					lastproj = projection;
					lasterror = error;
					lx = vertex[k].x;
					ly = vertex[k].y;
					lastz = z;
					j++;
					k++;
				}
			}
			else
			{
				k += calc_divisions + 1;
			}
			j = 0;
			i++;
			counter = (counter + 1) % multiple_factor;
		}

		// plot - y direction

		i = 0;
		j = 0;
		k = 0;
		counter = 0;

		while (j <= calc_divisions)
		{
			lasterror = true;
			if (counter == 0)
			{
				while (i <= calc_divisions)
				{
					z = vertex[k].z;
					invalid = Float.isNaN(z);
					if (!invalid)
					{
						if (z < zmin)
						{
							error = true;
							float ratio = (zmin - lastz) / (z - lastz);
							projection = projector.project(ratio * (vertex[k].x - lx) + lx,
														   ratio * (vertex[k].y - ly) + ly, -10);
						}
						else
						{
							if (z > zmax)
							{
								error = true;
								float ratio = (zmax - lastz) / (z - lastz);
								projection = projector.project(ratio * (vertex[k].x - lx) + lx,
															   ratio * (vertex[k].y - ly) + ly, 10);
							}
							else
							{
								error = false;
								projection = vertex[k].projection();
							}
						}
						if (lasterror && (!error) && (i != 0))
						{
							if (lastz > zmax)
							{
								float ratio = (zmax - z) / (lastz - z);
								lastproj = projector.project(
									ratio * (lx - vertex[k].x) + vertex[k].x,
									ratio * (ly - vertex[k].y) + vertex[k].y, 10);
							}
							else
							{
								if (lastz < zmin)
								{
									float ratio = (zmin - z) / (lastz - z);
									lastproj = projector.project(
										ratio * (lx - vertex[k].x) + vertex[k].x,
										ratio * (ly - vertex[k].y) + vertex[k].y, -10);
								}
							}
						}
						else
						{
							invalid = error && lasterror;
						}
					}
					else
					{
						error = true;
					}
					if (!invalid && (i != 0))
					{
						bufferGraphics.drawLine(lastproj.x, lastproj.y, projection.x, projection.y);
					}
					lastproj = projection;
					lasterror = error;
					lx = vertex[k].x;
					ly = vertex[k].y;
					lastz = z;
					i++;
					k += calc_divisions + 1;
				}
			}
			i = 0;
			k = ++j;
			counter = (counter + 1) % multiple_factor;
		}

	}
}


