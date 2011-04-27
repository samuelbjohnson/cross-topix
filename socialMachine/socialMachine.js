

dojo.declare("net.samuelbjohnson.jsdev.crosstopix.SocialMachine", null, {
	constructor: function(/*node*/containerNode) {
		this.container = containerNode;
		
		this.buildHtml();
		
		this.constructQuery();
		
		this.runQuery();
	},
	
	buildHtml: function() {
	
	},
	
	constructQuery: function() {
		this.query = {
			output: "json",
			jsonp: "",
			key: "",
			query: "prefix dcterms: <http://purl.org/dc/terms/> \
prefix cpdl:	<http://www3.cpdl.org/wiki/index.php/> \
prefix imslp:   <http://imslp.org/wiki/> \
prefix xt:  	<http://purl.org/twc/vocab/cross-topix#> \
 \
SELECT ?comparison ?page_1 ?page_2 ?sim ?title_1 ?title_2 \
 \
WHERE { \
  GRAPH <http://leo.tw.rpi.edu/source/orange-joiner/dataset/title-similarities/version/2011-Apr-20> { \
 	?comparison xt:comparable_1 ?page_1; \
             	xt:comparable_2 ?page_2; \
             	xt:similarity   ?sim . \
  } \
  GRAPH <http://leo.tw.rpi.edu/source/orange-amanda/dataset/ground-truth/version/2011-Apr-19> { \
 	?page_1 dcterms:title ?title_1 . \
  } \
  GRAPH <http://leo.tw.rpi.edu/source/orange-amanda/dataset/ground-truth/version/2011-Apr-19> { \
 	?page_2 dcterms:title ?title_2 . \
  } \
} ORDER BY DESC(?sim) LIMIT 1 OFFSET 0"
		};
	},
	
	processProposedMatch: function(/*json object*/data) {
		console.log("received data: ", data);
	},
	
	processAjaxError: function() {
		alert("ajax error");
	},
	
	runQuery: function() {
		var query, queryString, xhrArgs, result;
		
		query = this.query;
		
		queryString = dojo.objectToQuery(query);
		
		xhrArgs = {
			url: "http://leo.tw.rpi.edu:81/endpoint.php?" + queryString,
			handleAs: "text",
			load: dojo.hitch(this, this.processProposedMatch),
			error: dojo.hitch(this, this.processAjaxError)
		};
		
		result = dojo.xhrGet(xhrArgs);
		console.log("result: ", result);
	}
});