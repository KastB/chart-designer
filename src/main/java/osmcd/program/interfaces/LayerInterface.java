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
package osmcd.program.interfaces;

import java.awt.Point;

import osmcd.exceptions.InvalidNameException;
import osmcd.program.model.EastNorthCoordinate;
import osmcd.program.model.TileImageParameters;

public interface LayerInterface extends BundleObject, Iterable<MapInterface>, CapabilityDeletable
{

	public void addMap(MapInterface map);

	public int getMapCount();

	public MapInterface getMap(int index);

	public BundleInterface getAtlas();

	public long calculateTilesToDownload();

	public LayerInterface deepClone(BundleInterface atlas);

	public int getZoomLvl();

	public void setZoomLvl(int nZoomLvl);

	public void addMapsAutocut(String mapNameBase, MapSource mapSource, EastNorthCoordinate minCoordinate, EastNorthCoordinate maxCoordinate, int zoom,
			TileImageParameters parameters, int maxMapSize) throws InvalidNameException;

	public void addMapsAutocut(String mapNameBase, MapSource mapSource, Point minTileCoordinate, Point maxTileCoordinate, int zoom,
			TileImageParameters parameters, int maxMapSize, int overlapTiles) throws InvalidNameException;
}
