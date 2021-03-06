/*******************************************************************************
 * Copyright (c) OSMCB developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package osmcd.gui.mapview;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import osmb.mapsources.ACMapSource;
import osmb.mapsources.MP2Corner;
import osmb.mapsources.MP2MapSpace;
import osmb.mapsources.TileAddress;
import osmb.program.JobDispatcher;
import osmb.program.tiles.IfTileLoaderListener;
import osmb.program.tiles.MemoryTileCache;
import osmb.program.tiles.Tile;
import osmb.program.tiles.TileLoader;

/**
 * 
 * Provides a simple panel that displays rendered map tiles loaded from a specified map source.
 * 
 * @author Jan Peter Stotz
 * 
 */
// public class JMapViewer extends JPanel implements IfTileLoaderListener, IfMemoryTileCacheHolder
public class JMapViewer extends JPanel implements IfTileLoaderListener
{
	private static final long serialVersionUID = 1L;
	protected static Logger log = Logger.getLogger(JMapViewer.class);

	/**
	 * Vectors for clock-wise tile painting
	 */
	protected static final Point[] move =
	{ new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1) };

	protected TileLoader tileLoader = null;
	/**
	 * The mapTileLayers use this to actually paint the tiles on the graphics context
	 */
	protected MemoryTileCache mTileCache = null;
	protected ACMapSource mMapSource = null;
	protected boolean usePlaceHolderTiles = true;

	protected boolean mapMarkersVisible = false;
	protected MapGridLayer mapGridLayer = null;

	protected List<IfMapTileLayer> mapTileLayers = null;
	public List<IfMapLayer> mapLayers = null;

	/**
	 * x- and y-position of the center of this map-panel on the world map denoted in screen pixel regarding the current zoom level.
	 * <p>
	 * Setting center to (width / 2 , height / 2):
	 * <ul>
	 * <li>width (height) % 2 == 0 : center is right (bottom) pixel of (2 pixel-)center
	 * <li>width (height) % 2 == 1 : center is (1 pixel-)center
	 */
	protected Point center = new Point();

	/**
	 * The minimum zoom level available for the currently displayed map. This will be modified when the map source is changed.
	 * It is the bigger of {@link IfMapSpace.MIN_TECH_ZOOM} and {@link IfMapSource.getMinZoom()}
	 */
	protected int mMinZoom = MP2MapSpace.MIN_TECH_ZOOM;

	/**
	 * The maximum zoom level available for the currently displayed map. This will be modified when the map source is changed.
	 * It is the smaller of {@link IfMapSpace.MAX_TECH_ZOOM} and {@link IfMapSource.getMaxZoom()}
	 */
	protected int mMaxZoom = MP2MapSpace.MAX_TECH_ZOOM;

	/**
	 * Current zoom level
	 */
	protected int mZoom;

	/**
	 * The JobDispatcher is a 'normal' class, not a singleton any longer. {@link JobDispatcher}.
	 */
	protected JobDispatcher mJobDispatcher = null;

	public JMapViewer(ACMapSource defaultMapSource, int downloadThreadCount)
	{
		super();
		mapTileLayers = new LinkedList<IfMapTileLayer>();
		mapLayers = new LinkedList<IfMapLayer>();
		mTileCache = new MemoryTileCache(2000); // reasonable cache size for interactive map display
		tileLoader = new TileLoader(this, mTileCache);
		mapMarkersVisible = true;

		setLayout(null);
		mJobDispatcher = new JobDispatcher(downloadThreadCount); // mJobDispatcher is used in setMapSource(defaultMapSource);
		setMapSource(defaultMapSource);
		setMinimumSize(new Dimension(MP2MapSpace.TECH_TILESIZE, MP2MapSpace.TECH_TILESIZE));
		setPreferredSize(new Dimension(5 * MP2MapSpace.TECH_TILESIZE, 3 * MP2MapSpace.TECH_TILESIZE));
		setDisplayPositionByLatLon(52.0, 7.0, 8);
	}

	/**
	 * Changes the map pane so that it is centered on the specified coordinate at the given zoom level.
	 * 
	 * @param lat
	 *          latitude of the specified coordinate
	 * @param lon
	 *          longitude of the specified coordinate
	 * @param zoom
	 *          {@link #mMinZoom} <= zoom level <= {@link #mMaxZoom}
	 */
	public void setDisplayPositionByLatLon(double lat, double lon, int zoom)
	{
		setDisplayPositionByLatLon(new Point(getWidth() / 2, getHeight() / 2), lat, lon, zoom);
	}

	/**
	 * Changes the map pane so that the specified coordinate at the given zoom level is displayed on the map at the screen coordinate <code>mapPoint</code>.
	 * 
	 * @param mapPoint
	 *          point on the map denoted in pixels where the coordinate should be set
	 * @param lat
	 *          latitude of the specified coordinate
	 * @param lon
	 *          longitude of the specified coordinate
	 * @param zoom
	 *          {@link #mMinZoom} <= zoom level <= {@link #mMaxZoom}
	 */
	public void setDisplayPositionByLatLon(Point mapPoint, double lat, double lon, int zoom)
	{
		int x = MP2MapSpace.cLonToXIndex(lon, zoom);
		int y = MP2MapSpace.cLatToYIndex(lat, zoom);
		setDisplayPosition(mapPoint, x, y, zoom);
	}

	public void setDisplayPosition(int x, int y, int zoom)
	{
		setDisplayPosition(new Point(getWidth() / 2, getHeight() / 2), x, y, zoom);
	}

	/**
	 * 
	 * @param mapPoint
	 *          screen position
	 * @param x
	 *          MP2Pixel.x
	 * @param y
	 *          MP2Pixel.y
	 * @param zoom
	 *          MP2Pixel.zoom
	 */
	public void setDisplayPosition(Point mapPoint, int x, int y, int zoom)
	{
		Point p = new Point();
		int nSize = MP2MapSpace.getSizeInPixel(zoom);

		// horizontal position
		if (nSize < getWidth()) // width of map smaller than width of JMapViewer (widthMap < widthScreen)
			p.x = nSize / 2;
		else if (mapPoint.x > x) // widthMap >= widthScreen && margin at left side of map
			p.x = getWidth() / 2; // left-aligned
		else if (getWidth() - mapPoint.x > nSize - x) // widthMap >= widthScreen && margin at right side of map
			p.x = nSize - getWidth() / 2 + 1; // right-aligned
		else
			p.x = x - mapPoint.x + getWidth() / 2;

		// vertical position
		if (nSize < getHeight()) // height of map smaller than height of JMapViewer (heightMap < heightScreen)
			p.y = nSize / 2;
		else if (mapPoint.y > y) // heightMap >= heightScreen && margin at top side of map
			p.y = getHeight() / 2; // top-aligned
		else if (getHeight() - mapPoint.y > nSize - y) // heightMap >= heightScreen && margin at bottom side of map
			p.y = nSize - getHeight() / 2 + 1; // bottom-aligned
		else
			p.y = y - mapPoint.y + getHeight() / 2;

		center = p;
		log.trace("center.x = " + p.x + ", center.y = " + p.y);

		setIgnoreRepaint(true);
		try
		{
			int oldZoom = this.mZoom;
			this.mZoom = zoom;
			if (oldZoom != zoom)
				zoomChanged(oldZoom); // !!! if instance of PreviewMap, this notifies mapEventListeners
		}
		finally
		{
			setIgnoreRepaint(false);
			repaint();
		}
	}

	/**
	 * Sets the displayed map pane and zoom level so that the two points (x1/y1) and (x2/y2) visible. Please note that the coordinates have to be specified
	 * regarding {@link MP2MapSpace#MAX_TECH_ZOOM}.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	// W chaotic! MP2MapSpace.MAX_TECH_ZOOM was formerly reference value
	public void setDisplayToFitPixelCoordinates(int x1, int y1, int x2, int y2)
	{
		int mapZoomMax = mMapSource.getMaxZoom();
		int height = Math.max(0, getHeight());
		int width = Math.max(0, getWidth());
		int newZoom = mMapSource.getMaxZoom();
		int x = Math.abs(x1 - x2);
		int y = Math.abs(y1 - y2);
		while (x >= width || y >= height || newZoom > mapZoomMax)
		{
			newZoom--;
			x >>= 1;
			y >>= 1;
		}

		// Do not select a zoom level that is unsupported by the current map source
		newZoom = Math.max(mMapSource.getMinZoom(), Math.min(mMapSource.getMaxZoom(), newZoom));

		x = Math.min(x2, x1) + Math.abs(x1 - x2) / 2;
		y = Math.min(y2, y1) + Math.abs(y1 - y2) / 2;
		int z = 1 << (mapZoomMax - newZoom); // (MP2MapSpace.MAX_TECH_ZOOM - newZoom);
		x /= z;
		y /= z;
		// setDisplayPosition(x, y, newZoom); // W +1, +1
		setDisplayPosition(x + 1, y + 1, newZoom); // set center: see protected Point center = new Point();
	}

	/**
	 * @return The geo coordiantes of the center position ????
	 */
	public Point2D.Double getPosition()
	{
		double lon = MP2MapSpace.cXToLonPixelCenter(center.x, mZoom); // W #mapSpace mapSpace.cXToLon(center.x, mZoom);
		double lat = MP2MapSpace.cYToLatPixelCenter(center.y, mZoom); // W #mapSpace mapSpace.cYToLat(center.y, mZoom);
		return new Point2D.Double(lat, lon);
	}

	// W #??? test int <-> double
	/**
	 * This calculates the position of the mouse pointer as ???
	 * 
	 * @param mapPoint
	 *          position of mouse pointer in screen coordinates
	 * @return
	 */
	// position of mouse pointer to (ms, x, y, mZoom)
	public MP2Corner getPositionMPC(Point mapPoint)
	{
		int x = center.x + mapPoint.x - getWidth() / 2;
		int y = center.y + mapPoint.y - getHeight() / 2;
		log.info("center.x + mapPoint.x - getWidth() / 2 = " + x + ", center.y + mapPoint.y - getHeight() / 2 = " + y);
		log.info("center.x = " + center.x + ", mapPoint.x = " + mapPoint.x + ", getWidth() / 2 = " + getWidth() / 2 + ", center.y = " + center.y + ", mapPoint.y = "
		    + mapPoint.y + ", getHeight() / 2 = " + getHeight() / 2);
		MP2Corner mpcPos = new MP2Corner(x, y, mZoom);
		log.info("mpcPos.x = " + mpcPos.getX() + ", mpcPos.y = " + mpcPos.getY());
		return mpcPos;
	}

	/**
	 * This calculates the position of the mouse pointer in (lat | lon) 'geo' coordinates
	 * 
	 * @param mapPoint
	 *          position of mouse pointer in screen coordinates
	 * @return map pixel in (double lat| double lon) geo coordinates
	 */
	public Point2D.Double getPosition(Point mapPoint)
	{
		// W #mapSpace IfMapSpace mapSpace = mMapSource.getMapSpace();
		int x = center.x + mapPoint.x - getWidth() / 2;
		int y = center.y + mapPoint.y - getHeight() / 2;
		// log.info("center.x = " + center.x + ", mapPoint.x = " + mapPoint.x + ", getWidth() / 2 = " + getWidth() / 2 + ", center.y = " + center.y + ", mapPoint.y
		// = " + mapPoint.y + ", getHeight() / 2 = " + getHeight() / 2);
		double lon = MP2MapSpace.cXToLonPixelCenter(x, mZoom); // W #mapSpace mapSpace.cXToLon(x, mZoom);
		double lat = MP2MapSpace.cYToLatPixelCenter(y, mZoom); // W #mapSpace mapSpace.cYToLat(y, mZoom);
		log.info("lon.x = " + lon + ", lat.y = " + lat);
		return new Point2D.Double(lat, lon);
	}

	/**
	 * Calculates the position on the map of a given coordinate
	 * 
	 * @param lat
	 * @param lon
	 * @return point on the map or <code>null</code> if the point is not visible
	 */
	public Point getMapPosition(double lat, double lon)
	{
		int x = MP2MapSpace.cLonToXIndex(lon, mZoom);
		int y = MP2MapSpace.cLatToYIndex(lat, mZoom);
		x -= center.x - getWidth() / 2;
		y -= center.y - getHeight() / 2;
		if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) // W x >= getWidth(), y >= getHeight()
			return null;
		return new Point(x, y);
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		int nTiles = 0;
		Graphics2D g = (Graphics2D) graphics;
		// if (mapIsMoving) {
		// mapIsMoving = false;
		// Doesn't look very pretty but is much more faster
		// g.copyArea(0, 0, getWidth(), getHeight(), -mapMoveX, -mapMoveY);
		// return;
		// }
		super.paintComponent(g);

		int iMove = 0;

		int tileSize = MP2MapSpace.getTileSize();

		int tilex = center.x / tileSize;
		int tiley = center.y / tileSize;
		int off_x = (center.x % tileSize);
		int off_y = (center.y % tileSize);
		log.debug("tX=" + tilex + ", tY=" + tiley + ", oX=" + off_x + ", oY=" + off_y);

		int w2 = getWidth() / 2;
		int h2 = getHeight() / 2;
		int topLeftX = center.x - w2;
		int topLeftY = center.y - h2;
		log.debug("w2=" + w2 + ", h2=" + h2 + ", tlX=" + topLeftX + ", tlY=" + topLeftY);

		int posx = w2 - off_x;
		int posy = h2 - off_y;
		log.debug("pX=" + posx + ", pY=" + posy);

		int diff_left = off_x;
		int diff_right = tileSize - off_x;
		int diff_top = off_y;
		int diff_bottom = tileSize - off_y;

		boolean start_left = diff_left < diff_right;
		boolean start_top = diff_top < diff_bottom;

		if (start_top)
		{
			if (start_left)
				iMove = 2;
			else
				iMove = 3;
		}
		else
		{
			if (start_left)
				iMove = 1;
			else
				iMove = 0;
		} // calculate the visibility borders
		int x_min = -tileSize + 1; // W + 1 inserted
		int y_min = -tileSize + 1; // W + 1 inserted
		int x_max = getWidth() - 1; // W - 1 inserted
		int y_max = getHeight() - 1; // W - 1 inserted
		log.debug("xMin=" + x_min + ", yMin=" + y_min + ", xMax=" + x_max + ", yMax=" + y_max);

		// paint the tiles in a spiral, starting from center of the map
		boolean painted = (mapTileLayers.size() > 0);
		for (IfMapTileLayer l : mapTileLayers)
		{
			l.startPainting(mMapSource);
		}
		int x = 0;
		while (painted)
		{
			painted = false;
			for (int i = 0; i < 4; i++)
			{
				if (i % 2 == 0)
					x++;
				for (int j = 0; j < x; j++)
				{
					if ((x_min <= posx) && (posx <= x_max) && (y_min <= posy) && (posy <= y_max))
					{
						// tile is visible
						if ((tilex >= 0) && (tilex < (1 << mZoom)) && (tiley >= 0) && (tiley < (1 << mZoom)))
						{
							// tile exists in the world
							painted = true;
							for (IfMapTileLayer l : mapTileLayers)
							{
								log.debug("paint tile=" + new TileAddress(tilex, tiley, mZoom));
								l.paintTile(g, posx, posy, tilex, tiley, mZoom);
								++nTiles;
							}
						}
					}
					Point p = move[iMove];
					posx += p.x * tileSize;
					posy += p.y * tileSize;
					tilex += p.x;
					tiley += p.y;
				}
				iMove = (iMove + 1) % move.length;
			}
		}

		int bottomRightX = topLeftX + getWidth();
		int bottomRightY = topLeftY + getHeight();
		try
		{
			for (IfMapLayer l : mapLayers)
			{
				l.paint(this, g, mZoom, topLeftX, topLeftY, bottomRightX, bottomRightY);
			}
		}
		catch (ConcurrentModificationException e)
		{
			// This may happen when multiple GPX files are loaded at once and in the mean time the map view is repainted.
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					JMapViewer.this.repaint();
				}
			});
		}

		// outer border of the map
		int mapSize = tileSize << mZoom;
		g.setColor(Color.BLACK);
		g.drawRect(w2 - center.x, h2 - center.y, mapSize, mapSize);

		// g.drawString("Tiles in cache: " + tileCache.getTileCount(), 50, 20);
		log.debug("painted tiles=" + nTiles);
	}

	/**
	 * Moves the visible map pane.
	 * 
	 * @param x
	 *          horizontal movement in pixel.
	 * @param y
	 *          vertical movement in pixel
	 */
	public void moveMap(int x, int y)
	{
		Point screenCenter = new Point(getWidth() / 2, getHeight() / 2);
		setDisplayPosition(screenCenter, center.x + x, center.y + y, mZoom);

		// center.x += x;
		// center.y += y;
		// repaint();
	}

	/**
	 * @return The current zoom level.
	 */
	public int getZoom()
	{
		return mZoom;
	}

	/**
	 * Increases the current zoom level by one.
	 */
	public void zoomIn()
	{
		setZoom(mZoom + 1);
	}

	/**
	 * Increases the current zoom level by one.
	 */
	public void zoomIn(Point mapPoint)
	{
		setZoom(mZoom + 1, mapPoint);
	}

	/**
	 * Decreases the current zoom level by one.
	 */
	public void zoomOut()
	{
		setZoom(mZoom - 1);
	}

	/**
	 * Decreases the current zoom level by one.
	 */
	public void zoomOut(Point mapPoint)
	{
		setZoom(mZoom - 1, mapPoint);
	}

	/**
	 * W #??? // W test int <-> double
	 * 
	 * @param zoom
	 * @param mapPoint
	 */
	public void setZoomMPC(int zoom, Point mapPoint)
	{
		if (zoom == mZoom)
			return;
		int nSize = MP2MapSpace.getSizeInPixel(mZoom);
		if (zoom < mZoom && getHeight() > nSize && getWidth() > nSize)
			return;
		if (zoom < mMinZoom)
			return;
		if (zoom > mMaxZoom)
			return;
		mJobDispatcher.cancelOutstandingJobs(); // Clearing outstanding load requests

		MP2Corner zoomPos = getPositionMPC(mapPoint);
		// log.info("xx=" + zoomPos.getX() + ", yy" + zoomPos.getY() + ", zoom=" + zoom + ", mapPoint=" + mapPoint);
		zoomPos = zoomPos.adaptToZoomlevel(zoom);
		// log.info("x=" + zoomPos.getX() + ", y" + zoomPos.getY() + ", zoom=" + zoom + ", mapPoint=" + mapPoint);
		setDisplayPosition(mapPoint, zoomPos.getX(), zoomPos.getY(), zoom);
	}

	/**
	 * W #??? // W test int <-> double
	 * This is the method which is called before every zoom change. So here we limit the allowed zoom levels.
	 * 
	 * @param zoom
	 * @param mapPoint
	 */
	public void setZoom(int zoom, Point mapPoint)
	{
		setZoomMPC(zoom, mapPoint); // W #??? test int <-> double
		// if (zoom == this.mZoom)
		// return;
		// log.info("new zoom=" + zoom);
		// mJobDispatcher.cancelOutstandingJobs(); // Clearing outstanding load requests
		// zoom = Math.max(zoom, Math.max(MP2MapSpace.MIN_TECH_ZOOM, mMapSource.getMinZoom()));
		// zoom = Math.min(zoom, Math.min(MP2MapSpace.MAX_TECH_ZOOM, mMapSource.getMaxZoom()));
		// // mZoom = zoom; // This is later done by setDisplayPositionByLatLon()
		// Point2D.Double zoomPos = getPosition(mapPoint);
		// setDisplayPositionByLatLon(mapPoint, zoomPos.x, zoomPos.y, zoom);
	}

	public void setZoom(int zoom)
	{
		setZoom(zoom, new Point(getWidth() / 2, getHeight() / 2));
		repaint();
	}

	/**
	 * Called by {@link #setDisplayPosition(Point, int, int, int)}, if instance of PreviewMap, it notifies mapEventListeners.
	 * 
	 * Every time the zoom level changes this method is called. Override it in derived implementations for adapting zoom dependent values. The new zoom level can
	 * be obtained via {@link #getZoom()}.
	 * 
	 * @param oldZoom
	 *          the previous zoom level
	 */
	protected void zoomChanged(int oldZoom)
	{
	}

	public boolean isTileGridVisible()
	{
		return (mapGridLayer != null);
	}

	public void setTileGridVisible(boolean tileGridVisible)
	{
		if (isTileGridVisible() == tileGridVisible)
			return;
		if (tileGridVisible)
		{
			mapGridLayer = new MapGridLayer();
			addMapTileLayers(mapGridLayer);
		}
		else
		{
			removeMapTileLayers(mapGridLayer);
			mapGridLayer = null;
		}
		repaint();
	}

	public boolean getMapMarkersVisible()
	{
		return mapMarkersVisible;
	}

	public MemoryTileCache getTileImageCache()
	{
		return mTileCache;
	}

	public TileLoader getTileLoader()
	{
		return tileLoader;
	}

	public ACMapSource getMapSource()
	{
		return mMapSource;
	}

	public void setMapSource(ACMapSource mapSource)
	{
		this.mMapSource = mapSource;
		mMinZoom = Math.max(mapSource.getMinZoom(), MP2MapSpace.MIN_TECH_ZOOM);
		mMaxZoom = Math.min(mapSource.getMaxZoom(), MP2MapSpace.MAX_TECH_ZOOM);
		mJobDispatcher.cancelOutstandingJobs();
		if (mZoom > mMaxZoom)
			setZoom(mMaxZoom);
		if (mZoom < mMinZoom)
			setZoom(mMinZoom);
		mapTileLayers.clear();
		log.info("Map layer changed to: " + mapSource);
		mapTileLayers.add(new DefaultMapTileLayer(this, mapSource));
		if (mapGridLayer != null)
			mapTileLayers.add(mapGridLayer);
		repaint();
	}

	public JobDispatcher getJobDispatcher()
	{
		return mJobDispatcher;
	}

	public boolean isUsePlaceHolderTiles()
	{
		return usePlaceHolderTiles;
	}

	/**
	 * The loader has finished to load the tile. Draw it on the map view.
	 */
	@Override
	public void tileLoadingFinished(Tile tile, boolean success)
	{
		if (success)
		{
			mTileCache.addTile(tile);
			log.debug(tile + " successfully loaded");
		}
		else
		{
			log.debug(tile + " download failed");
		}
		repaint();
	}

	/**
	 * A tile was downloaded from the online map source. Display some info.
	 */
	@Override
	public void tileDownloaded(Tile tile, int size)
	{
		log.trace(tile + " loaded from online map source, size=" + size);
	}

	/**
	 * A tile was loaded from the local cache. Display some info.
	 */
	@Override
	public void tileLoadedFromCache(Tile tile, int size)
	{
		log.trace(tile + " loaded from mtc, size=" + size);
	}

	/**
	 * Adds one layer to the current list of tile layers for this map viewer
	 * 
	 * @param mapTileLayer
	 *          The layer to be added to the current list of tile layers
	 */
	public void addMapTileLayers(IfMapTileLayer mapTileLayer)
	{
		mapTileLayers.add(mapTileLayer);
	}

	public void removeMapTileLayers(IfMapTileLayer mapTileLayer)
	{
		mapTileLayers.remove(mapTileLayer);
	}

	/**
	 * @return the maximum zoom level
	 */
	public int getMaxZoom()
	{
		return mMaxZoom;
	}

	/**
	 * @param maxZoom
	 *          the maximum zoom level to set
	 */
	public void setMaxZoom(int maxZoom)
	{
		mMaxZoom = maxZoom;
	}

	/**
	 * @return the minimum zoom level
	 */
	public int getMinZoom()
	{
		return mMinZoom;
	}

	/**
	 * @param mMinZoom
	 *          the minimum zoom level to set
	 */
	public void setMinZoom(int minZoom)
	{
		mMinZoom = minZoom;
	}
}
