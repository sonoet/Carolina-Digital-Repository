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
package edu.unc.lib.dl.data.ingest.solr.filter.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.unc.lib.dl.data.ingest.solr.exception.IndexingException;
import edu.unc.lib.dl.data.ingest.solr.indexing.DocumentIndexingPackage;
import edu.unc.lib.dl.search.solr.model.IndexDocumentBean;
import edu.unc.lib.dl.xml.JDOMNamespaceUtil;

public class RLASupplementalFilter extends CollectionSupplementalInformationFilter {
	private static final Logger log = LoggerFactory.getLogger(RLASupplementalFilter.class);
	
	private static final String CATALOG_ID = "rla_catalog_number";
	private static final String CATALOG_FIELD = "rla_catalog_number_d";
	
	private static final String SITE_CODE_ID = "rla_site_code";
	private static final String SITE_CODE_FIELD = "rla_site_code_d";
	
	private static final String CONTEXT_1_ID = "rla_context_1";
	private static final String CONTEXT_1_FIELD = "rla_context_1_d";
	
	@Override
	public void filter(DocumentIndexingPackage dip) throws IndexingException {
		IndexDocumentBean idb = dip.getDocument();
		Element mods = dip.getMods();
		
		idb.setKeyword(new ArrayList<String>());
		if (mods != null) {
			if (idb.getDynamicFields() == null)
				idb.setDynamicFields(new HashMap<String, Object>());
			try {
				extractValues(mods, idb);
			} catch (JDOMException e) {
				log.error("Failed to set RLA fields", e);
			}
		}
	}
	
	private void extractValues(Element mods, IndexDocumentBean idb) throws JDOMException {
		List<?> elements = mods.getChildren();
		
		if (elements == null)
			return;
		
		for (Object elementObject : elements) {
			Element element = (Element) elementObject;
			
			if ("identifier".equals(element.getName())) {
				if (CATALOG_ID.equalsIgnoreCase(element.getAttributeValue("ID"))) {
					idb.getDynamicFields().put(CATALOG_FIELD, element.getTextTrim());
				}
			} else if ("subject".equals(element.getName())) {
				if (SITE_CODE_ID.equalsIgnoreCase(element.getAttributeValue("ID"))) {
					Element geographicEl = element.getChild("geographic", JDOMNamespaceUtil.MODS_V3_NS);
					idb.getDynamicFields().put(SITE_CODE_FIELD, geographicEl.getTextTrim());
				}
			} else if ("note".equals(element.getName())) {
				if (CONTEXT_1_ID.equalsIgnoreCase(element.getAttributeValue("ID"))) {
					idb.getDynamicFields().put(CONTEXT_1_FIELD, element.getTextTrim());
				}
			}
		}
	}
}
