package com.andredittrich.surface3d;

/*
 * Copyright (C) 2010 by Mathias Menninghaus (mmenning (at) uos (dot) de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;


import android.graphics.Color;
import android.util.Log;

/**
 * Connects to an GOCAD (*.ts) File via HTTP or FileSystem and reads the objects
 * data. It is possible to read from several URLS and files with several
 * GOCAD-Objects. Also MetaData like name and color of the objects is read. The
 * read objects are put into a list of TSObjects which are defined in a inner
 * class. By reading the data, each object is translated into the center of the
 * scene (the center of all objects)
 * 
 * @version 03.06.2010
 * 
 * @author Mathias Menninghaus
 */
public class GOCADConnector {
	
	private static final String TAG = GOCADConnector.class.getSimpleName();
	private static int default_nr = 0;
	private TSObject tsObject;
	/*
	 * Min and max values for the whole scene
	 */
	private float maxY;
	private float maxX;
	private float maxZ;
	private float minY;
	private float minX;
	private float minZ;

	/*
	 * correction subtrahends for the translation into the origin
	 */
	private float correctx;
	private float correcty;
	private float correctz;

	public float getMaxX() {
		return maxX;
	}

	public float getMaxY() {
		return maxY;
	}

	public float getMaxZ() {
		return maxZ;
	}

	public float getMinX() {
		return minX;
	}

	public float getMinY() {
		return minY;
	}

	public float getMinZ() {
		return minZ;
	}

	
//	public GOCADConnector() {
//		resetBounds();
//	}
	
	/**
	 * translate the ogllayer objects into the center (0,0) of the scene build
	 * by the other ogllayers
	 * 
	 * @return corrected list of ogllayers
	 */
	private TSObject correctAll() {
		/*
		 * After generating TSObjects translate them into the origin of the
		 * scene
		 */
		correctx = (maxX + minX) / 2;
		correcty = (maxY + minY) / 2;
		correctz = (maxZ + minZ) / 2;

		maxX -= correctx;
		maxY -= correcty;
		maxZ -= correctz;

		minX -= correctx;
		minY -= correcty;
		minZ -= correctz;

		tsObject.convert(correctx, correcty, correctz);
		

		/*
		 * return the TSObjects
		 */
		return tsObject;
	}

