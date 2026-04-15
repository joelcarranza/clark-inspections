/*
 * NOTICES
 * -------
 * 
 * Copyright 2012 by Gatekeeper Systems All Rights Reserved.
 * 
 * Unpublished Work -- Protected under the copyright laws of the United States.
 * 
 * Restricted Rights Legend: Use, duplication or disclosure of the software
 * contained hereon is governed by the terms of a license agreement.  In
 * the absence of an agreement, use, duplication or disclosure by the United
 * States Government is subject to restrictions stated in subparagraph
 * (c)(1) of the Commercial Computer Software -- Restricted Rights clause
 * at FAR 52.227-9 or subparagraph (c)(1)(ii) of the Rights in Technical
 * Data and Computer Software clause at DFARS 252.227-7013, as applicable.
 * 
 * Contractor/Manufacturer:
 * 
 *     Gatekeeper Systems
 *     99 East C Street Ste. 209
 *     Upland, Ca. 91786
 * 
 *     Tel: (626) 449-8135
 *     Fax: (626) 440-1742
 * 
 *     E-Mail: info@gatekeeper.com
 *     URL:    http://www.gatekeeper.com/
 */
package gks.clark.inspections;

import java.util.ArrayList;
import java.util.Collection;

import gks.clark.inspections.model.WorkOrder;
import gks.map.MapControl;
import gks.map.event.MapViewListener;
import gks.map.proxy.LocalLayerOptions;
import gks.map.proxy.MGGeometry;
import gks.map.proxy.MGMap;
import gks.map.proxy.MGMapLayer;
import gks.util.BoundingBox;
import gks.util.Mappable;
import gks.util.ResourceLoader;

/**
 * Constructs and manages a locally drawn layer. The display properties of the map
 * feature are pulled directly from the "live" java objects instead of from a
 * backend database. Useful in scenarios where the underlying model objects may
 * not be stored in the database in a accessible form. 
 */
public class LocalMapLayerManager<T> {
	MGMapLayer layer;
	MapControl mapControl;
	final ArrayList<T> features;
	
	
	/**
	 * Name of locally drawn layer that will be created
	 */
	final String layerName;
	
	final String stylesResourcePath;
	
	
	public LocalMapLayerManager(MapControl mapControl, String layerName, String stylesResourcePath) {
		this.mapControl  = mapControl;
		this.layerName = layerName;
		this.stylesResourcePath = stylesResourcePath;
		this.features = new ArrayList<T>();
	}

	protected void createLayer() {
		LocalLayerOptions options = new LocalLayerOptions();
		options.setName(layerName);
		options.setLayerStyleLocation(ResourceLoader.getURL(stylesResourcePath));
		layer = mapControl.getMap().createMapLayer(options);
	}
	
	public void destroy() {
		if(layer != null) {
			mapControl.getMap().removeMapLayer(layer.getName());
		}
		layer = null;
	}
	
	/**
	 * Update the layer with the contents from the table view. The map will 
	 * be refreshed
	 */
	public void redraw() {
		redraw(null);
	}

	/**
	 * Update the layer with the contents from the table view, optionally
	 * invoking an action when the operation finishes. The map will 
	 * be refreshed
	 */
	public void redraw(final Runnable successCallback) {
		if(layerName == null) {
			return;
		}
		
		final MGMap map = mapControl.getMap();
		map.invokeMapAction(new Runnable() {

			public void run() {
				createMarkers(map);
				map.refresh();
				if(successCallback != null) {
					successCallback.run();
				}
			}
		});
	}

	

	protected MGGeometry geometryForObject(Mappable m) {
		BoundingBox bbox = m.getExtent(null);
		ArrayList<Double> coords = new ArrayList<Double>();
		coords.add(bbox.getCenterX());
		coords.add(bbox.getCenterY());		
		MGGeometry geometry = mapControl.getMap().createGeometry("point", coords);
		return geometry;
	}

	public void setFeatures(Collection<T> features) {
		this.features.clear();
		this.features.addAll(features);		
		
		redraw();
	}
	

	public void setFeature(T feature) {
		this.features.clear();
		if(feature != null) {
			this.features.add(feature);		
		}

		redraw();
	}
	
	public void clear() {
		this.features.clear();
		redraw();
	}

	
	
	public String getLayerName() {
		return layerName;
	}

	public String getStylesResourcePath() {
		return stylesResourcePath;
	}

	public MGMapLayer getLayer() {
		return layer;
	}


	/**
	 * Update the markers without refreshing the map. Generally, you DO NOT 
	 * want to call this function. Call redraw() instead. Map must not be busy.
	 * <p>
	 * This is primarily used for redrawing markers inside of the 
	 * {@link MapViewListener#mapViewChanging(gks.map.event.MapViewEvent)} event handler
	 *
	 * @since 4.1.5
	 */
	public void reloadMarkers() {
		createMarkers(mapControl.getMap());
	}
	
	private void createMarkers(final MGMap map) {
		if(layer == null) {
			createLayer();
		}
		else if(!layer.getName().equals(layerName)) { // we've changed the layer name
			layer.removeAllObjects();
			map.removeMapLayer(layer.getName());
			createLayer();
		}
		else {
			layer.removeAllObjects();
		}
		for(T d:features) {
			createMapObject(d);
		}
	}

	protected void createMapObject(T d) {
		Mappable m = (Mappable)d;
		if(m.isMappable()) {
			MGGeometry geometry = geometryForObject(m);
			String style = d.getClass().getName();
			layer.createMapObject(m.getMapKey(),null,null,style,geometry,false);
		}
	}



}
