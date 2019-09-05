/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package surface;

/**
 *
 * @author salagarsamy
 */
public interface ISurfacePlotModel
{
	static final int PLOT_MODE_WIREFRAME = 0;
	static final int PLOT_MODE_NORENDER = 1;
	static final int PLOT_MODE_SPECTRUM = 2;
	static final int PLOT_MODE_GRAYSCALE = 3;
	static final int PLOT_MODE_DUALSHADE = 4;

	public int getPlotMode();

	public float calculateZ(float x, float y);

	public boolean isBoxed();

	public boolean isMesh();

	public boolean isScaleBox();

	public boolean isDisplayXY();

	public boolean isDisplayZ();

	public boolean isDisplayGrids();

	public int getCalcDivisions();
	public int getDispDivisions();

	public float getXMin();
	public float getXMax();

	public float getYMin();
	public float getYMax();

	public float getZMin();
	public float getZMax();

	public String getXAxisLabel();
	public String getYAxisLabel();
	public String getZAxisLabel();
}