	/**
	 * Reads in a single TSObject. It assumes that a 'HEAD' already has been
	 * read. Only fills L�nkedLists of the OGLLayer so it is recommended to use
	 * the convert method!
	 * 
	 * @param first
	 *            if it is the first tsObject which is read (important to
	 *            calculate min and max values for the coordinates)
	 * @return OGLLayer the OGLLayer, only the LinkedList and MetaData are
	 *         filled. use the convert method.
	 * @throws IOException
	 *             when an I/O error occurs
	 * @throws NumberFormatException
	 *             if there are not well formated values for the read data
	 * @throws TSFormatException
	 *             if the data is not in GOCAD format
	 */
	public TSObject readTSObject(BufferedReader in) throws IOException {
		tsObject = new TSObject();

		resetBounds();
		
		float tmpx;
		float tmpy;
		float tmpz;

		boolean head = true; // Flag for reading the head(->metadata)
		boolean body = false; // Flag for reading the body (->vertices and
		// indices)
		boolean end = false; // Flag to end reading of this OGLLayer

		String[] parsed = null;
		String actline = in.readLine();

		/*
		 * get every line
		 */
		while (!end && actline != null) {

			/*
			 * start with header
			 */
			if (head) {
				if (actline.startsWith("name:")) {
					/*
					 * read in the name if available
					 */
					tsObject.name = actline = actline.substring(5);
				} else if (actline.startsWith("*solid*color:")) {
					/*
					 * read in the color if available and transform it in
					 * Android-format
					 */
					actline = actline.substring(13).trim();
					parsed = actline.split(" ");
					if (parsed.length >= 3) {
						try {
							tsObject.color = Color.rgb((int) (Float
									.valueOf(parsed[0]) * 255), (int) (Float
									.valueOf(parsed[1]) * 255), (int) (Float
									.valueOf(parsed[2]) * 255));
						} catch (NumberFormatException ex) {
							Log
									.w(TAG, "Color not well formated: "
											+ actline, ex);
							throw new NumberFormatException(
									"Color not well formated: " + actline);
						}
					}

				} else if (actline.startsWith("}")) {
					/*
					 * check if the whole header has been read
					 */
					head = false;
					body = true;
				}
			}

			/*
			 * continue with body
			 */
			if (body) {

				if (actline.startsWith("PVRTX") || actline.startsWith("VRTX")) {
					/*
					 * do not make a difference between 'PVRTX' and 'VRTX'
					 */
					parsed = actline.split(" ");
					if (parsed.length == 6 || parsed.length == 5) {
						try {
							/*
							 * if a vertex found read its coordinates
							 */
							tmpx = Float.valueOf(parsed[2]);
							tmpy = Float.valueOf(parsed[3]);
							tmpz = Float.valueOf(parsed[4]);
							/*
							 * correct min an max values
							 */
							if (tmpx > maxX)
								maxX = tmpx;
							else if (tmpx < minX)
								minX = tmpx;

							if (tmpy > maxY)
								maxY = tmpy;
							else if (tmpy < minY)
								minY = tmpy;

							if (tmpz > maxZ)
								maxZ = tmpz;
							else if (tmpz < minZ)
								minZ = tmpz;
							/*
							 * and add the coordinates to the lists in the
							 * OGLLayer
							 */
							tsObject.vrtx.add(tmpx);
							tsObject.vrtx.add(tmpy);
							tsObject.vrtx.add(tmpz);

						} catch (NumberFormatException ex) {
							Log.w(TAG, "Vertices not well formated: " + actline,
									ex);
							throw new NumberFormatException(
									"Vertices not well formated: " + actline);
						}
					}
				} else if (body && actline.startsWith("TRGL")) {
					/*
					 * read in triangle-indices. every triangle belongs to three
					 * vertices.
					 */
					parsed = actline.split(" ");
					if (parsed.length == 4) {
						try {
							/*
							 * if triangle found put the indices into the
							 * TSObjects List for indices
							 */
							tsObject.trgl.add(Short.valueOf(parsed[1]));
							tsObject.trgl.add(Short.valueOf(parsed[2]));
							tsObject.trgl.add(Short.valueOf(parsed[3]));
						} catch (NumberFormatException ex) {
							Log.w(TAG, "Indices not well formated: " + actline,
									ex);
							throw new NumberFormatException(
									"Indices not well formated: " + actline);
						}
					}
				} else if (body && actline.matches("END")) {
					/*
					 * check if the end of the object is reached. use
					 * String.matches to check for end of object, because
					 * several tags in the GOCAD format already start with
					 * 'END'.
					 */
					body = false;
					end = true;
				}

			}
			if (!end) {
				actline = in.readLine();
			}
		}

		if (!end) {
			/*
			 * if no end has been reached, the file is inconsistent
			 */
			throw new TSFormatException(
					"File read complete, but no 'END' reached");
		}

		return correctAll();
	}

	/**
	 * sets the max and min values to default
	 */
	public void resetBounds() {
		maxY = Integer.MIN_VALUE;
		maxX = Integer.MIN_VALUE;
		maxZ = Integer.MIN_VALUE;
		minY = Integer.MAX_VALUE;
		minX = Integer.MAX_VALUE;
		minZ = Integer.MAX_VALUE;
	}

	public class TSFormatException extends RuntimeException {

		private static final long serialVersionUID = -8345108363313953426L;

		public TSFormatException(String msg) {
			super(msg);
		}
	}

	/**
	 * A OGLLayer consists of vertex and triangle - data and its metadata. It is
	 * also its own listener.
	 * 
	 * @author Mathias Menninghaus (mmenning@uos.de)
	 * @version 22.06.2009
	 * 
	 */
	public class TSObject implements OGLLayer {

		/*
		 * List of vertices and triangles
		 */
		private LinkedList<Float> vrtx = new LinkedList<Float>();

		private LinkedList<Short> trgl = new LinkedList<Short>();
		/*
		 * buffers for opengl|es output
		 */
		private ByteBuffer bbb;

