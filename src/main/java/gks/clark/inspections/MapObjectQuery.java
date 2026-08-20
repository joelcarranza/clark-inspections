/*
 *
 * NOTICES
 * -------
 *
 * Copyright 1999, 2000 by Gatekeeper Systems All Rights Reserved.
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
 *
 */
package gks.clark.inspections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gks.map.proxy.MGMapLayer;
import gks.map.proxy.MGMapObject;
import gks.util.QueryBuilder;

/**
 * Groups a set of selected {@link MGMapObject}s by their {@link MGMapLayer} and builds a
 * {@link QueryBuilder} query from that grouping, with one parameter per layer
 * (layer name -> comma-separated object keys).
 */
public class MapObjectQuery {

	private final Map<String,Set<String>> keysByLayer;

	private MapObjectQuery(Map<String,Set<String>> keysByLayer) {
		this.keysByLayer = keysByLayer;
	}

	public static MapObjectQuery fromMapObjects(MGMapObject mapObjects[]) {
		Map<String,Set<String>> keysByLayer = new HashMap<String,Set<String>>();
		for(MGMapObject mapObject : mapObjects) {
			MGMapLayer layer = mapObject.getMapLayer();
			Set<String> keys = keysByLayer.get(layer);
			if(keys == null) {
				keys = new HashSet<String>();
				keysByLayer.put(layer.getName(), keys);
			}
			keys.add(mapObject.getKey());
		}
		return new MapObjectQuery(keysByLayer);
	}

	public String buildQuery() {
		QueryBuilder q = new QueryBuilder();
		for(Map.Entry<String,Set<String>> e : keysByLayer.entrySet()) {
			q.append(e.getKey(), e.getValue());
		}
		return q.toString();
	}

}
