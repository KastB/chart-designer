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
/**
 * Package level definition of adapters for JAXB 
 */
@XmlJavaTypeAdapters({ @XmlJavaTypeAdapter(value = PointAdapter.class, type = java.awt.Point.class),
		@XmlJavaTypeAdapter(value = DimensionAdapter.class, type = java.awt.Dimension.class),
		@XmlJavaTypeAdapter(value = PolygonAdapter.class, type = java.awt.Polygon.class) })
package osmcd.program;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

<<<<<<< HEAD:src/main/java/osmcd/program/package-info.java
import osmb.program.jaxb.DimensionAdapter;
import osmb.program.jaxb.PointAdapter;
import osmb.program.jaxb.PolygonAdapter;
=======
import osmcb.program.jaxb.DimensionAdapter;
import osmcb.program.jaxb.PointAdapter;
import osmcb.program.jaxb.PolygonAdapter;
>>>>>>> f8aa735da6b335186129503e00a72e25e428f318:src/main/java/osmcd/program/model/package-info.java

