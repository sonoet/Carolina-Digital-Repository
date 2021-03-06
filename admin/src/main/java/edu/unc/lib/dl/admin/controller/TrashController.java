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
package edu.unc.lib.dl.admin.controller;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.unc.lib.dl.search.solr.model.SearchRequest;
import edu.unc.lib.dl.search.solr.model.SearchResultResponse;
import edu.unc.lib.dl.search.solr.util.SearchFieldKeys;
import edu.unc.lib.dl.search.solr.util.SearchStateUtil;

@Controller
public class TrashController extends AbstractSearchController {
	protected static List<String> resultsFieldList = Arrays.asList(SearchFieldKeys.ID.name(),
			SearchFieldKeys.TITLE.name(), SearchFieldKeys.DATE_UPDATED.name(), SearchFieldKeys.RESOURCE_TYPE.name(),
			SearchFieldKeys.CONTENT_MODEL.name(), SearchFieldKeys.STATUS.name(), SearchFieldKeys.ANCESTOR_PATH.name(),
			SearchFieldKeys.VERSION.name(), SearchFieldKeys.ROLE_GROUP.name(), SearchFieldKeys.RELATIONS.name(),
			SearchFieldKeys.ANCESTOR_NAMES.name());
	
	@RequestMapping(value = "trash", method = RequestMethod.GET)
	public String trashForEverything(Model model, HttpServletRequest request) {
		SearchRequest searchRequest = generateSearchRequest(request);

		getTrash(searchRequest, model, request);
		return "search/trashList";
	}
	
	@RequestMapping(value = "trash/{pid}", method = RequestMethod.GET)
	public String trashForContainer(@PathVariable("pid") String pid, Model model, HttpServletRequest request) {
		SearchRequest searchRequest = generateSearchRequest(request);
		searchRequest.setRootPid(pid);

		getTrash(searchRequest, model, request);
		return "search/trashList";
	}
	
	private void getTrash(SearchRequest searchRequest, Model model, HttpServletRequest request) {
		searchRequest.getSearchState().setRowsPerPage(searchSettings.maxPerPage);
		// Force deleted status into the request
		searchRequest.getSearchState().getFacets().put(SearchFieldKeys.STATUS.name(), "Deleted");
		
		SearchResultResponse resultResponse = getSearchResults(searchRequest, resultsFieldList);

		model.addAttribute("searchStateUrl", SearchStateUtil.generateStateParameterString(searchRequest.getSearchState()));
		model.addAttribute("searchQueryUrl", SearchStateUtil.generateSearchParameterString(searchRequest.getSearchState()));
		model.addAttribute("resultResponse", resultResponse);
		model.addAttribute("queryMethod", "trash");
		request.getSession().setAttribute("resultOperation", "trash");
	}
}
