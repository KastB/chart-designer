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
package osmcd.gui.gpxtree;

import osmcd.OSMCDStrs;
import osmcd.data.gpx.gpx11.Gpx;
import osmcd.data.gpx.gpx11.MetadataType;
<<<<<<< HEAD
import osmcd.gui.mapview.GpxLayer;
=======
import osmcd.gui.mapview.layer.GpxLayer;
>>>>>>> f8aa735da6b335186129503e00a72e25e428f318

public class GpxRootEntry extends GpxEntry {

	public GpxRootEntry(GpxLayer layer) {
		this.setLayer(layer);
		this.setWaypointParent(true);
	}

	public String toString() {
		String name = getMetaDataName();
		if (name != null && !name.equals("")) {
			return name;
		} else {
			if (getLayer().getFile() == null) {
				return OSMCDStrs.RStr("rp_gpx_root_default_name_nofile");
			} else {
				return String.format(OSMCDStrs.RStr("rp_gpx_root_default_name_hasfile"), getLayer()
						.getFile().getName());
			}
		}
	}

	public String getMetaDataName() {
		try {
			return getLayer().getGpx().getMetadata().getName();
		} catch (NullPointerException e) {
			return null;
		}
	}

	public void setMetaDataName(String name) {
		Gpx gpx = getLayer().getGpx();
		if (gpx.getMetadata() == null)
			gpx.setMetadata(new MetadataType());
		gpx.getMetadata().setName(name);

		// Notify the model about the changed node text
		getLayer().getPanel().getTreeModel().nodeChanged(getNode());
	}
}
