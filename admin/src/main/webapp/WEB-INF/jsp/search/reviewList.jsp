<%--

    Copyright 2008 The University of North Carolina at Chapel Hill

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cdr" uri="http://cdr.lib.unc.edu/cdrUI" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page trimDirectiveWhitespaces="true" %>
<div class="result_page contentarea">
	<div class="search_menu"></div>

	<div class="result_area">
		<div>
		</div>
	</div>
</div>

<script>
	//console.log("Starting " + (new Date()).getTime());
	var require = {
		config: {
			'reviewList' : {
				'metadataObjects': ${cdr:resultsToJSON(resultResponse, accessGroupSet)},
				'pageStart' : ${resultResponse.searchState.startRow},
				'pageRows' : ${resultResponse.searchState.rowsPerPage},
				'resultCount' : ${resultResponse.resultCount},
				'resultUrl' : '${currentRelativeUrl}',
				'invalidVocabCount' : ${invalidVocabCount},
				'filterParams' : '${cdr:urlEncode(searchQueryUrl)}'
				<c:if test="${not empty resultResponse.selectedContainer}">
					, 'container' : ${cdr:metadataToJSON(resultResponse.selectedContainer, accessGroupSet)}
				</c:if>
			},
		}
	};
	//console.log("Loaded in " + ((new Date()).getTime() - startTimer));
</script>
<script type="text/javascript" src="/static/js/lib/require.js" data-main="/static/js/admin/reviewList"></script>