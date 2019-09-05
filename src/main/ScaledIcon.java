/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017-2019 Zachary J. Ward
 *
 * This file is part of Amua. Amua is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Amua is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amua.  If not, see <http://www.gnu.org/licenses/>.
 */

package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.Icon;

import gui.frmMain;


public class ScaledIcon implements Icon{
	Image image;
	BufferedImage imageDisabled;
	int width, height;
	boolean enabled;
		
	//constructor
	public ScaledIcon(String basePath, int origRes, int width, int height, boolean enabled) {
		try {
			this.width=width;
			this.height=height;
			this.enabled=enabled;

			//get resolution
			int res=origRes;
			int screenRes=Toolkit.getDefaultToolkit().getScreenResolution();
			if(screenRes>200) {res=128;}
			String path=basePath+"_"+res+".png";

			if(enabled==true) {
				image=Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource(path));
			}
			else {
				imageDisabled = ImageIO.read(frmMain.class.getResource(path));
				int w=imageDisabled.getWidth();
				int h=imageDisabled.getHeight();

				//convert to grayscale
				for(int y = 0; y < h; y++){
					for(int x = 0; x < w; x++){

						Color c = new Color(imageDisabled.getRGB(x, y),true);

						int red = (int) (c.getRed() * 0.299);
						int green = (int) (c.getGreen() * 0.587);
						int blue = (int) (c.getBlue() * 0.114);
						int alpha = (int) (c.getAlpha() * 0.5);

						int grey = red + green + blue;

						Color newColor = new Color(grey, grey, grey, alpha);

						imageDisabled.setRGB(x, y, newColor.getRGB());

					}
				}
			}

		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public ScaledIcon(String basePath, int origRes, int highRes) {
		this.width=origRes;
		this.height=origRes;
		this.enabled=true;

		//get resolution
		int res=origRes;
		int screenRes=Toolkit.getDefaultToolkit().getScreenResolution();
		if(screenRes>120) {res=highRes;}
		String path=basePath+"_"+res+".png";

		image=Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource(path));
	}
	

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if(enabled) {
			g.drawImage(image, x, y, width, height, null);
		}
		else {
			g.drawImage(imageDisabled, x, y, width, height, null);
		}	
		if(c!=null) {
			c.repaint();
			c.revalidate();
		}
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}
	
	
	
	
}