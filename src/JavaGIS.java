import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgis.*;
import database.Utils;

public class JavaGIS {
	public static void main(String[] args) {
		if (args.length == 0) {
			java.sql.Connection conn = Utils.getConnection();
			PreparedStatement stmt;

			try {
				stmt = conn
						.prepareStatement("select ST_X(ST_Centroid(bbox)),ST_Y(ST_Centroid(bbox)), bbox from ways where tags->'amenity'='townhall' and (tags->'name' like '%Grenoble%')");

				ResultSet res = stmt.executeQuery();
				while (res.next()) {
					System.out.println("longitude centroid = " + res.getDouble(1)
							+ "; latitude centroid = " + res.getDouble(2)
							+ "; bbox = "
							+ ((PGgeometry) res.getObject(3)).getGeometry());
				}

				res.close();
				Utils.closeConnection();

			} catch (SQLException e) {
				Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null,
						e);
			}
		} else {
			java.sql.Connection conn = Utils.getConnection();
			PreparedStatement stmt;

			try {
				stmt = conn
						.prepareStatement("SELECT tags->'name' FROM ways WHERE tags->'name' LIKE ?");
				stmt.setString(1, args[0]);
				ResultSet res = stmt.executeQuery();
				while (res.next()) {
					System.out.println("nom = " + res.getString(1));
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