		private FloatBuffer vertex;
		private ShortBuffer triangle;
		public String name = null;

		/*
		 * flags for the visualization in SurfaceRenderer
		 */
		private boolean fill = true;

		private boolean visible = true;
		private boolean selected = false;
		/*
		 * the color in android-format
		 */
		private int color = 0;

		private TSObject() {
			default_nr++;
		}

		/**
		 * returns color in android-format. if no color has been read a random
		 * color
		 * 
		 * @return read or random color (@see android.graphics.color)
		 */
		public int getColor() {
			if (color == 0) {
				color = Color.argb(255, (int) (Math.random() * 255),
						(int) (Math.random() * 255),
						(int) (Math.random() * 255));
				Log.d(TAG, "New Color: " + color + ": "
						+ ((float) Color.red(color)) / 255.0f + " "
						+ ((float) Color.green(color)) / 255.0f + " "
						+ ((float) Color.blue(color)) / 255.0f);
			}
			return color;
		}

		/**
		 * returns indices as an ShortBuffer in order of Index_1_of_Triangle_1,
		 * Index_2_of_Triangle_1, Index_2_of_Triangle_1, Index_1_of_Triangle_2,
		 * ... , Index_3_of_Triangle_n
		 * 
		 * @return
		 */
		public ShortBuffer getIndexBuffer() {
			return triangle;
		}

		/**
		 * returns the name of the OGLLayer. If no name has been read a default
		 * name according to a number.
		 * 
		 * @return read name or default-name
		 */
		public String getName() {
			if (name == null) {
				name = "DEFAULT#" + default_nr;
			}
			return name;
		}

		/**
		 * returns a FloatBuffer of indices in the order of: x1,y1,z1,x2,y2...
		 * 
		 * @return
		 */
		public FloatBuffer getVertexBuffer() {
			return vertex;
		}

		public boolean isFill() {
			return fill;
		}

		public boolean isSelected() {
			return selected;
		}

		public boolean isVisible() {
			return visible;
		}

		public void setFill(boolean fill) {
			this.fill = fill;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		public String toString() {
			return name;
		}

		/**
		 * Translates the vertices into the origin by using correction
		 * subtrahends because opengl is not able to display great
		 * coordinate-values.
		 * 
		 * @param correctx
		 *            subtrahend for x-coordinates
		 * @param correcty
		 *            subtrahend for y-coordinates
		 * @param correctz
		 *            subtrahend for z-coordinates
		 */
		private void convert(float correctx, float correcty, float correctz) {

			/*
			 * create Float and ShortBuffer for the use in OpenGL. Therefore
			 * they must be ordered native and allocated direct a float needs 4
			 * a short 2 bytes.
			 */
			bbb = ByteBuffer.allocateDirect(4 * vrtx.size());
			bbb.order(ByteOrder.nativeOrder());
			vertex = bbb.asFloatBuffer();

			/*
			 * fill a FloatBuffer with vertices and correct the position of
			 * every vertex by using correction subtrahends
			 */
			int i = 0;
			vertex.position(0);
			for (Float actual : vrtx) {
				switch (i) {
				case 0:
					vertex.put(actual - correctx);
					break;
				case 1:
					vertex.put(actual - correcty);
					break;
				case 2:
					vertex.put(actual - correctz);
					break;
				}
				i = ++i % 3;
			}

			/*
			 * Fill the ShortBuffer with indices. Assumption is that the first
			 * read vertex has index 1 and so on.
			 */
			bbb = ByteBuffer.allocateDirect(2 * trgl.size());
			bbb.order(ByteOrder.nativeOrder());
			triangle = bbb.asShortBuffer();
			triangle.position(0);
			for (Short actual : trgl) {
				/* in opengl|es indices always start with 0 */
				triangle.put((short) (actual - 1));
			}

			/*
			 * reference to the first entry otherwise it cannot be displayed in
			 * opengl|es
			 */
			vertex.position(0);
			triangle.position(0);

			/*
			 * set Lists null to free memory
			 */
			vrtx = null;
			trgl = null;
		}
	}
}