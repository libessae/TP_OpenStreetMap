import geoexplorer.gui.GeoMainFrame;
import geoexplorer.gui.GraphicalPrimitive;
import geoexplorer.gui.MapPanel;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgis.*;
import database.Utils;

public class JavaGIS {
	public static void main(String[] args) {
		if (args.length == 0) {
			
			// Fonction de base
			/*java.sql.Connection conn = Utils.getConnection();
			PreparedStatement stmt;

			try {
				stmt = conn
						.prepareStatement("select ST_X(ST_Centroid(bbox)),ST_Y(ST_Centroid(bbox)), bbox from ways where tags->'amenity'='townhall' and (tags->'name' like '%Grenoble%')");

				ResultSet res = stmt.executeQuery();
				while (res.next()) {
					System.out.println("longitude centroid = " + res.getDouble(1)
							+ "; latitude centroid = " + res.getDouble(2)
							+ "; bbox = " + ((PGgeometry) res.getObject(3)).getGeometry());
				}

				res.close();
				Utils.closeConnection();

			} catch (SQLException e) {
				Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null,
						e);
			}*/
			
			// Graphique
			
			MapPanel mp = new MapPanel(916074.4, 6453841.3, 15000);
			
			java.sql.Connection conn = Utils.getConnection();
			PreparedStatement stmt;

			try {
				stmt = conn
						.prepareStatement("SELECT ST_Transform(linestring, 2154) FROM ways WHERE tags->'highway' LIKE '%' AND ST_Intersects(linestring, ST_Setsrid(ST_GeomFromText('Polygon((5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1, 5.8 45.1))'),4326))");

				ResultSet res = stmt.executeQuery();
				while (res.next()) {
					geoexplorer.gui.LineString linestring = new geoexplorer.gui.LineString();
					System.out.println("linestring = " + ((PGgeometry) res.getObject(1)).getGeometry());
					PGgeometry geom = (PGgeometry)res.getObject(1);
					LineString ls = (LineString)geom.getGeometry();
					for( int p = 0; p < ls.numPoints(); p++ )
					{
						Point pt = ls.getPoint(p);
						linestring.addPoint(new geoexplorer.gui.Point(pt.x, pt.y));
					}
					mp.addPrimitive(linestring);


				}
				res.close();
				Utils.closeConnection();

			} catch (SQLException e) {
				Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null,
						e);
			}
			
			/*geoexplorer.gui.Point point = new geoexplorer.gui.Point(5.73644115, 45.18644215);
			point.setShape(point.CROSS);
			mp.addPrimitive(point);*/
			new GeoMainFrame("MapPanel", mp);		
			
		} else {
			java.sql.Connection conn = Utils.getConnection();
			PreparedStatement stmt;

			try {
				stmt = conn
						.prepareStatement("SELECT tags->'name', ST_X(geom), ST_Y(geom) FROM nodes WHERE tags->'name' LIKE ?");
				stmt.setString(1, args[0]);
				ResultSet res = stmt.executeQuery();
				while (res.next()) {
					System.out.println("nom = " + res.getString(1));
					System.out.println("longitude = " + res.getDouble(2));
					System.out.println("latitude = " + res.getDouble(3));
				}

				res.close();
				Utils.closeConnection();

			} catch (SQLException e) {
				Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null,
						e);
			}
		}
	}
}
