/**
 * Copyright 2008 The University of North Carolina at Chapel Hill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.unc.lib.dl.data.ingest.solr.filter;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.unc.lib.dl.data.ingest.solr.IndexingException;
import edu.unc.lib.dl.data.ingest.solr.indexing.DocumentIndexingPackage;
import edu.unc.lib.dl.data.ingest.solr.util.JDOMQueryUtil;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.util.ContentModelHelper;
import edu.unc.lib.dl.xml.JDOMNamespaceUtil;

/**
 * Populates the relations field with pertinent triples from RELS-Ext that are primarily intended for post retrieval purposes.
 * 
 * @author bbpennel
 *
 */
public class SetRelationsFilter extends AbstractIndexDocumentFilter {
	private static final Logger log = LoggerFactory.getLogger(SetRelationsFilter.class);
	
	public SetRelationsFilter() {
	}
	
	@Override
	public void filter(DocumentIndexingPackage dip) throws IndexingException {
		if (dip.getFoxml() == null)
			throw new IndexingException("Unable to extract relations, no FOXML document was provided for " + dip.getPid().getPid());
		
		log.debug("Applying setRelationsFilter");
		List<String> relations = new ArrayList<String>();
		Element relsExt = dip.getRelsExt();
		try {
			// Retrieve the default web datastream
			String defaultWebData = this.getDefaultWebData(dip, relsExt);
			if (defaultWebData != null)
				relations.add(ContentModelHelper.CDRProperty.defaultWebData.name() + "|" + new PID(defaultWebData).getPid());
			
			// Retrieve the default web object, from the cached version if possible.
			DocumentIndexingPackage defaultWebObjectPackage = dip.getDefaultWebObject();
			String defaultWebObject;
			if (defaultWebObjectPackage != null) {
				defaultWebObject = defaultWebObjectPackage.getPid().getPid();
			} else {
				defaultWebObject = JDOMQueryUtil.getRelationValue(ContentModelHelper.CDRProperty.defaultWebObject.name(), JDOMNamespaceUtil.CDR_NS, relsExt);
			}
			if (defaultWebObject != null)
				relations.add(ContentModelHelper.CDRProperty.defaultWebObject.name() + "|" + (new PID(defaultWebObject)).getPid());
			
			// Retrieve original content datastream name for items with a main content payload
			String sourceData = JDOMQueryUtil.getRelationValue(ContentModelHelper.CDRProperty.sourceData.name(), JDOMNamespaceUtil.CDR_NS, relsExt);
			if (sourceData != null)
				relations.add(ContentModelHelper.CDRProperty.sourceData.name() + "|" + ((new PID(sourceData).getPid())));
			// Retrieve and store slug
			String slug = relsExt.getChildText(ContentModelHelper.CDRProperty.slug.name(), JDOMNamespaceUtil.CDR_NS);
			if (slug != null)
				relations.add(ContentModelHelper.CDRProperty.slug.name() + "|" + slug);
			// Retrieve the default sort order for a container if specified
			String defaultSortOrder = JDOMQueryUtil.getRelationValue(ContentModelHelper.CDRProperty.sortOrder.name(), JDOMNamespaceUtil.CDR_NS, relsExt);
			if (defaultSortOrder != null){
				defaultSortOrder = defaultSortOrder.substring(defaultSortOrder.indexOf('#') + 1);
				relations.add(ContentModelHelper.CDRProperty.sortOrder.name() + "|" + defaultSortOrder);
			}
			String embargoUntil = JDOMQueryUtil.getRelationValue(ContentModelHelper.CDRProperty.embargoUntil.name(), JDOMNamespaceUtil.CDR_ACL_NS, relsExt);
			if (embargoUntil != null)
				relations.add(ContentModelHelper.CDRProperty.embargoUntil.name() + "|" + embargoUntil);
			
			dip.getDocument().setRelations(relations);
		} catch (JDOMException e) {
			throw new IndexingException("Failed to set relations for " + dip.getPid(), e);
		}
	}
	
	private String getDefaultWebData(DocumentIndexingPackage dip, Element relsExt) throws JDOMException {
		String defaultWebData = JDOMQueryUtil.getRelationValue(ContentModelHelper.CDRProperty.defaultWebData.name(), JDOMNamespaceUtil.CDR_NS, relsExt);
		// If this object does not have a defaultWebData but its defaultWebObject does, then use that instead.
		if (defaultWebData == null && dip.getDefaultWebObject() != null) {
			defaultWebData = JDOMQueryUtil.getRelationValue(ContentModelHelper.CDRProperty.defaultWebData.name(), JDOMNamespaceUtil.CDR_NS, dip.getDefaultWebObject().getRelsExt());
		}
		if (defaultWebData == null)
			return null;
		return defaultWebData;
	}
}
