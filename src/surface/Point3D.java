package surface;
/*----------------------------------------------------------------------------------------*
 * SurfaceVertex.java                                                                     *
 *                                                                                        *
 * Surface Plotter   version 1.10    14 Oct 1996                                          *
 *                   version 1.20     8 Nov 1996                                          *
 *                   version 1.30b1  17 May 1997                                          *
 *                   version 1.30b2  18 Oct 2001                                          *
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

/**
 * The class <code>SurfaceVertex</code> represents a vertex in 3D space.
 *
 * @author  Yanto Suryono
 */

public final class Point3D {
  private Point  projection;
  private int    project_index;
  private static Projector projector;

  private static float zmin, zmax;
  private static float zfactor;
  private static int   master_project_index = 0;     // over 4 billion times to reset

  /**
   * The x coordinate
   */
  public float x;

  /**
   * The y coordinate
   */
  public float y;

  /**
   * The z coordinate
   */
  public float z;
  
  /**
   * The constructor of <code>SurfaceVertex</code>.
   * The x and y coordinated must be in normalized form, i.e: in the range -10 .. +10.
   *
   * @param ix the x coordinate
   * @param iy the y coordinate
   * @param iz the z coordinate
   */
   
  Point3D(float ix, float iy, float iz) {
    x = ix; y = iy; z = iz;
    project_index = master_project_index-1;
  }
   
  /**
   * Determines whether this vertex is invalid, i.e has invalid coordinates value.
   *
   * @return <code>true</code> if this vertex is invalid
   */
   
  public final boolean isInvalid() {
    return Float.isNaN(z); 
  }

  /**
   * Gets the 2D projection of the vertex.
   *
   * @return the 2D projection
   */
   
  public final Point projection() {
    if (project_index != master_project_index) {
      projection = projector.project(x,y,(z-zmin)*zfactor-10);
      project_index = master_project_index;
    }
    return projection;
  }
  
  /**
   * Transforms coordinate values to fit the scaling factor of the
   * projector. This routine is only used for transforming center of projection
   * in Surface Plotter.
   */
     
  public final void transform() {
    x = x / projector.getXScaling();
    y = y / projector.getYScaling();
    z = (zmax-zmin)*(z/projector.getZScaling()+10)/20 + zmin;
  }
  
  /**
   * Invalidates all vertices. This will force the projector
   * to recalculate vertex projection.
   */
   
  public static void invalidate() {
    master_project_index++;
  }

  /**
   * Sets the projector to project this vertex.
   *
   * @param projector the projector
   */
     
  public static void setProjector(Projector projector) {
    Point3D.projector = projector;
  }
  
  /**
   * Sets the minimum and maximum value of z range.
   * This values is used to compute a factor to normalized
   * z values into the range -10 .. +10.
   *
   * @param zmin the minimum z
   * @param zmax the maximum z 
   */
   
  public static void setZRange(float zmin, float zmax) {
    Point3D.zmin = zmin; Point3D.zmax = zmax;
    zfactor = 20/(zmax-zmin);
  }
}

