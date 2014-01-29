import geoexplorer.gui.GeoMainFrame;
import geoexplorer.gui.GraphicalPrimitive;
import geoexplorer.gui.MapPanel;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Color;

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
			
			Map <String, Color> couleur = new HashMap<String,Color>(); 
			couleur.put("motorway", Color.blue);
			couleur.put("trunk", Color.green);
			couleur.put("primary", Color.red);
			couleur.put("secondary", new Color(237,127,16));
			couleur.put("tertiary", new Color(255,255,20));
			couleur.put("unclassified", new Color(88,41,0));
			couleur.put("residential", Color.lightGray);
			couleur.put("service", Color.pink);

			
			MapPanel mp = new MapPanel(915000, 6456500, 15000);
			
			java.sql.Connection conn = Utils.getConnection();
			PreparedStatement stmt;

			try {
			/*	stmt = conn
						.prepareStatement("SELECT ST_Transform(linestring, 2154), tags->'highway' FROM ways WHERE tags->'highway' LIKE '%' AND ST_Intersects(linestring, ST_Setsrid(ST_GeomFromText('Polygon((5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1, 5.8 45.1))'),4326))");

				ResultSet res = stmt.executeQuery();
				geoexplorer.gui.LineString linestring;
				while (res.next()) {
					String highway = res.getString(2);
					Color color = couleur.get(highway);
					if ( color != null) {
						linestring = new geoexplorer.gui.LineString(color);
					}else{
						linestring = new geoexplorer.gui.LineString();
					}
					
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
				
				stmt = conn
						.prepareStatement("SELECT ST_Transform(bbox, 2154) FROM ways WHERE tags->'building' LIKE '%' AND ST_Intersects(linestring, ST_Setsrid(ST_GeomFromText('Polygon((5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1, 5.8 45.1))'),4326))");

				res = stmt.executeQuery();
				geoexplorer.gui.Polygon polygone;
				while (res.next()) {
					polygone = new geoexplorer.gui.Polygon();
					
					PGgeometry geom = (PGgeometry)res.getObject(1);
					Polygon pol = (Polygon)geom.getGeometry();
					for( int p = 0; p < pol.numPoints(); p++ )
					{
						Point pt = pol.getPoint(p);
						polygone.addPoint(new geoexplorer.gui.Point(pt.x, pt.y));
					}
					mp.addPrimitive(polygone);


				}*/
								
				stmt = conn
						.prepareStatement("SELECT q.quartier, q.the_geom, COUNT(n) FROM quartier q, nodes n WHERE n.tags->'shop'='bakery' AND ST_Intersects(ST_Transform(q.the_geom, 4326), n.geom) GROUP BY q.quartier, q.the_geom ORDER BY COUNT(n) DESC");

				ResultSet res = stmt.executeQuery();
				geoexplorer.gui.Polygon polygone;
				while (res.next()) {
					Color couleur_quartier;
					int nb_boulangeries = res.getInt(3);
					if (nb_boulangeries < 5){
						couleur_quartier = Color.yellow;
					}else if (nb_boulangeries < 10){
						couleur_quartier = Color.orange;
					}else{
						couleur_quartier = Color.red;
					}
					polygone = new geoexplorer.gui.Polygon(Color.black, couleur_quartier);
					
					PGgeometry geom = (PGgeometry)res.getObject(2);
					MultiPolygon pol = (MultiPolygon)geom.getGeometry();
					for( int p = 0; p < pol.numPoints(); p++ )
					{
						Point pt = pol.getPoint(p);
						polygone.addPoint(new geoexplorer.gui.Point(pt.x, pt.y));
					}
					mp.addPrimitive(polygone);


				}
				res.close();
				
				stmt = conn
						.prepareStatement("SELECT ST_Transform(linestring, 2154) FROM ways WHERE tags->'highway' LIKE '%' AND ST_Intersects(linestring, ST_Setsrid(ST_GeomFromText('Polygon((5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1, 5.8 45.1))'),4326))");

				res = stmt.executeQuery();
				geoexplorer.gui.LineString linestring;
				while (res.next()) {
					linestring = new geoexplorer.gui.LineString(Color.darkGray);
					
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
