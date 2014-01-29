import geoexplorer.gui.GeoMainFrame;
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

	/*
	 * Fonction qui affiche le menu de l'application
	 */
	private static void affiche_menu() {
		System.out
				.println("Pour tester notre application, veuillez passer en argument le numéro de la question que vous souhaitez tester :");
		System.out
				.println("\t - 8 => programme simple pour tester la connection à la base de données.");
		System.out
				.println("\t - 9 $name => affiche tous les noms et coordonnées géographiques des points dont le nom ressemble à $name.");
		System.out
				.println("\t - 10 => affiche toutes les routes et tous les bâtiments autour de Grenoble.");
		System.out
				.println("\t - 11 => affiche le nombre de boulangeries par quartier à Grenoble.");
	}

	/*
	 * Main de l'application
	 */
	public static void main(String[] args) {

		/* L'utilisateur doit passer en paramètre la question qu'il veut tester */
		if (args.length > 0) {

			java.sql.Connection conn;
			PreparedStatement stmt;

			switch (args[0]) {

			case "8":
				// Question 8 : Fonction de base

				try {
					conn = Utils.getConnection();
					stmt = conn
							.prepareStatement("SELECT ST_X(ST_Centroid(bbox)),ST_Y(ST_Centroid(bbox)), bbox FROM ways WHERE tags->'amenity'='townhall' AND (tags->'name' like '%Grenoble%')");
					ResultSet res = stmt.executeQuery();
					
					System.out
							.println("Voici les coordonnées (en WGS84) du centroïde et la bbox de la mairie de Grenoble :");
					
					/* Affichage des réponses de la requête */
					while (res.next()) {
						System.out.println("\t longitude centroide = "
								+ res.getDouble(1));
						System.out.println("\t latitude centroide = "
								+ res.getDouble(2));
						System.out
								.println("\t bbox = "
										+ ((PGgeometry) res.getObject(3))
												.getGeometry());
					}

					res.close();
					Utils.closeConnection();

				} catch (SQLException e) {
					Logger.getLogger(Utils.class.getName()).log(Level.SEVERE,
							null, e);
				}
				break;

			case "9":
				// Question 9 : affiche tous les noms et coordonnées
				// géographiques des points correspondant à l'argument 2

				/* La question 9 doit avoir un deuxième argument pour pouvoir être testée */
				if (args.length < 2) {
					System.out
							.println("Pour tester la question 9, vous devez passer en deuxième argument le nom du lieu pour lequel vous cherchez les coordonnées.");
				} else {

					try {
						conn = Utils.getConnection();
						stmt = conn
								.prepareStatement("SELECT tags->'name', ST_X(geom), ST_Y(geom) FROM nodes WHERE tags->'name' LIKE ?");
						stmt.setString(1, args[1]);
						ResultSet res = stmt.executeQuery();
						
						/* Affichage des résultats de la question */
						while (res.next()) {
							System.out.println("nom = " + res.getString(1));
							System.out.println("\t longitude = "
									+ res.getDouble(2));
							System.out
									.println("\t latitude = " + res.getDouble(3));
						}

						res.close();
						Utils.closeConnection();

					} catch (SQLException e) {
						Logger.getLogger(Utils.class.getName()).log(
								Level.SEVERE, null, e);
					}
				}
				break;

			case "10":
				// Question 10 : affiche toutes les routes et tous les bâtiments
				// autour de Grenoble
						
				/* Tableau permettant de stocker les couleurs des types de routes principales */
				Map<String, Color> couleur = new HashMap<String, Color>();
				couleur.put("motorway", Color.blue);
				couleur.put("trunk", Color.green);
				couleur.put("primary", Color.red);
				couleur.put("secondary", new Color(237, 127, 16)); // Orange
				couleur.put("tertiary", new Color(255, 255, 20)); // Jaune
				couleur.put("unclassified", new Color(88, 41, 0)); // Marron
				couleur.put("residential", Color.lightGray);
				couleur.put("service", Color.pink);

				try {
					conn = Utils.getConnection();
					
					/* Affichage des routes */
					stmt = conn
							.prepareStatement("SELECT ST_Transform(linestring, 2154), tags->'highway' FROM ways WHERE tags->'highway' LIKE '%' AND ST_Intersects(linestring, ST_Setsrid(ST_GeomFromText('Polygon((5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1, 5.8 45.1))'),4326))");

					ResultSet res = stmt.executeQuery();

					/* Creation de la MapPanel avec les coordonnées correspondantes à Grenoble (en Lambert 93) */
					MapPanel mp = new MapPanel(915000, 6456200, 15000);

					geoexplorer.gui.LineString linestring;
					
					/* Pour chaque route on ajoute une lineString au MapPanel */
					while (res.next()) {
						String highway = res.getString(2);
						
						/* Récupération de la couleur correspondant au type de la route */
						Color color = couleur.get(highway);
						if (color != null) {
							linestring = new geoexplorer.gui.LineString(color);
						} else {
							linestring = new geoexplorer.gui.LineString();
						}

						PGgeometry geom = (PGgeometry) res.getObject(1);
						LineString ls = (LineString) geom.getGeometry();
						
						for (int p = 0; p < ls.numPoints(); p++) {
							Point pt = ls.getPoint(p);
							linestring.addPoint(new geoexplorer.gui.Point(pt.x,
									pt.y));
						}
						
						mp.addPrimitive(linestring);

					}
					res.close();

					/* Affichage des bâtiments */
					stmt = conn
							.prepareStatement("SELECT ST_Transform(bbox, 2154) FROM ways WHERE tags->'building' LIKE '%' AND ST_Intersects(linestring, ST_Setsrid(ST_GeomFromText('Polygon((5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1, 5.8 45.1))'),4326))");

					res = stmt.executeQuery();
					
					geoexplorer.gui.Polygon polygone;
					
					/* Pour chaque bâtiment on ajoute un polygone au MapPanel */
					while (res.next()) {
						polygone = new geoexplorer.gui.Polygon();

						PGgeometry geom = (PGgeometry) res.getObject(1);
						Polygon pol = (Polygon) geom.getGeometry();
						
						for (int p = 0; p < pol.numPoints(); p++) {
							Point pt = pol.getPoint(p);
							polygone.addPoint(new geoexplorer.gui.Point(pt.x,
									pt.y));
						}
						
						mp.addPrimitive(polygone);

					}
					res.close();
					Utils.closeConnection();
					
					/* Affichage de la carte */
					new GeoMainFrame("MapPanel", mp);

				} catch (SQLException e) {
					Logger.getLogger(Utils.class.getName()).log(Level.SEVERE,
							null, e);
				}
				break;

			case "11":

				try {
					conn = Utils.getConnection();
					
					/* Affichage des quartiers contenant des boulangeries */
					stmt = conn
							.prepareStatement("SELECT q.quartier, q.the_geom, COUNT(n) FROM quartier q, nodes n WHERE n.tags->'shop'='bakery' AND ST_Intersects(ST_Transform(q.the_geom, 4326), n.geom) GROUP BY q.quartier, q.the_geom ORDER BY COUNT(n) DESC");

					ResultSet res = stmt.executeQuery();

					/* Creation de la MapPanel avec les coordonnées correspondantes à Grenoble (en Lambert 93) */
					MapPanel mp = new MapPanel(915000, 6456200, 15000);
					
					geoexplorer.gui.Polygon polygone;
					
					/* Pour chaque quartier contenant au moins une boulangerie on ajoute un polygone au MapPanel */
					while (res.next()) {
						
						/* Choix de la couleur en fonction du nombre de boulangeries */
						Color couleur_quartier;
						int nb_boulangeries = res.getInt(3);
						if (nb_boulangeries < 5) {
							couleur_quartier = Color.yellow;
						} else if (nb_boulangeries < 10) {
							couleur_quartier = Color.orange;
						} else {
							couleur_quartier = Color.red;
						}
						polygone = new geoexplorer.gui.Polygon(Color.black,
								couleur_quartier);

						PGgeometry geom = (PGgeometry) res.getObject(2);
						MultiPolygon pol = (MultiPolygon) geom.getGeometry();
						
						for (int p = 0; p < pol.numPoints(); p++) {
							Point pt = pol.getPoint(p);
							polygone.addPoint(new geoexplorer.gui.Point(pt.x,
									pt.y));
						}
						mp.addPrimitive(polygone);

					}
					res.close();

					/* Affichage des routes de Grenoble pour faciliter l'observation des quartiers affichés */
					stmt = conn
							.prepareStatement("SELECT ST_Transform(linestring, 2154) FROM ways WHERE tags->'highway' LIKE '%' AND ST_Intersects(linestring, ST_Setsrid(ST_GeomFromText('Polygon((5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1, 5.8 45.1))'),4326))");

					res = stmt.executeQuery();
					geoexplorer.gui.LineString linestring;
					
					/* Pour chaque route on ajoute une linestring au MapPanel */
					while (res.next()) {
						linestring = new geoexplorer.gui.LineString(
								Color.darkGray);

						PGgeometry geom = (PGgeometry) res.getObject(1);
						LineString ls = (LineString) geom.getGeometry();
						for (int p = 0; p < ls.numPoints(); p++) {
							Point pt = ls.getPoint(p);
							linestring.addPoint(new geoexplorer.gui.Point(pt.x,
									pt.y));
						}
						mp.addPrimitive(linestring);

					}
					res.close();
					Utils.closeConnection();
					
					/* Affichage de la carte */
					new GeoMainFrame("MapPanel", mp);

				} catch (SQLException e) {
					Logger.getLogger(Utils.class.getName()).log(Level.SEVERE,
							null, e);
				}
				break;

			default:
				affiche_menu();
			}
		} else {
			affiche_menu();
		}
	}
}
