package com.psv.datos_mapas.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoJsonHelper {

    public static Map<String, Object> toFeatureCollection(List<Map<String, Object>> features) {
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "FeatureCollection");
        geoJson.put("features", features);
        return geoJson;
    }

    public static Map<String, Object> toFeature(Geometry geometry, Map<String, Object> properties) {
        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");
        feature.put("geometry", toGeometryObject(geometry));
        feature.put("properties", properties);
        return feature;
    }

    private static Map<String, Object> toGeometryObject(Geometry geometry) {
        Map<String, Object> geomObj = new HashMap<>();
        
        String type = geometry.getGeometryType();
        if ("MultiPolygon".equals(type)) {
            geomObj.put("type", "MultiPolygon");
        } else if ("Polygon".equals(type)) {
            geomObj.put("type", "Polygon");
        } else if ("Point".equals(type)) {
            geomObj.put("type", "Point");
        } else {
            geomObj.put("type", type);
        }
        
        geomObj.put("coordinates", toCoordinateArray(geometry));
        
        return geomObj;
    }

    private static Object toCoordinateArray(Geometry geometry) {
        String type = geometry.getGeometryType();

        if ("MultiPolygon".equals(type)) {
            // GeoJSON MultiPolygon: [ [ [ [x,y], ... ] ], ... ]
            int numGeometries = geometry.getNumGeometries();
            Object[][][][] result = new Object[numGeometries][][][];
            for (int i = 0; i < numGeometries; i++) {
                result[i] = new Object[][][] { toRing(geometry.getGeometryN(i)) };
            }
            return result;
        } else if ("Polygon".equals(type)) {
            // GeoJSON Polygon: [ [ [x,y], ... ] ]  (array de anillos)
            return new Object[][][] { toRing(geometry) };
        } else if ("Point".equals(type)) {
            Coordinate c = geometry.getCoordinate();
            return new Object[] { c.x, c.y };
        } else {
            // LineString u otros: [ [x,y], ... ]
            return toRing(geometry);
        }
    }

    private static Object[][] toRing(Geometry geometry) {
        Coordinate[] coords = geometry.getCoordinates();
        Object[][] ring = new Object[coords.length][];
        for (int i = 0; i < coords.length; i++) {
            ring[i] = new Object[] { coords[i].x, coords[i].y };
        }
        return ring;
    }
}