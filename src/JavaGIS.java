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
			java.sql.Connection conn = Utils.getConnection();
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
			}
			
			// Graphique
			MapPanel mp = new MapPanel(5.75, 45.15, 0.2);
			geoexplorer.gui.Point point = new geoexplorer.gui.Point(5.73644115, 45.18644215);
			point.setShape(point.CROSS);
			mp.addPrimitive(point);
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
